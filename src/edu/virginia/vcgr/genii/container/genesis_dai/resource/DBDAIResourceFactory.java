package edu.virginia.vcgr.genii.container.genesis_dai.resource;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceFactory;

public class DBDAIResourceFactory extends RNSDBResourceFactory{
	
	static private Log _logger = LogFactory.getLog(DBDAIResourceFactory.class);
	
	static private final String _CREATE_DAIR_ENTRY_TABLE_STMT =
		"CREATE TABLE dair " +
		"(id INTEGER GENERATED BY DEFAULT AS IDENTITY, " +
		"servicekey VARCHAR (128), " +
		"servicename VARCHAR (128), " +
		"serviceEPR BLOB (128K), " +
		"resourcename VARCHAR (256), " +
		"resourceEPR BLOB (128K), " +
		"CONSTRAINT dair PRIMARY KEY (id))";

	public DBDAIResourceFactory(DatabaseConnectionPool pool,
			IResourceKeyTranslater translator) 
		throws SQLException 
	{
		super(pool, translator);
	}

	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new DBDAIResource(parentKey, _pool, _translater);
		}
		catch (SQLException sqe)
		{
			_logger.warn(sqe.getLocalizedMessage(), sqe);
			throw new ResourceException(sqe.getLocalizedMessage(), sqe);
		}
	}

	public DatabaseConnectionPool getConnectionPool()
	{
		return _pool;
	}
	
	protected void createTables() throws SQLException
	{
		Connection conn = null;
		super.createTables();
		 
		try
		{
			conn = _pool.acquire();
			DatabaseTableUtils.createTables(conn, false, 
				_CREATE_DAIR_ENTRY_TABLE_STMT);
			conn.commit();
			 
		}
		finally
		{
			_pool.release(conn);
		}		
	}
}