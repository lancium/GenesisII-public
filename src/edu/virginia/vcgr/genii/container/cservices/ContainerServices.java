package edu.virginia.vcgr.genii.container.cservices;

import java.io.File;
import java.io.IOException;
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
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamedInstances;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.cservices.percall.PersistentOutcallContainerService;
import edu.virginia.vcgr.genii.container.cservices.ver1.Version1Upgrader;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class ContainerServices
{
	static private Log _logger = LogFactory.getLog(ContainerServices.class);
	
	static final private String _CONTAINER_SERVICES_FILENAME = 
		"container-services.xml";
	
	static private Map<String, ContainerService> _services = null;
	
	static private Collection<ContainerService> getServices(File configFile)
		throws IOException
	{
		Version1Upgrader.upgrade(configFile, new File(configFile.getParentFile(), "cservices"));
		
		Collection<ContainerService> services = 
			ContainerServicesParser.parseConfigFile(configFile);
		
		// This is a hack for now -- add in services that are always there.
		services.add(new PersistentOutcallContainerService());
		services.add(new AccountingService());
		
		return services;
	}
	
	static private DatabaseConnectionPool findConnectionPool()
	{
		DatabaseConnectionPool ret = 
			(DatabaseConnectionPool)NamedInstances.getServerInstances(
				).lookup("connection-pool");
		if (ret == null)
			throw new ConfigurationException(
				"Unable to find database connection pool.");
		
		return ret;
	}
	
	static public ContainerService findService(String serviceName)
	{
		ContainerService ret = _services.get(serviceName);
		if (ret == null)
			throw new NoSuchServiceException(serviceName);
		
		return ret;
	}
	
	synchronized static public void loadAll()
	{
		if (_services != null)
			throw new ConfigurationException("Container Services already loaded.");
	
		ExecutorService executor = Executors.newFixedThreadPool(8);
		DatabaseConnectionPool connectionPool = findConnectionPool();
		ContainerServicesProperties properties = new ContainerServicesProperties(
			connectionPool);
		
		_services = new HashMap<String, ContainerService>();
	
		Collection<String> toRemove = new LinkedList<String>();
		
		Deployment deployment = Installation.getDeployment(
			new DeploymentName());
		File configFile = deployment.getConfigurationFile(
			_CONTAINER_SERVICES_FILENAME);
		if (configFile != null && configFile.exists())
		{
			try
			{
				Collection<ContainerService> services = getServices(configFile);
				if (services != null)
				{
					for (ContainerService service : services)
					{
						_services.put(service.serviceName(), service);
						try
						{
							service.load(executor, connectionPool, properties);
						}
						catch (Throwable cause)
						{
							_logger.error(String.format(
								"Unable to load service \"%s\".", 
								service.serviceName()), cause);
							toRemove.add(service.serviceName());
						}
					}
					
					for (String remove : toRemove)
						_services.remove(remove);
				}
			}
			catch (IOException ioe)
			{
				_logger.error("Unable to load any container services.", ioe);
			}
		}
	}
	
	synchronized static public void startAll()
	{
		Collection<String> toRemove = new LinkedList<String>();
		
		for (ContainerService service : _services.values())
		{
			if (service.started())
			{
				_logger.warn("Service \"" + service.serviceName() 
					+ "\" is already started.");
				continue;
			}
			
			try
			{
				service.start();
			}
			catch (Throwable cause)
			{
				_logger.error(String.format("Unable to start service \"%s\".",
					service.serviceName()), cause);
				toRemove.add(service.serviceName());
			}
		}
		
		for (String remove : toRemove)
			_services.remove(remove);
	}
}