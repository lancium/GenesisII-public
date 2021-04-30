package edu.virginia.vcgr.genii.container.resource.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ResourceSummary
{
	static private Log _logger = LogFactory.getLog(ResourceSummary.class);

	/**
	 * Retrieves a list of all known resources in the container, grouped by the fully qualified name of the implementing class.
	 * 
	 * @param connection
	 *            The database connection to use for querying.
	 * @return A map of implementing class, to collection of resources in existance for that class.
	 */
	static public Map<String, Collection<ResourceSummaryInformation>> resources(Connection connection) throws SQLException
	{
		Map<String, Collection<ResourceSummaryInformation>> ret = new HashMap<String, Collection<ResourceSummaryInformation>>();

		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = connection.prepareStatement("SELECT resourceid, humanname, epi, implementingclass " + "FROM resources2");
			long startTime = System.currentTimeMillis();
			rs = ps.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("get resources time is " + (System.currentTimeMillis() - startTime));

			while (rs.next()) {
				String implClassString = rs.getString(4);
				Collection<ResourceSummaryInformation> list = ret.get(implClassString);
				if (list == null)
					ret.put(implClassString, list = new LinkedList<ResourceSummaryInformation>());
				list.add(new ResourceSummaryInformation(rs.getString(1), rs.getString(2), rs.getString(3), implClassString));
			}

			return ret;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(ps);
		}
	}

	static public Collection<ResourceSummaryInformation> resourcesFor(Connection connection, Class<?> implementingClass) throws SQLException
	{
		return resourcesForClass(connection, implementingClass.getName());
	}

	static public Collection<ResourceSummaryInformation> resourcesForClass(Connection connection, String implementingClassName)
		throws SQLException
	{
		Collection<ResourceSummaryInformation> ret = new LinkedList<ResourceSummaryInformation>();

		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT resourceid, humanname, epi FROM resources2 " + "WHERE implementingClass = ?");
			stmt.setString(1, implementingClassName);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("resourcesforclass time is " + (System.currentTimeMillis() - startTime));

			while (rs.next()) {
				ret.add(new ResourceSummaryInformation(rs.getString(1), rs.getString(2), rs.getString(3), implementingClassName));
			}

			return ret;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public EndpointReferenceType getEPR(Connection connection, String resourceID) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT epr FROM resources2 WHERE resourceid = ?");
			stmt.setString(1, resourceID);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("getEPR time is " + (System.currentTimeMillis() - startTime));

			if (rs.next())
				return EPRUtils.fromBlob(rs.getBlob(1));

			return null;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public EndpointReferenceType getEPRFromEPI(Connection connection, String epi) throws ResourceException, SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			stmt = connection.prepareStatement("SELECT epr FROM resources2 WHERE epi = ?");
			stmt.setString(1, epi);
			long startTime = System.currentTimeMillis();
			rs = stmt.executeQuery();
			if (DatabaseConnectionPool.ENABLE_DB_TIMING_LOGS && _logger.isDebugEnabled())
				_logger.debug("getEPR from EPI time is " + (System.currentTimeMillis() - startTime));

			if (rs.next())
				return EPRUtils.fromBlob(rs.getBlob(1));

			return null;
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}

	static public void addResource(Connection connection, String resourceID, String humanName, Class<?> implementingClass,
		EndpointReferenceType epr) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;

		WSName name = new WSName(epr);
		String epi = name.getEndpointIdentifier().toString();

		try {
			stmt = connection.prepareStatement(
				"INSERT INTO resources2(" + "resourceid, humanname, epi, epr, implementingclass) " + "VALUES(?, ?, ?, ?, ?)");
			stmt.setString(1, resourceID);
			if (humanName == null)
				stmt.setNull(2, Types.VARCHAR);
			else
				stmt.setString(2, humanName);
			stmt.setString(3, epi);
			stmt.setBlob(4, EPRUtils.toBlob(epr, "resources2", "epr"));
			stmt.setString(5, implementingClass.getName());
			if (stmt.executeUpdate() != 1)
				throw new SQLException(String.format("Unable to add resource \"%s\" to resources2 table.", resourceID));
		} finally {
			StreamUtils.close(stmt);
		}
	}
	
	static public void updateEPR(Connection connection, EndpointReferenceType epr, String resourceID) 
			throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		
		try {
			stmt = connection.prepareStatement("UPDATE resources2 SET epr = ? WHERE resourceid = ?");
			stmt.setBlob(1, EPRUtils.toBlob(epr, "resources2", "epr"));
			stmt.setString(2, resourceID);
			if (stmt.executeUpdate() != 1)
				throw new SQLException(String.format("Unable to update resource \"%s\" to resources2 table.", resourceID));
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static public void removeResources(Connection connection, String... resourceIDs) throws SQLException
	{
		PreparedStatement stmt = null;

		try {
			stmt = connection.prepareStatement("DELETE FROM resources2 WHERE resourceid = ?");
			for (String resourceID : resourceIDs) {
				stmt.setString(1, resourceID);
				stmt.addBatch();
			}

			stmt.executeBatch();
		} finally {
			StreamUtils.close(stmt);
		}
	}

	static public void cleanupLeakedResources(Connection connection) throws SQLException
	{
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("DELETE FROM resources2 " + "WHERE resourceid NOT IN " + "(SELECT resourceid FROM resources)");
			stmt.executeUpdate();
		} finally {
			StreamUtils.close(stmt);
		}
	}
}