package edu.virginia.vcgr.genii.container.rns;

import java.sql.Connection;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class RNSDBResourceFactory extends BasicDBResourceFactory implements
		IResourceFactory
{
	static private final String _CREATE_ENTRY_TABLE_STMT =
		"CREATE TABLE entries (resourceid VARCHAR(128), name VARCHAR(256), " +
		"endpoint BLOB(2G), id VARCHAR(40), " +
		"attrs VARCHAR (8192) FOR BIT DATA, " +
		"CONSTRAINT contextsconstraint1 PRIMARY KEY (resourceid, name))";
	static private String _CREATE_ENTRY_INDEX_STMT = "CREATE INDEX idx ON entries (id)";
	
	
	
	public RNSDBResourceFactory(
			DatabaseConnectionPool pool)
		throws SQLException
	{
		super(pool);
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
		super.createTables();
		
		try
		{
			conn = _pool.acquire(false);
			DatabaseTableUtils.createTables(conn, false, 
				_CREATE_ENTRY_TABLE_STMT);
			DatabaseTableUtils.createTables(conn, false, 
					_CREATE_ENTRY_INDEX_STMT );
			conn.commit();
		}
		finally
		{
			_pool.release(conn);
		}
	}
}