package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

/**
 * definitions for the grid's active namespace configuration.
 * 
 * @author Chris Koeritz
 */

public class NamespaceDefinitions
{
	static private Log _logger = LogFactory.getLog(NamespaceDefinitions.class);

	static private final String NAMESPACE_PROPERTIES_FILE_NAME = "namespace.properties";

	private File _namespacePropertiesFile;
	private Properties _namespaceProperties;

	/**
	 * constructor wants the deployment root and the config directory within that root.
	 */
	NamespaceDefinitions(HierarchicalDirectory deploymentDirectory, HierarchicalDirectory configurationDirectory)
	{
		_namespacePropertiesFile = configurationDirectory.lookupFile(NAMESPACE_PROPERTIES_FILE_NAME);
		_namespaceProperties = new Properties();

		if (!_namespacePropertiesFile.exists())
			throw new InvalidDeploymentException(deploymentDirectory.getName(), "Couldn't find namespace properties file \""
				+ NAMESPACE_PROPERTIES_FILE_NAME + " in deployment's configuration directory.");

		FileInputStream fin = null;
		try {
			fin = new FileInputStream(_namespacePropertiesFile);
			_namespaceProperties.load(fin);
		} catch (IOException ioe) {
			_logger.fatal("Unable to load namespace properties from deployment.", ioe);
			throw new InvalidDeploymentException(deploymentDirectory.getName(),
				"Unable to load namespace properties from deployment.");
		} finally {
			StreamUtils.close(fin);
		}
	}

	/**
	 * general lookup for a property with no default.
	 */
	public String getProperty(String propertyName)
	{
		return getProperty(propertyName, null);
	}

	/**
	 * general lookup for properties that can give a default value if not found.
	 */
	public String getProperty(String propertyName, String def)
	{
		String toReturn = _namespaceProperties.getProperty(propertyName, def);
		if (_logger.isTraceEnabled())
			_logger.trace("found value=" + toReturn + " for property=" + propertyName);
		return toReturn;
	}

	/**
	 * the top-level folder where most containers can be located.
	 */
	public String getContainerDirectory()
	{
		return getProperty("edu.virginia.vcgr.genii.container.namespace.container-directory");
	}

	/**
	 * the specific location for the root, or Bootstrap, Container. this container may not always be
	 * located in the main containers directory.
	 */
	public String getRootContainer()
	{
		return getProperty("edu.virginia.vcgr.genii.container.namespace.bootstrap-container");
	}

	/**
	 * the top-level storage for most of the grid's users. this is used as the default when no
	 * specific path is provided.
	 */
	public String getUsersDirectory()
	{
		return getProperty("edu.virginia.vcgr.genii.container.namespace.users-directory");
	}

	/**
	 * the top-level storage for most of the grid user's home directories.
	 */
	public String getHomesDirectory()
	{
		return getProperty("edu.virginia.vcgr.genii.container.namespace.homes-directory");
	}
}
