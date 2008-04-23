package edu.virginia.vcgr.genii.container.q2.resource;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;

public class QueueDBResource extends BasicDBResource implements IQueueResource
{
	public QueueDBResource(
			ResourceKey parentKey, 
			DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translater)
		throws SQLException
	{
		super(parentKey, connectionPool, translater);
	}
	
	public void destroy() throws ResourceException
	{
		super.destroy();
		
		PreparedStatement stmt = null;
		
		try
		{
			stmt = _connection.prepareStatement("DELETE FROM q2resources WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.addBatch();
			
			stmt.close();
			stmt = null;
			stmt = _connection.prepareStatement("DELETE FROM q2jobs WHERE queueid = ?");
			stmt.setString(1, _resourceKey);
			stmt.addBatch();
			
			stmt.executeBatch();
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
}