package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class Deployment
{
	static private Log _logger = LogFactory.getLog(Deployment.class);
	
	static public final String DEPLOYMENT_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.client.deployment-name";
	static public final String DEPLOYMENT_NAME_ENVIRONMENT_VARIABLE =
		"GENII_DEPLOYMENT_NAME";
	
	static private final String DEFAULT_DEPLOYMENT_NAME = "default";
	static private final String CONFIGURATION_DIRECTORY_NAME = "configuration";
	static private final String SERVICES_DIRECTORY_NAME = "services";
	static private final String WEB_CONTAINER_PROPERTIES_FILENAME = 
		"web-container.properties";
	
	static private Map<String, Deployment> _knownDeployments =
		new HashMap<String, Deployment>(4);
	
	private File _deploymentDirectory;
	private File _configurationDirectory;
	private Security _security;
	private File _servicesDirectory;
	private Properties _webContainerProperties;
	
	private Deployment(File deploymentDirectory)
	{
		_deploymentDirectory = deploymentDirectory;
		_configurationDirectory = new File(_deploymentDirectory, 
			CONFIGURATION_DIRECTORY_NAME);
		if (!_configurationDirectory.exists())
			throw new InvalidDeploymentException(_deploymentDirectory.getName(),
				"Does not contain a " +
				CONFIGURATION_DIRECTORY_NAME + " directory.");
		
		_security = new Security(_deploymentDirectory,
			_configurationDirectory);
		
		_servicesDirectory = new File(_deploymentDirectory,
			SERVICES_DIRECTORY_NAME);
		if (!_servicesDirectory.exists())
			throw new InvalidDeploymentException(_deploymentDirectory.getName(),
				"Does not contain a " +
				SERVICES_DIRECTORY_NAME + " directory.");
		
		_webContainerProperties = loadWebContainerProperties(
			_deploymentDirectory.getName(), _configurationDirectory);
	}
	
	static private Properties loadWebContainerProperties(
		String deploymentName, File configurationDirectory)
	{
		FileInputStream fin = null;
		Properties ret = new Properties();
		
		try
		{
			fin = new FileInputStream(new File(configurationDirectory, 
				WEB_CONTAINER_PROPERTIES_FILENAME));
			ret.load(fin);
			return ret;
		}
		catch (IOException ioe)
		{
			_logger.fatal("Unable to load web container properties from deployment.", ioe);
			throw new InvalidDeploymentException(deploymentName,
				"Unable to load web container properties from deployment.");
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	public File getConfigurationFile(String configurationFilename)
	{
		return new File(_configurationDirectory, configurationFilename);
	}
	
	public Security security()
	{
		return _security;
	}
	
	public File getServicesDirectory()
	{
		return _servicesDirectory;
	}
	
	public Properties webContainerProperties()
	{
		return _webContainerProperties;
	}
	
	public String getName()
	{
		return _deploymentDirectory.getName();
	}
	
	static void reload()
	{
		synchronized(_knownDeployments)
		{
			_knownDeployments.clear();
		}
	}
	
	static Deployment getDeployment(File deploymentsDirectory,
		String deploymentName)
	{
		Deployment ret;
		if (deploymentName == null)
			deploymentName = figureOutDefaultDeploymentName();
		
		synchronized(_knownDeployments)
		{
			ret = _knownDeployments.get(deploymentName);
			if (ret == null)
			{
				File dep = new File(deploymentsDirectory, deploymentName);
				if (!dep.exists())
					throw new NoSuchDeploymentException(deploymentName);
				if (!dep.isDirectory())
					throw new InvalidDeploymentException(deploymentName, 
						"Not a directory");
				_knownDeployments.put(deploymentName,
					ret = new Deployment(dep));
			}
		}
		
		return ret;
	}
	
	static private String figureOutDefaultDeploymentName()
	{
		String deploymentName = System.getProperty(DEPLOYMENT_NAME_PROPERTY);
		
		// jfk3w - added user config information - including user's deployment path
		if (deploymentName == null)
			deploymentName = System.getenv(DEPLOYMENT_NAME_ENVIRONMENT_VARIABLE);
		
		// if all else fails, try "default"
		if (deploymentName == null)
			deploymentName = DEFAULT_DEPLOYMENT_NAME;
		
		return deploymentName;
	}
}