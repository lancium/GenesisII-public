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
	
	static private final String CONFIGURATION_DIRECTORY_NAME = "configuration";
	static private final String SERVICES_DIRECTORY_NAME = "services";
	static private final String URI_PROPERTIES_FILENAME =
		"uri-manager.properties";
	static private final String WEB_CONTAINER_PROPERTIES_FILENAME = 
		"web-container.properties";
	static private final String REJUVENATION_PROPERTYIES_FILENAME =
		"rejuvenation.properties";
	static private final String SECURE_RUNNABLE_DIRECTORY_NAME = 
		"secure-runnable";
	
	static private Map<String, Deployment> _knownDeployments =
		new HashMap<String, Deployment>(4);
	
	private File _deploymentDirectory;
	private File _configurationDirectory;
	private File _secureRunnableDirectory;
	private Security _security;
	private File _servicesDirectory;
	private Properties _webContainerProperties;
	private Properties _uriManagerProperties;
	private Properties _rejuvenationProperties;
	
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
		_uriManagerProperties = loadURIManagerProperties(
			_deploymentDirectory.getName(), _configurationDirectory);
		_rejuvenationProperties = loadRejuvenationProperties(
			_deploymentDirectory.getName(), _configurationDirectory);
		
		_secureRunnableDirectory = new File(_deploymentDirectory, 
			SECURE_RUNNABLE_DIRECTORY_NAME);
	}
	
	static private Properties loadURIManagerProperties(
		String deploymentName, File configurationDirectory)
	{
		FileInputStream fin = null;
		Properties ret = new Properties();
		
		try
		{
			fin = new FileInputStream(new File(configurationDirectory, 
				URI_PROPERTIES_FILENAME));
			ret.load(fin);
			return ret;
		}
		catch (IOException ioe)
		{
			_logger.debug("Unable to load uri manager properties from deployment.", ioe);
			return new Properties();
		}
		finally
		{
			StreamUtils.close(fin);
		}
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
	
	static private Properties loadRejuvenationProperties(
		String deploymentName, File configurationDirectory)
	{
		FileInputStream fin = null;
		Properties ret = new Properties();
		
		try
		{
			fin = new FileInputStream(new File(configurationDirectory, 
				REJUVENATION_PROPERTYIES_FILENAME));
			ret.load(fin);
			return ret;
		}
		catch (IOException ioe)
		{
			_logger.debug(
				"Unable to load software rejuvenation information.  " +
				"Assuming there isn't any.", ioe);
			return new Properties();
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	public File secureRunnableDirectory()
	{
		return _secureRunnableDirectory;
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
	
	public Properties uriManagerProperties()
	{
		return _uriManagerProperties;
	}
	
	public Properties webContainerProperties()
	{
		return _webContainerProperties;
	}
	
	public Properties softwareRejuvenationProperties()
	{
		return _rejuvenationProperties;
	}
	
	public DeploymentName getName()
	{
		return new DeploymentName(_deploymentDirectory.getName());
	}
	
	static void reload()
	{
		synchronized(_knownDeployments)
		{
			_knownDeployments.clear();
		}
	}
	
	static Deployment getDeployment(File deploymentsDirectory,
		DeploymentName deploymentName)
	{
		Deployment ret;
		
		String deploymentNameString = deploymentName.toString();
		
		synchronized(_knownDeployments)
		{
			ret = _knownDeployments.get(deploymentNameString);
			if (ret == null)
			{
				File dep = new File(deploymentsDirectory, deploymentNameString);
				if (!dep.exists())
					throw new NoSuchDeploymentException(deploymentNameString);
				if (!dep.isDirectory())
					throw new InvalidDeploymentException(deploymentNameString, 
						"Not a directory");
				_knownDeployments.put(deploymentNameString,
					ret = new Deployment(dep));
			}
		}
		
		return ret;
	}
}
