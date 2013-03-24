package edu.virginia.vcgr.genii.container.resolver;

import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

import java.sql.Connection;

public class GeniiResolverDBResourceFactory extends BasicDBResourceFactory implements IResourceFactory
{
	static private final String _CREATE_TABLE_STMT = "CREATE TABLE resolverentries (resourceid VARCHAR(128), "
		+ "epi VARCHAR(512), targetid INTEGER, endpoint BLOB(2G), " + "PRIMARY KEY (resourceid, epi, targetid))";

	public GeniiResolverDBResourceFactory(DatabaseConnectionPool pool) throws SQLException
	{
		super(pool);
	}

	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try {
			return new GeniiResolverDBResource(parentKey, _pool);
		} catch (SQLException sqe) {
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}

	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();
		try {
			conn = _pool.acquire(false);
			DatabaseTableUtils.createTables(conn, false, _CREATE_TABLE_STMT);
			conn.commit();
		} finally {
			_pool.release(conn);
		}
	}
}
