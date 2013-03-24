package edu.virginia.vcgr.genii.container.cservices;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public abstract class AbstractContainerService implements ContainerService
{
	private boolean _started = false;
	private String _serviceName;
	private ExecutorService _executor = null;
	private DatabaseConnectionPool _connectionPool = null;
	private ContainerServicesProperties _cservicesProperties = null;

	protected AbstractContainerService(String serviceName)
	{
		_serviceName = serviceName;
	}

	protected ExecutorService getExecutor()
	{
		return _executor;
	}

	protected DatabaseConnectionPool getConnectionPool()
	{
		return _connectionPool;
	}

	protected abstract void startService() throws Throwable;

	protected abstract void loadService() throws Throwable;

	@Override
	final synchronized public void load(ExecutorService executor, DatabaseConnectionPool connectionPool,
		ContainerServicesProperties cservicesProperties) throws Throwable
	{
		_executor = executor;
		_connectionPool = connectionPool;
		_cservicesProperties = cservicesProperties;

		loadService();
	}

	@Override
	public void setProperties(Properties properties)
	{
		// By default, we ignore properties.
	}

	@Override
	public String serviceName()
	{
		return _serviceName;
	}

	@Override
	final synchronized public void start() throws Throwable
	{
		if (_connectionPool == null)
			throw new ConfigurationException("Attempt to start service \"" + _serviceName
				+ "\" without datbase connection pool.");
		if (_started)
			throw new ConfigurationException("Service \"" + _serviceName + "\" has already been started.");

		startService();
	}

	@Override
	public boolean started()
	{
		return _started;
	}

	@Override
	public ContainerServicesProperties getContainerServicesProperties()
	{
		return _cservicesProperties;
	}
}