package edu.virginia.vcgr.genii.container.genesis_dair.resource;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.db.DatabaseTableUtils;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rns.RNSDBResourceFactory;

public class DBDAIRResourceFactory extends RNSDBResourceFactory {
	
	static private Log _logger = LogFactory.getLog(DBDAIRResourceFactory.class);
	
	static private final String _CREATE_DATARESOURCE_ENTRY_TABLE_STMT =
		"CREATE TABLE dataresources " +
		"(resourceid INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY, " +
		"servicekey VARCHAR (128) NOT NULL, " +
		"serviceEPR BLOB (128K) NOT NULL, " +
		"resourcename VARCHAR (128) NOT NULL, " +
		"resourceEPR BLOB (128K) NOT NULL, " +
		"query VARCHAR (512), " +
		"CONSTRAINT dataresourcesC1 UNIQUE (resourcename))";
		
	public DBDAIRResourceFactory(DatabaseConnectionPool connectionPool)
		throws SQLException 
	{
		super(connectionPool);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new DBDAIRResource(parentKey, _pool);
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
				_CREATE_DATARESOURCE_ENTRY_TABLE_STMT);
			conn.commit(); 
		}
		finally
		{
			_pool.release(conn);
		}		
	}

}
