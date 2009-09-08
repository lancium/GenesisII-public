package edu.virginia.vcgr.genii.container.resource.db.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.hsqldb.Types;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ResourceSummary
{
	/**
	 * Retrieves a list of all known resources in the container, grouped by the
	 * fully qualified name of the implementing class.
	 * 
	 * @param connection The database connection to use for querying.
	 * @return A map of implementing class, to collection of resources in
	 * existance for that class.
	 */
	static public Map<String, Collection<ResourceSummaryInformation>> 
		resources(Connection connection) throws SQLException
	{
		Map<String, Collection<ResourceSummaryInformation>> ret =
			new HashMap<String, Collection<ResourceSummaryInformation>>();
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try
		{
			ps = connection.prepareStatement(
				"SELECT resourceid, humanname, epi, implementingclass " +
				"FROM resources2");
			rs = ps.executeQuery();
			
			while (rs.next())
			{
				String implClassString = rs.getString(4);
				Collection<ResourceSummaryInformation> list = ret.get(
					implClassString);
				if (list == null)
					ret.put(implClassString, 
						list = new LinkedList<ResourceSummaryInformation>());
				list.add(new ResourceSummaryInformation(rs.getString(1),
					rs.getString(2), rs.getString(3), implClassString));
			}
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(ps);
		}
	}
	
	static public Collection<ResourceSummaryInformation> resourcesFor(
		Connection connection, Class<?> implementingClass)
			throws SQLException
	{
		return resourcesForClass(connection, implementingClass.getName());
	}
	
	static public Collection<ResourceSummaryInformation> resourcesForClass(
		Connection connection, String implementingClassName) throws SQLException
	{
		Collection<ResourceSummaryInformation> ret =
			new LinkedList<ResourceSummaryInformation>();
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT resourceid, humanname, epi FROM resources2 " +
				"WHERE implementingClass = ?");
			stmt.setString(1, implementingClassName);
			rs = stmt.executeQuery();
			
			while (rs.next())
			{
				ret.add(new ResourceSummaryInformation(
					rs.getString(1), rs.getString(2),
					rs.getString(3), implementingClassName));
			}
			
			return ret;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static public EndpointReferenceType getEPR(Connection connection, 
		String resourceID) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"SELECT epr FROM resources2 WHERE resourceid = ?");
			stmt.setString(1, resourceID);
			rs = stmt.executeQuery();
			if (rs.next())
				return EPRUtils.fromBlob(rs.getBlob(1));
			
			return null;
		}
		finally
		{
			StreamUtils.close(rs);
			StreamUtils.close(stmt);
		}
	}
	
	static public void addResource(Connection connection,
		String resourceID, String humanName, Class<?> implementingClass,
		EndpointReferenceType epr) throws SQLException, ResourceException
	{
		PreparedStatement stmt = null;
		
		WSName name = new WSName(epr);
		String epi = name.getEndpointIdentifier().toString();
		
		try
		{
			stmt = connection.prepareStatement(
				"INSERT INTO resources2(" +
					"resourceid, humanname, epi, epr, implementingclass) " +
				"VALUES(?, ?, ?, ?, ?)");
			stmt.setString(1, resourceID);
			if (humanName == null)
				stmt.setNull(2, Types.VARCHAR);
			else
				stmt.setString(2, humanName);
			stmt.setString(3, epi);
			stmt.setBlob(4, EPRUtils.toBlob(epr, "resources2", "epr"));
			stmt.setString(5, implementingClass.getName());
			if (stmt.executeUpdate() != 1)
				throw new SQLException(String.format(
					"Unable to add resource \"%s\" to resources2 table.",
					resourceID));
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
	
	static public void removeResources(Connection connection,
		String...resourceIDs) throws SQLException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = connection.prepareStatement(
				"DELETE FROM resources2 WHERE resourceid = ?");
			for (String resourceID : resourceIDs)
			{
				stmt.setString(1, resourceID);
				stmt.addBatch();
			}
			
			stmt.executeBatch();
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}