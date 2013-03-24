package edu.virginia.vcgr.genii.container.resource.db;

import java.sql.SQLException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.Initializable;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResourceFactory;
import edu.virginia.vcgr.genii.container.resource.IResourceProvider;

public class BasicDBResourceProvider implements IResourceProvider, Initializable
{
	private IResourceFactory _factory = null;

	public BasicDBResourceProvider()
	{
	}

	private DatabaseConnectionPool createConnectionPool()

	{
		DatabaseConnectionPool pool = null;

		Object obj = NamedInstances.getServerInstances().lookup("connection-pool");
		if (obj != null) {
			pool = (DatabaseConnectionPool) obj;
			return pool;
		}

		throw new ConfigurationException("Couldn't find connection pool.");
	}

	synchronized public IResourceFactory getFactory()
	{
		if (_factory == null) {
			try {
				_factory = instantiateResourceFactory(createConnectionPool());
			} catch (Exception e) {
				throw new RuntimeException(e.getLocalizedMessage(), e);
			}
		}

		return _factory;
	}

	protected IResourceFactory instantiateResourceFactory(DatabaseConnectionPool pool) throws SQLException, ResourceException
	{
		return new BasicDBResourceFactory(pool);
	}

	@Override
	public void initialize() throws Throwable
	{
		getFactory();
	}
}