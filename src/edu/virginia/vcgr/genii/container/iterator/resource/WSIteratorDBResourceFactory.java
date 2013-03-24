package edu.virginia.vcgr.genii.container.iterator.resource;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;

public class WSIteratorDBResourceFactory extends BasicDBResourceFactory
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(WSIteratorDBResourceFactory.class);

	static private final String[] _CREATE_STMTS = new String[] { "CREATE TABLE iterators ("
		+ "entryid BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " + "iteratorid VARCHAR(256) NOT NULL, "
		+ "elementindex BIGINT NOT NULL, " + "contents BLOB(2G) NOT NULL, "
		+ "CONSTRAINT iteratorsuniqueconstraint UNIQUE (iteratorid, elementindex))" };

	public WSIteratorDBResourceFactory(DatabaseConnectionPool pool) throws SQLException
	{
		super(pool);
	}

	@Override
	public WSIteratorResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try {
			return new WSIteratorDBResource(parentKey, _pool);
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
			DatabaseTableUtils.createTables(conn, false, _CREATE_STMTS);
			conn.commit();
		} finally {
			_pool.release(conn);
		}
	}

	public DatabaseConnectionPool getConnectionPool()
	{
		return _pool;
	}
}