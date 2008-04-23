package edu.virginia.vcgr.genii.container.resource.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class BasicDBResourceFactory implements IResourceFactory
{
	static private final String _CREATE_KEY_TABLE_STMT =
		"CREATE TABLE resources (resourceid VARCHAR(128) PRIMARY KEY," +
			"createtime TIMESTAMP)";
	static private final String _CREATE_PROPERTY_TABLE_STMT =
		"CREATE TABLE properties (resourceid VARCHAR(128), propname VARCHAR(256)," +
			"propvalue BLOB (128K), CONSTRAINT propertiesconstraint1 " +
			"PRIMARY KEY (resourceid, propname))";
		
	protected DatabaseConnectionPool _pool;
	protected IResourceKeyTranslater _translater;
	
	public BasicDBResourceFactory(DatabaseConnectionPool pool, IResourceKeyTranslater translater)
		throws SQLException
	{
		_pool = pool;
		_translater = translater;
		createTables();
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new BasicDBResource(parentKey, _pool, _translater);
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
		
		try
		{
			conn = _pool.acquire();
			stmt = conn.createStatement();
			
			stmt.executeUpdate(_CREATE_KEY_TABLE_STMT);
			stmt.executeUpdate(_CREATE_PROPERTY_TABLE_STMT);
			conn.commit();
		}
		catch (SQLException sqe)
		{
			// assume the table already exists.
		}
		finally
		{
			if (stmt != null)
				try { stmt.close(); } catch (SQLException sqe) {}
			if (conn != null)
				_pool.release(conn);
		}
	}
	
	public DatabaseConnectionPool getConnectionPool()
	{
		return _pool;
	}
}