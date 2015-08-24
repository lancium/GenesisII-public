package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.Installation;

public class ContainerProperties extends Properties
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(ContainerProperties.class);

	static final private String CONTAINER_PROPERTIES_FILENAME = "container.properties";

	// specifies which service types do not need to include X509 certificates in response EPRs.
	static final public String NO_X509_CLASS_LIST = "NO_X509_CLASS_LIST";

	// this is the single active copy of the object we hang onto per jvm.
	static private ContainerProperties _realContainerProperties = null;

	// tracks whether we successfully loaded from the properties file or not.
	// private boolean _existed = false;

	/* ... */

	/**
	 * for all normal run-time classes, the container properties is accessed this way.
	 */
	static public ContainerProperties getContainerProperties()
	{
		synchronized (ContainerProperties.class) {
			if (_realContainerProperties == null) {
				_realContainerProperties = new ContainerProperties();
			}
		}
		return _realContainerProperties;
	}

	/**
	 * reports if the container properties file was found or not.
	 */
	// public boolean existed()
	// {
	// return _existed;
	// }

	static private File getContainerPropertiesFile()
	{
		// first see if we can find our file in the state directory.
		File ret = new File(InstallationProperties.getUserDir(), CONTAINER_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;
		// if it wasn't there, try the install directory's lib folder.
		ret = new File(Installation.getInstallDirectory() + "/lib", CONTAINER_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;
		return null;
	}

	/* ... */

	/**
	 * returns the configured deployments directory, if one is defined. otherwise returns the default location.
	 */
	public String getDeploymentsDirectory()
	{
		// use the environment variable first.
		String toReturn = ApplicationBase.getDeploymentDirFromEnvironment();
		if (toReturn == null) {
			File checkLocalDir = new File(InstallationProperties.getUserDir(), "deployments");
			if (checkLocalDir.exists() && checkLocalDir.isDirectory()) {
				if (_logger.isTraceEnabled())
					_logger.trace("found deployments folder in state directory: " + checkLocalDir);
				toReturn = checkLocalDir.getAbsolutePath();
			}
		}
		if (toReturn == null)
			toReturn = getProperty(ClientProperties.GENII_DEPLOYMENT_DIRECTORY_PROPERTY_NAME);
		// well, nothing worked, so use a default based on the installation directory.
		if (toReturn == null)
			toReturn = new File(Installation.getInstallDirectory(), ClientProperties.DEPLOYMENTS_DIRECTORY_NAME).getAbsolutePath();
		if (_logger.isDebugEnabled())
			_logger.debug("deployments folder calculated as: '" + toReturn + "'");
		return toReturn;
	}

	public String getDeploymentName()
	{
		String toReturn =
			InstallationProperties.getInstallationProperties().getProperty(ClientProperties.GENII_DEPLOYMENT_NAME_PROPERTY_NAME);
		if (toReturn == null)
			toReturn = getProperty(ClientProperties.GENII_DEPLOYMENT_NAME_PROPERTY_NAME);
		return toReturn;
	}

	/* ... */

	/**
	 * returns the "no x509" list of service types that should not be adding x509 certs to their EPR responses. this list cannot include
	 * random byte io if replication is to work, since delegation is needed during replication actions.
	 */
	public String getEPRConstructionProperties()
	{
		String toReturn = InstallationProperties.getInstallationProperties().getProperty(NO_X509_CLASS_LIST);
		if (toReturn == null)
			toReturn = getProperty(NO_X509_CLASS_LIST);
		return toReturn;
	}

	/* ... */

	/**
	 * This is not to be used in general; normally the getContainerProperties() method should be used. There should be exactly two uses of
	 * this method, one here in getContainerProperties() and one in the Installation class (in ...genii.client.configuration package).
	 */
	public ContainerProperties()
	{
		File file = getContainerPropertiesFile();
		if (file != null) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				load(in);
				// _existed = true;
			} catch (IOException e) {
				return;
			} finally {
				StreamUtils.close(in);
			}
		}
	}

}