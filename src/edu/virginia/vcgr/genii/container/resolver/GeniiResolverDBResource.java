package edu.virginia.vcgr.genii.container.resolver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.axis.types.URI;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class GeniiResolverDBResource extends BasicDBResource implements IGeniiResolverResource
{
	static private final String _INSERT_RESOLVER_ENTRY_STMT = "INSERT INTO resolverentries VALUES (?, ?, ?, ?)";
	static private final String _DELETE_RESOLVER_ENTRY_STMT = "DELETE FROM resolverentries "
		+ "WHERE resourceid = ? and epi = ? and targetid = ?";
	static private final String _GET_TARGETEPI_LIST_STMT = "SELECT DISTINCT epi FROM resolverentries " + "WHERE resourceid = ?";
	static private final String _GET_TARGETEPR_STMT = "SELECT endpoint FROM resolverentries "
		+ "WHERE resourceid = ? AND epi = ? AND targetid = ?";
	static private final String _GET_TARGETID_LIST_STMT = "SELECT targetid FROM resolverentries "
		+ "WHERE resourceid = ? AND epi = ? " + "ORDER BY targetid";
	static private final String _GET_ENTRY_COUNT_STMT = "SELECT COUNT(targetid) FROM resolverentries " + "WHERE resourceid = ?";
	static private final String _LIST_RESOLVERS_STMT = "SELECT DISTINCT resourceid, epi FROM resolverentries";
	static private final String _GET_ALL_ENTRIES_STMT = "SELECT epi, targetid, endpoint from resolverentries "
		+ "WHERE resourceid = ?";

	public GeniiResolverDBResource(ResourceKey parentKey, DatabaseConnectionPool connectionPool) throws SQLException
	{
		super(parentKey, connectionPool);
	}

	public void addTargetEPR(URI targetEPI, int targetID, EndpointReferenceType targetEPR) throws ResourceException
	{
		PreparedStatement stmt = null;
		try {
			stmt = _connection.prepareStatement(_INSERT_RESOLVER_ENTRY_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, targetEPI.toString());
			stmt.setString(3, Integer.toString(targetID));
			stmt.setBlob(4, EPRUtils.toBlob(targetEPR, "resolverentries", "endpoint"));
			if (stmt.executeUpdate() != 1)
				throw new SQLException("Unable to insert resolver entry");
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public void removeTargetEPR(URI targetEPI, int targetID) throws ResourceException
	{
		PreparedStatement stmt = null;
		try {
			stmt = _connection.prepareStatement(_DELETE_RESOLVER_ENTRY_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, targetEPI.toString());
			stmt.setString(3, Integer.toString(targetID));
			stmt.executeUpdate();
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(stmt);
		}
	}

	public URI[] getTargetEPIList() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet result = null;
		List<URI> targetEPIList = new ArrayList<URI>();
		try {
			stmt = _connection.prepareStatement(_GET_TARGETEPI_LIST_STMT);
			stmt.setString(1, _resourceKey);
			result = stmt.executeQuery();
			if (result != null) {
				while (result.next()) {
					String epi = result.getString(1);
					targetEPIList.add(new URI(epi));
				}
			}
		} catch (Exception exception) {
			throw new ResourceException(exception.getLocalizedMessage(), exception);
		} finally {
			StreamUtils.close(result);
			StreamUtils.close(stmt);
		}
		return targetEPIList.toArray(new URI[targetEPIList.size()]);
	}

	public EndpointReferenceType getTargetEPR(URI targetEPI, int targetID) throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet result = null;
		EndpointReferenceType targetEPR = null;
		try {
			stmt = _connection.prepareStatement(_GET_TARGETEPR_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, targetEPI.toString());
			stmt.setString(3, Integer.toString(targetID));
			result = stmt.executeQuery();
			if (result.next()) {
				targetEPR = EPRUtils.fromBlob(result.getBlob(1));
			}
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(result);
			StreamUtils.close(stmt);
		}
		return targetEPR;
	}

	public int[] getTargetIDList(URI targetEPI) throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet result = null;
		List<Integer> targetIDList = new ArrayList<Integer>();
		try {
			stmt = _connection.prepareStatement(_GET_TARGETID_LIST_STMT);
			stmt.setString(1, _resourceKey);
			stmt.setString(2, targetEPI.toString());
			result = stmt.executeQuery();
			if (result != null) {
				while (result.next()) {
					String value = result.getString(1);
					targetIDList.add(new Integer(value));
				}
			}
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(result);
			StreamUtils.close(stmt);
		}
		int[] retval = new int[targetIDList.size()];
		for (int idx = 0; idx < retval.length; idx++)
			retval[idx] = targetIDList.get(idx).intValue();
		return retval;
	}

	/**
	 * Return the total number of entries in the resolverEntries table for this resource.
	 */
	public int getEntryCount() throws ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = _connection.prepareStatement(_GET_ENTRY_COUNT_STMT);
			stmt.setString(1, _resourceKey);
			result = stmt.executeQuery();
			if (result.next())
				return result.getInt(1);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(result);
			StreamUtils.close(stmt);
		}
		return 0;
	}

	/**
	 * Return a map from targetEPI to resolver resourceKey for each targetEPI defined in the local
	 * database.
	 */
	public HashMap<URI, String> listAllResolvers() throws ResourceException
	{
		ResultSet rs = null;
		PreparedStatement stmt = null;
		HashMap<URI, String> results = new HashMap<URI, String>();
		try {
			stmt = _connection.prepareStatement(_LIST_RESOLVERS_STMT);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String resourceKey = rs.getString(1);
				URI targetEPI = new URI(rs.getString(2));
				results.put(targetEPI, resourceKey);
			}
		} catch (Exception exception) {
			throw new ResourceException(exception.getLocalizedMessage(), exception);
		} finally {
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
		return results;
	}

	public void writeAllEntries(ObjectOutputStream ostream) throws ResourceException, IOException
	{
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = _connection.prepareStatement(_GET_ALL_ENTRIES_STMT);
			stmt.setString(1, _resourceKey);
			result = stmt.executeQuery();
			while (result.next()) {
				ostream.writeObject(result.getString(1));
				ostream.writeInt(result.getInt(2));
				// Blob -> byte[] -> EPR -> byte[] -> Stream, or worse...
				EndpointReferenceType targetEPR = EPRUtils.fromBlob(result.getBlob(3));
				ostream.writeObject(EPRUtils.toBytes(targetEPR));
			}
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		} finally {
			StreamUtils.close(result);
			StreamUtils.close(stmt);
		}
	}
}
