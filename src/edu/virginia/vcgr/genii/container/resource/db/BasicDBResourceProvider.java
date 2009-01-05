package edu.virginia.vcgr.genii.container.resource.db;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;

public class BasicDBResourceProvider implements IResourceProvider
{
	static private final String _CONNECTION_POOL_NAME = 
		"edu.virginia.vcgr.genii.db.connection-pool";
	
	private String _connectionPoolName = null;
	private IResourceFactory _factory = null;
	
	public BasicDBResourceProvider(Properties properties)
		throws SQLException
	{	
		_connectionPoolName = properties.getProperty(
			_CONNECTION_POOL_NAME);
		if (_connectionPoolName == null)
			throw new ConfigurationException(
				"Can't create a BasicDBResourceProvider without a connection pool name (\"" 
					+ _CONNECTION_POOL_NAME + "\".");
	}
	
	private DatabaseConnectionPool createConnectionPool()

	{
		DatabaseConnectionPool pool = null;
		
		Object obj = NamedInstances.getServerInstances().lookup(_connectionPoolName);
		if (obj != null)
		{
			pool = (DatabaseConnectionPool)obj;
			return pool;
		}
		
		throw new ConfigurationException("Couldn't find connection pool \"" +
			_connectionPoolName + "\".");
	}
	
	synchronized public IResourceFactory getFactory()
	{
		if (_factory == null)
		{
			try
			{
				_factory = instantiateResourceFactory(createConnectionPool());
			}
			catch (Exception e)
			{
				throw new RuntimeException(e.getLocalizedMessage(), e);
			}
		}
		
		return _factory;
	}

	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException, ResourceException
	{
		return new BasicDBResourceFactory(pool);
	}
}