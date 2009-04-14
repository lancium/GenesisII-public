package edu.virginia.vcgr.genii.container.q2.resource;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class QueueDBResource extends BasicDBResource implements IQueueResource
{
	public QueueDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(parentKey, connectionPool);
	}
	
	public void destroy() throws ResourceException
	{
		super.destroy();
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement("DELETE FROM q2resources WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			
			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2jobs WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			
			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2eprs WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
			
			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2errors WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.executeUpdate();
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Error while trying to destroy resource.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}

	@Override
	public void setEPR(EndpointReferenceType epr) throws ResourceException
	{
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement(
				"INSERT INTO q2eprs (queueid, queueepr) VALUES (?, ?)");
			stmt.setString(1, _resourceKey);
			stmt.setBlob(2, EPRUtils.toBlob(epr));
			stmt.executeUpdate();
			
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(
				"Error while trying to set EPR for queue resource.", sqe);
		}
		finally
		{
			StreamUtils.close(stmt);
		}
	}
}