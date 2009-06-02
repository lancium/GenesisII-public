package edu.virginia.vcgr.genii.container.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;

public class DatabaseConnectionManager
{
	static private Log _logger = LogFactory.getLog(
		DatabaseConnectionManager.class);
	
	static private class ConnectionDescription
	{
		private DatabaseConnectionPool _pool;
		private Connection _connection;
		
		public ConnectionDescription(DatabaseConnectionPool connectionPool,
			Connection databaseConnection)
		{
			_pool = connectionPool;
			_connection = databaseConnection;
		}
		
		public DatabaseConnectionPool getConnectionPool()
		{
			return _pool;
		}
		
		public Connection getConnection()
		{
			return _connection;
		}
	}
	
	static private class ConnectionStorage 
		extends ThreadLocal<Collection<ConnectionDescription>>
	{
		protected Collection<ConnectionDescription> initialValue()
		{
			return new ArrayList<ConnectionDescription>();
		}
	}
	
	static private ConnectionStorage _storage =
		new ConnectionStorage();
	
	static private HashMap<String, DatabaseConnectionPool> _pools =
		new HashMap<String, DatabaseConnectionPool>();
	
	static public Connection acquireConnection(String connectionPoolName, 
		boolean useAutoCommit) throws SQLException
	{
		DatabaseConnectionPool pool;
		synchronized (_pools)
		{
			pool = _pools.get(connectionPoolName);
			if (pool == null)
				_pools.put(connectionPoolName, 
					(pool = findConnectionPool(connectionPoolName)));
		}
		
		Connection conn = pool.acquire(useAutoCommit);
		_storage.get().add(new ConnectionDescription(pool, conn));
		return conn;
	}
	
	static public void releaseAllConnections(boolean success)
	{
		for (ConnectionDescription desc : _storage.get())
		{
			Connection conn = desc.getConnection();
			DatabaseConnectionPool pool = desc.getConnectionPool();
			
			try
			{
				if (success)
					conn.commit();
				else
					conn.rollback();
			}
			catch (Throwable t)
			{
				_logger.error(
					"Problem committing or rolling back the database.", t);
			}
			finally
			{
				pool.release(conn);
			}
		}
	}
	
	static private DatabaseConnectionPool findConnectionPool(String poolName)
	{
		DatabaseConnectionPool pool = null;
		Object obj = NamedInstances.getServerInstances().lookup(poolName);
		if (obj != null)
		{
			pool = (DatabaseConnectionPool)obj;
			return pool;
		}
		
		throw new ConfigurationException("Couldn't find connection pool \"" +
			poolName + "\".");
	}
}