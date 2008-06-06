package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Deployment
{
	static public final String DEPLOYMENT_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.client.deployment-name";
	static public final String DEPLOYMENT_NAME_ENVIRONMENT_VARIABLE =
		"GENII_DEPLOYMENT_NAME";
	
	static private final String DEFAULT_DEPLOYMENT_NAME = "default";
	static private final String CONFIGURATION_DIRECTORY_NAME = "configuration";
	static private final String SECURITY_DIRECTORY_NAME = "security";
	static private final String SERVICES_DIRECTORY_NAME = "services";
	
	static private Map<String, Deployment> _knownDeployments =
		new HashMap<String, Deployment>(4);
	
	private File _deploymentDirectory;
	private File _configurationDirectory;
	private File _securityDirectory;
	private File _servicesDirectory;
	
	private Deployment(File deploymentDirectory)
	{
		_deploymentDirectory = deploymentDirectory;
		_configurationDirectory = new File(_deploymentDirectory, 
			CONFIGURATION_DIRECTORY_NAME);
		if (!_configurationDirectory.exists())
			throw new InvalidDeploymentException(_deploymentDirectory.getName(),
				"Does not contain a " +
				CONFIGURATION_DIRECTORY_NAME + " directory.");
		
		_securityDirectory = new File(_deploymentDirectory, 
			SECURITY_DIRECTORY_NAME);
		if (!_securityDirectory.exists())
			throw new InvalidDeploymentException(_deploymentDirectory.getName(),
				"Does not contain a " +
				SECURITY_DIRECTORY_NAME + " directory");
		
		_servicesDirectory = new File(_deploymentDirectory,
			SERVICES_DIRECTORY_NAME);
		if (!_servicesDirectory.exists())
			throw new InvalidDeploymentException(_deploymentDirectory.getName(),
				"Does not contain a " +
				SERVICES_DIRECTORY_NAME + " directory.");
	}
	
	public File getConfigurationFile(String configurationFilename)
	{
		return new File(_configurationDirectory, configurationFilename);
	}
	
	public File getSecurityFile(String securityFilename)
	{
		return new File(_securityDirectory, securityFilename);
	}
	
	public File getServicesDirectory()
	{
		return _servicesDirectory;
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