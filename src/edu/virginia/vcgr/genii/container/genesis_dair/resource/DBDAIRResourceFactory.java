package edu.virginia.vcgr.genii.container.genesis_dair.resource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
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
		"CONSTRAINT dataresourcesC1 UNIQUE (resourcename))";
	
		
	public DBDAIRResourceFactory(DatabaseConnectionPool connectionPool,
			IResourceKeyTranslater translator)
			throws SQLException {
		super(connectionPool, translator);
	}
	
	public IResource instantiate(ResourceKey parentKey) throws ResourceException
	{
		try
		{
			return new DBDAIRResource(parentKey, _pool, _translater);
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
		Statement stmt = null;
		super.createTables();
		 
		try
		{
			 conn = _pool.acquire();
			 stmt = conn.createStatement();
			 
			 stmt.executeUpdate(_CREATE_DATARESOURCE_ENTRY_TABLE_STMT);
			 conn.commit();
			 
		}
		catch (SQLException sqe)
		{
			 // assume the table already exists.
		}
		finally
		{
			 if (stmt != null)
				 try {stmt.close(); } 
			 catch(SQLException sqe) {}
				 
			 if (conn != null)
				 _pool.release(conn);
		}		
	}

}
