package edu.virginia.vcgr.genii.container.rns;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class RNSDBResourceFactory extends BasicDBResourceFactory implements
		IResourceFactory
{
	static private final String _CREATE_ENTRY_TABLE_STMT =
		"CREATE TABLE entries (resourceid VARCHAR(128), name VARCHAR(256), " +
		"endpoint BLOB (128K), id VARCHAR(40), " +
		"attrs VARCHAR (8192) FOR BIT DATA, " +
		"CONSTRAINT contextsconstraint1 PRIMARY KEY (resourceid, name))";
	
	public RNSDBResourceFactory(DatabaseConnectionPool connectionPool)
		throws SQLException
	{
		super(connectionPool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new RNSDBResource(parentKey, _pool);
		}
		catch (SQLException sqe)
		{
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}
	
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		Statement stmt = null;
		
		super.createTables();
		
		try
		{
			conn = _pool.acquire();
			stmt = conn.createStatement();
			
			stmt.executeUpdate(_CREATE_ENTRY_TABLE_STMT);
			conn.commit();
		}
		catch (SQLException sqe)
		{
//			 assume the table already exists.
		}
		finally
		{
			if (stmt != null)
				try { stmt.close(); } catch (SQLException sqe) {}
			if (conn != null)
				_pool.release(conn);
		}
	}
}