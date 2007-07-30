package edu.virginia.vcgr.genii.container.resource.db;

import java.sql.SQLException;
import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.IResourceKeyTranslater;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;
import edu.virginia.vcgr.genii.container.resource.StringResourceKeyTranslater;

public class BasicDBResourceProvider implements IResourceProvider
{
	static private final String _CONNECTION_POOL_NAME = 
		"edu.virginia.vcgr.genii.db.connection-pool";
	
	private String _connectionPoolName = null;
	private IResourceFactory _factory = null;
	private IResourceKeyTranslater _translater;
	
	@SuppressWarnings("unchecked")
	public BasicDBResourceProvider(Properties properties)
		throws SQLException, ConfigurationException
	{	
		_translater = instantiateTranslater();
		_connectionPoolName = properties.getProperty(
			_CONNECTION_POOL_NAME);
		if (_connectionPoolName == null)
			throw new ConfigurationException(
				"Can't create a BasicDBResourceProvider without a connection pool name (\"" 
					+ _CONNECTION_POOL_NAME + "\".");
	}
	
	@SuppressWarnings("unchecked")
	private DatabaseConnectionPool createConnectionPool()
		throws ConfigurationException
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

	public IResourceKeyTranslater getTranslater()
	{
		return _translater;
	}
	
	protected IResourceKeyTranslater instantiateTranslater()
	{
		return new StringResourceKeyTranslater();
	}
	
	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool)
		throws SQLException, ResourceException
	{
		return new BasicDBResourceFactory(pool);
	}
}