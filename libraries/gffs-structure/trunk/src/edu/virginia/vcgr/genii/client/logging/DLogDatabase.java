package edu.virginia.vcgr.genii.client.logging;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.logging.DLogConstants;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.LogEntryType;
import edu.virginia.vcgr.genii.common.RPCCallerType;
import edu.virginia.vcgr.genii.common.RPCMetadataType;

public class DLogDatabase
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(DLogDatabase.class);

	private static final int MAX_IDLE_CONNECTIONS = 3;

	protected ArrayList<Connection> connections = new ArrayList<Connection>();
	protected static ArrayList<DLogDatabase> connectors = new ArrayList<DLogDatabase>();

	protected String dbUrl;
	protected String dbUser;
	protected String dbPass;
	protected String entryTable;
	protected String metadataTable;
	protected String hierarchyTable;

	// Query Strings
	private String selectEPR = null;
	private String selectParent = null;
	private String selectParentCmd = null;
	private String selectChildIDs = null;
	private String whereChildIDs = null;
	private String selectChildren1 = null;
	private String selectChildren2 = null;
	private String whereChildren1 = null;
	private String whereChildren2 = null;
	private String selectLog = null;
	private String whereLog = null;
	private String hierarchySql = null;
	private String metadataSql1 = null;
	private String metadataSql2 = null;
	private String insertCmd = null;

	public static DLogDatabase getLocalConnector()
	{
		if (!connectors.isEmpty())
			return connectors.get(0);
		else
			return null;
	}

	public DLogDatabase()
	{
	}

	public DLogDatabase(String _dbUrl, String _dbUser, String _dbPass, String _entryTable, String _metadataTable,
		String _hierarchyTable)
	{
		dbUrl = _dbUrl;
		dbUser = _dbUser;
		dbPass = _dbPass;
		entryTable = _entryTable;
		metadataTable = _metadataTable;
		hierarchyTable = _hierarchyTable;

		initQueries();
		connectors.add(this);
	}

	protected void initQueries()
	{
		hierarchySql =
			"INSERT INTO " + hierarchyTable + " (" + DLogConstants.DLOG_HIERARCHY_CHILD + ", "
				+ DLogConstants.DLOG_HIERARCHY_PARENT + ", " + DLogConstants.DLOG_HIERARCHY_DATE + ") "
				+ "VALUES (?, ?, NOW());";

		metadataSql1 =
			"INSERT INTO " + metadataTable + " (" + DLogConstants.DLOG_METADATA_RPCID + ", "
				+ DLogConstants.DLOG_METADATA_DATE_SENT + ", " + DLogConstants.DLOG_METADATA_EPR + ", "
				+ DLogConstants.DLOG_METADATA_REQUEST + ", " + DLogConstants.DLOG_METADATA_OP_NAME + ") "
				+ "VALUES (?, NOW(), ?, ?, ?);";

		metadataSql2 =
			"UPDATE " + metadataTable + " SET " + DLogConstants.DLOG_METADATA_RPCID + " = ?, "
				+ DLogConstants.DLOG_METADATA_DATE_RCVD + " = NOW(), " + DLogConstants.DLOG_METADATA_RESPONSE + " = ? "
				+ "WHERE " + DLogConstants.DLOG_METADATA_RPCID + " = ?";

		selectEPR =
			"SELECT " + DLogConstants.DLOG_METADATA_EPR + " FROM " + metadataTable + " WHERE "
				+ DLogConstants.DLOG_METADATA_RPCID + " = ?";

		selectChildIDs = "SELECT * FROM " + hierarchyTable;
		whereChildIDs = " WHERE " + DLogConstants.DLOG_HIERARCHY_PARENT + " = ?";

		selectChildren1 = "SELECT * FROM " + hierarchyTable;
		whereChildren1 = " WHERE " + DLogConstants.DLOG_HIERARCHY_PARENT + " = ?";

		selectChildren2 = "SELECT * FROM " + metadataTable;
		whereChildren2 = " WHERE " + DLogConstants.DLOG_METADATA_RPCID + " = ?";

		selectLog = "SELECT * FROM " + entryTable;
		whereLog = " WHERE " + DLogConstants.DLOG_ENTRY_FIELD_RPCID + " = ?";

		selectParent = "SELECT DISTINCT " + DLogConstants.DLOG_ENTRY_FIELD_RPCID + " FROM " + entryTable;

		selectParentCmd =
			"SELECT " + DLogConstants.DLOG_METADATA_OP_NAME + " FROM " + metadataTable + " WHERE "
				+ DLogConstants.DLOG_METADATA_RPCID + " = ?";

		insertCmd =
			"INSERT INTO " + metadataTable + " ( " + DLogConstants.DLOG_METADATA_RPCID + ", "
				+ DLogConstants.DLOG_METADATA_OP_NAME + ", " + DLogConstants.DLOG_METADATA_DATE_SENT + " ) "
				+ " VALUES (?, ?, NOW())";
	}

	public Connection getConnection() throws SQLException
	{
		if (connections.isEmpty())
			return DriverManager.getConnection(dbUrl, dbUser, dbPass);
		else
			return connections.remove(0);
	}

	public Collection<LogEntryType> selectLogs(String rpcID) throws SQLException
	{
		Connection con = null;
		Collection<LogEntryType> ret = new ArrayList<LogEntryType>();
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			con = getConnection();

			if (rpcID == null) {
				stmt = con.prepareStatement(selectLog);
			} else {
				stmt = con.prepareStatement(selectLog + whereLog);
				stmt.setString(1, rpcID);
			}
			res = stmt.executeQuery();
			if (res != null) {
				while (res.next()) {
					ret.add(new LogEntryType(res.getString(DLogConstants.DLOG_ENTRY_FIELD_DATE), res
						.getString(DLogConstants.DLOG_ENTRY_FIELD_MESSAGE), res
						.getString(DLogConstants.DLOG_ENTRY_FIELD_LOGGER), res.getString(DLogConstants.DLOG_ENTRY_FIELD_LEVEL),
						res.getString(DLogConstants.DLOG_ENTRY_FIELD_RPCID), res
							.getString(DLogConstants.DLOG_ENTRY_FIELD_STACK_TRACE)));
				}
			}
		} finally {
			StreamUtils.close(res);
			StreamUtils.close(stmt);
			closeConnection(con);
		}
		return ret;
	}

	public EndpointReferenceType getEndpoint(String rpcID) throws SQLException
	{
		Connection con = null;
		EndpointReferenceType ret = null;
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			con = getConnection();

			if (rpcID == null) {
				throw new SQLException("Can't get EPR from database without RPCID");
			}
			stmt = con.prepareStatement(selectEPR);
			stmt.setString(1, rpcID);
			res = stmt.executeQuery();
			if (res != null) {
				while (res.next()) {
					try {
						ret = EPRUtils.fromBlob(res.getBlob(DLogConstants.DLOG_METADATA_EPR));
					} catch (ResourceException e) {
						throw new SQLException("Problem converting database object into EPR type", e);
					}
				}
			}
		} finally {
			StreamUtils.close(res);
			StreamUtils.close(stmt);
			closeConnection(con);
		}
		return ret;
	}

	public Collection<String> selectParentIDs() throws SQLException
	{
		Connection con = null;
		Collection<String> ret = new ArrayList<String>();
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			con = getConnection();
			stmt = con.prepareStatement(selectParent);
			res = stmt.executeQuery();
			if (res != null) {
				while (res.next()) {
					ret.add(res.getString(DLogConstants.DLOG_ENTRY_FIELD_RPCID));
				}
			}
		} finally {
			StreamUtils.close(res);
			StreamUtils.close(stmt);
			closeConnection(con);
		}
		return ret;
	}

	public Map<String, Collection<String>> selectChildIDs(String rpcID) throws SQLException
	{
		Connection con = null;
		Map<String, Collection<String>> ret = new HashMap<String, Collection<String>>();
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			con = getConnection();

			if (rpcID == null) {
				stmt = con.prepareStatement(selectChildIDs);
			} else {
				stmt = con.prepareStatement(selectChildIDs + whereChildIDs);
				stmt.setString(1, rpcID);
			}
			res = stmt.executeQuery();
			if (res != null) {
				while (res.next()) {
					if (!ret.containsKey(res.getString(DLogConstants.DLOG_HIERARCHY_PARENT))) {
						ret.put(res.getString(DLogConstants.DLOG_HIERARCHY_PARENT), new ArrayList<String>());
					}
					ret.get(res.getString(DLogConstants.DLOG_HIERARCHY_PARENT)).add(
						res.getString(DLogConstants.DLOG_HIERARCHY_CHILD));
				}
			}
		} finally {
			StreamUtils.close(res);
			StreamUtils.close(stmt);
			closeConnection(con);
		}
		return ret;

	}

	public Map<String, Collection<RPCCallerType>> selectChildren(String rpcID) throws SQLException, ResourceException
	{
		Connection con = null;
		Map<String, Collection<RPCCallerType>> ret = new HashMap<String, Collection<RPCCallerType>>();
		PreparedStatement stmt = null;
		ResultSet res = null;

		// Two phases, start with the IDs first
		try {
			con = getConnection();

			if (rpcID == null) {
				stmt = con.prepareStatement(selectChildren1);
			} else {
				stmt = con.prepareStatement(selectChildren1 + whereChildren1);
				stmt.setString(1, rpcID);
			}
			res = stmt.executeQuery();
			if (res != null) {
				while (res.next()) {
					if (!ret.containsKey(res.getString(DLogConstants.DLOG_HIERARCHY_PARENT))) {
						ret.put(res.getString(DLogConstants.DLOG_HIERARCHY_PARENT), new ArrayList<RPCCallerType>());
					}
					RPCCallerType entry = new RPCCallerType();
					entry.setRpcid(res.getString(DLogConstants.DLOG_HIERARCHY_CHILD));
					ret.get(res.getString(DLogConstants.DLOG_HIERARCHY_PARENT)).add(entry);
				}
			}
		} finally {
			StreamUtils.close(res);
			StreamUtils.close(stmt);
		}

		// Now get the metadata
		try {
			for (String parentID : ret.keySet()) {
				Collection<RPCCallerType> children = ret.get(parentID);

				for (RPCCallerType child : children) {
					String childID = child.getRpcid();

					RPCMetadataType meta = new RPCMetadataType("unknownMethod", null, null, null, null, null);

					stmt = con.prepareStatement(selectChildren2 + whereChildren2);
					stmt.setString(1, childID);
					res = stmt.executeQuery();
					if (res != null) {
						if (res.next()) {
							meta.setIssueDate(res.getString(DLogConstants.DLOG_METADATA_DATE_SENT));
							meta.setReturnDate(res.getString(DLogConstants.DLOG_METADATA_DATE_RCVD));
							meta.setTargetEPR(EPRUtils.fromBlob(res.getBlob(DLogConstants.DLOG_METADATA_EPR)));

							Blob blob = res.getBlob(DLogConstants.DLOG_METADATA_REQUEST);
							if (blob != null) {
								String request = new String(blob.getBytes(1, (int) blob.length()));
								meta.setRequestMessage(request);
							}

							blob = res.getBlob(DLogConstants.DLOG_METADATA_RESPONSE);
							if (blob != null) {
								String response = new String(blob.getBytes(1, (int) blob.length()));
								meta.setResponseMessage(response);
							}

							meta.setMethodName(res.getString(DLogConstants.DLOG_METADATA_OP_NAME));
						}
					}
					child.setMetadata(meta);
				}
			}
		} finally {
			StreamUtils.close(res);
			StreamUtils.close(stmt);
			closeConnection(con);
		}

		return ret;
	}

	public void recordRPCID(String rpcid) throws SQLException, AxisFault
	{
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement(hierarchySql);
			stmt.setString(1, rpcid);
			String parentRPCID = DLogUtils.getRPCID();
			if (parentRPCID != null) {
				stmt.setString(2, parentRPCID);
			} else {
				stmt.setString(2, "NULL RPCID");
			}
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
			closeConnection(con);
		}
	}

	public void recordCommand(String commandLine) throws SQLException
	{
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement(insertCmd);
			stmt.setString(1, DLogUtils.getRPCID());
			stmt.setString(2, commandLine);
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
			closeConnection(con);
		}
	}

	public void recordMeta1(String tempID, byte[] body, EndpointReferenceType epr, String op) throws SQLException, AxisFault,
		SOAPException
	{
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement(metadataSql1);
			stmt.setString(1, tempID);
			stmt.setBlob(2, EPRUtils.toBlob(epr, metadataTable, DLogConstants.DLOG_METADATA_EPR));
			stmt.setBlob(3, new SerialBlob(body));
			stmt.setString(4, op);
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
			closeConnection(con);
		}
	}

	protected void closeConnection(Connection con)
	{
		try {
			// just in case
			con.commit();
		} catch (SQLException e) {
		}

		if (connections.size() >= MAX_IDLE_CONNECTIONS) {
			StreamUtils.close(con);
		} else {
			connections.add(con);
		}
	}

	public void recordMeta2(String tempID, byte[] body, String rpcid) throws SQLException, AxisFault, SOAPException
	{
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = getConnection();
			stmt = con.prepareStatement(metadataSql2);
			stmt.setString(1, rpcid);
			stmt.setBlob(2, new SerialBlob(body));
			stmt.setString(3, tempID);
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
			closeConnection(con);
		}
	}

	public String getCommand(String rpcid) throws SQLException, ResourceException
	{
		Connection con = null;
		String ret = null;
		PreparedStatement stmt = null;
		ResultSet res = null;

		try {
			con = getConnection();
			stmt = con.prepareStatement(selectParentCmd);
			stmt.setString(1, rpcid);
			res = stmt.executeQuery();
			if (res != null) {
				if (res.next()) {
					ret = res.getString(DLogConstants.DLOG_METADATA_OP_NAME);
				}
			}
		} finally {
			StreamUtils.close(res);
			StreamUtils.close(stmt);
			closeConnection(con);
		}
		return ret;
	}
}
