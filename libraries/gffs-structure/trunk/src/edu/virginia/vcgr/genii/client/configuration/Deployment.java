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
	static private final String DYNAMIC_PAGES_DIRECTORY_NAME = "dynamic-pages";
	static private final String WEB_CONTAINER_PROPERTIES_FILENAME = "web-container.properties";

	static private Map<String, Deployment> _knownDeployments = new HashMap<String, Deployment>(4);

	private HierarchicalDirectory _deploymentDirectory;
	private HierarchicalDirectory _configurationDirectory;
	private Security _security;
	private HierarchicalDirectory _servicesDirectory;
	private HierarchicalDirectory _dynamicPagesDirectory;
	private Properties _webContainerProperties;
	private NamespaceDefinitions _namespace;

	private Deployment(File deploymentDirectory)
	{
		if (_logger.isDebugEnabled()) {
			_logger.debug("loading deployment from: " + deploymentDirectory);
		}

		_deploymentDirectory = HierarchicalDirectory.openRootHierarchicalDirectory(deploymentDirectory);

		_configurationDirectory = _deploymentDirectory.lookupDirectory(CONFIGURATION_DIRECTORY_NAME);

		if (!_configurationDirectory.exists())
			throw new InvalidDeploymentException(_deploymentDirectory.getName(),
				"Does not contain a " + CONFIGURATION_DIRECTORY_NAME + " directory.");

		_security = new Security(_deploymentDirectory, _configurationDirectory);
		if (!_security.loadedOkay()) {
			// signify that there was no security configuration.
			_security = null;
		}

		_namespace = new NamespaceDefinitions(_deploymentDirectory, _configurationDirectory);
		if (!_namespace.loadedOkay()) {
			// signify that there was no namespace definition.
			_namespace = null;
		}

		_servicesDirectory = _deploymentDirectory.lookupDirectory(SERVICES_DIRECTORY_NAME);

		if (!_servicesDirectory.exists())
			throw new InvalidDeploymentException(_deploymentDirectory.getName(),
				"Does not contain a " + SERVICES_DIRECTORY_NAME + " directory.");

		_dynamicPagesDirectory = _deploymentDirectory.lookupDirectory(DYNAMIC_PAGES_DIRECTORY_NAME);

		_webContainerProperties = loadWebContainerProperties(_deploymentDirectory.getName(), _configurationDirectory);
	}

	static private Properties loadWebContainerProperties(String deploymentName, HierarchicalDirectory configurationDirectory)
	{
		FileInputStream fin = null;
		Properties ret = new Properties();

		try {
			fin = new FileInputStream(configurationDirectory.lookupFile(WEB_CONTAINER_PROPERTIES_FILENAME));
			ret.load(fin);
			return ret;
		} catch (IOException ioe) {
			_logger.debug("Unable to load web container properties from deployment: " + deploymentName);
			// throw new InvalidDeploymentException(deploymentName, "Unable to load web container properties from deployment.");
			return null;
		} finally {
			StreamUtils.close(fin);
		}
	}

	public HierarchicalDirectory getDeploymentTop()
	{
		return _deploymentDirectory;
	}

	public HierarchicalDirectory getConfigurationDirectory()
	{
		return _configurationDirectory;
	}

	public File getConfigurationFile(String configurationFilename)
	{
		return _configurationDirectory.lookupFile(configurationFilename);
	}

	public Security security()
	{
		if (_security == null) {
			throw new RuntimeException("failed to access security definitions on this deployment");
		}
		return _security;
	}

	public NamespaceDefinitions namespace()
	{
		if (_namespace == null) {
			throw new RuntimeException("failed to access namespace definitions on this deployment");
		}
		return _namespace;
	}

	public HierarchicalDirectory getServicesDirectory()
	{
		return _servicesDirectory;
	}

	public HierarchicalDirectory getDynamicPagesDirectory()
	{
		return _dynamicPagesDirectory;
	}

	public Properties webContainerProperties()
	{
		if (_webContainerProperties == null) {
			throw new RuntimeException("failed to access web container properties on this deployment");
		}
		return _webContainerProperties;
	}

	public DeploymentName getName()
	{
		return new DeploymentName(_deploymentDirectory.getName());
	}

	static void reload()
	{
		synchronized (_knownDeployments) {
			_knownDeployments.clear();
		}
	}

	public static Deployment getDeployment(File deploymentsDirectory, DeploymentName deploymentName)
	{
		Deployment ret;

		String deploymentNameString = deploymentName.toString();

		synchronized (_knownDeployments) {
			ret = _knownDeployments.get(deploymentNameString);
			if (ret == null) {
				File dep = new File(deploymentsDirectory, deploymentNameString);
				if (!dep.exists())
					throw new NoSuchDeploymentException(deploymentNameString);
				if (!dep.isDirectory())
					throw new InvalidDeploymentException(deploymentNameString, "Not a directory");
				_knownDeployments.put(deploymentNameString, ret = new Deployment(dep));
			}
		}

		return ret;
	}
}
