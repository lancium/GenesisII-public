package edu.virginia.vcgr.genii.container.cservices;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.HierarchicalDirectory;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.container.cservices.conf.ContainerServiceConfiguration;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class ContainerServices
{
	static private Log _logger = LogFactory.getLog(ContainerServices.class);

	static private Map<Class<? extends ContainerService>, ContainerService> _services = null;

	static private Collection<ContainerService> getServices(HierarchicalDirectory configDirectory) throws IOException
	{
		HierarchicalDirectory configurationDirectory = configDirectory.lookupDirectory("cservices");
		Collection<ContainerServiceConfiguration> configs = ContainerServiceConfiguration
			.loadConfigurations(configurationDirectory);
		Collection<ContainerService> ret = new ArrayList<ContainerService>(configs.size());
		Class<? extends ContainerService> serviceClass = null;

		for (ContainerServiceConfiguration configuration : configs) {
			try {
				serviceClass = configuration.serviceClass();
				ret.add(configuration.instantiate());
			} catch (InvocationTargetException e) {
				_logger.error(
					String.format("Error loading container service %s from file %s.", serviceClass,
						configuration.configurationFile()), e.getCause());
			} catch (Throwable cause) {
				_logger.error(
					String.format("Error loading container service %s from file %s.", serviceClass,
						configuration.configurationFile()), cause);
			}
		}

		return ret;
	}

	static private DatabaseConnectionPool findConnectionPool()
	{
		DatabaseConnectionPool ret = (DatabaseConnectionPool) NamedInstances.getServerInstances().lookup("connection-pool");
		if (ret == null)
			throw new ConfigurationException("Unable to find database connection pool.");

		return ret;
	}

	static public <Type extends ContainerService> Type findService(Class<Type> serviceType)
	{
		Type ret = serviceType.cast(_services.get(serviceType));
		if (ret == null)
			throw new NoSuchServiceException(serviceType.toString());

		return ret;
	}

	static public boolean hasService(Class<?> serviceType)
	{
		return _services.containsKey(serviceType);
	}

	synchronized static public void loadAll()
	{
		if (_services != null)
			throw new ConfigurationException("Container Services already loaded.");

		ExecutorService executor = Executors.newFixedThreadPool(8);
		DatabaseConnectionPool connectionPool = findConnectionPool();
		ContainerServicesProperties properties = new ContainerServicesProperties(connectionPool);

		_services = new HashMap<Class<? extends ContainerService>, ContainerService>();

		Collection<String> toRemove = new LinkedList<String>();

		Deployment deployment = Installation.getDeployment(new DeploymentName());
		try {
			Collection<ContainerService> services = getServices(deployment.getConfigurationDirectory());
			if (services != null) {
				for (ContainerService service : services) {
					_services.put(service.getClass(), service);
					try {
						service.load(executor, connectionPool, properties);
					} catch (Throwable cause) {
						_logger.error(String.format("Unable to load service \"%s\".", service.serviceName()), cause);
						toRemove.add(service.serviceName());
					}
				}

				for (String remove : toRemove)
					_services.remove(remove);
			}
		} catch (IOException ioe) {
			_logger.error("Unable to load any container services.", ioe);
		}
	}

	synchronized static public void startAll()
	{
		Collection<String> toRemove = new LinkedList<String>();

		for (ContainerService service : _services.values()) {
			if (service.started()) {
				_logger.warn("Service \"" + service.serviceName() + "\" is already started.");
				continue;
			}

			try {
				service.start();
			} catch (Throwable cause) {
				_logger.error(String.format("Unable to start service \"%s\".", service.serviceName()), cause);
				toRemove.add(service.serviceName());
			}
		}

		for (String remove : toRemove)
			_services.remove(remove);
	}
}
