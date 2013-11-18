package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.HierarchicalDirectory;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.KeystoreSecurityConstants;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.security.axis.AclAuthZClientTool;
import edu.virginia.vcgr.genii.security.identity.Identity;

public class InstallationProperties extends Properties
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(InstallationProperties.class);
	// our singular instance of this class.
	static private InstallationProperties _realInstallationProperties = null;

	/**
	 * for all normal run-time classes, the installation properties are accessed this way.
	 */
	static public InstallationProperties getInstallationProperties()
	{
		synchronized (InstallationProperties.class) {
			if (_realInstallationProperties == null) {
				_realInstallationProperties = new InstallationProperties();
			}
		}
		return _realInstallationProperties;
	}

	public InstallationProperties()
	{
		File file = getInstallationPropertiesFile();
		if (file != null) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				load(in);
				_logger.debug("successfully loaded installation properties.");
			} catch (IOException e) {
				_logger.debug("failed to load installation properties.");
				return;
			} finally {
				StreamUtils.close(in);
			}
		}
	}

	static private File getInstallationPropertiesFile()
	{
		File ret = new File(getUserDir(), InstallationConstants.INSTALLATION_PROPERTIES_FILENAME);
		// ContainerProperties.getContainerProperties().getUserDirectory(),
		// INSTALLATION_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;
		return null;
	}

	public String getTLSKeyPassword()
	{
		return getProperty(InstallationConstants.TLS_KEY_PASSWORD_PROPERTY);
	}

	public String getTLSKeystorePassword()
	{
		return getProperty(InstallationConstants.TLS_KEYSTORE_PASSWORD_PROPERTY);
	}

	// this member is an absolute path; do not try to use hierarchical deployment calls on it.
	public String getTLSKeystoreFile()
	{
		return getProperty(InstallationConstants.TLS_KEYSTORE_FILE_PROPERTY);
	}

	public String getTLSKeystoreType()
	{
		return getProperty(InstallationConstants.TLS_KEYSTORE_TYPE_PROPERTY);
	}

	public String getSigningKeystoreFile()
	{
		String keystoreLoc = getProperty(InstallationConstants.SIGNING_KEYSTORE_FILE_PROPERTY);
		if (keystoreLoc == null) {
			// we didn't have the local installation properties, so fall back to old-school methods.
			Security resourceIdSecProps = Installation.getDeployment(new DeploymentName()).security();
			String keyProp =
				resourceIdSecProps.getProperty(KeystoreSecurityConstants.Container.RESOURCE_IDENTITY_KEY_STORE_PROP);
			keystoreLoc =
				Installation.getDeployment(new DeploymentName()).security().getSecurityFile(keyProp).getAbsolutePath();
		}
		return keystoreLoc;
	}

	public String getContainerHostname()
	{
		return getProperty(InstallationConstants.CONTAINER_HOSTNAME_PROPERTY);
	}

	public String getContainerPort()
	{
		return getProperty(InstallationConstants.CONTAINER_PORT_PROPERTY);
	}

	public String getConnectionCommand()
	{
		return getProperty(InstallationConstants.GRID_CONNECTION_COMMAND_PROPERTY);
	}

	public String getDeploymentName()
	{
		return getProperty(InstallationConstants.GENII_DEPLOYMENT_NAME_PROPERTY);
	}

	public Collection<File> getDefaultOwnerFiles()
	{
		HierarchicalDirectory hier = getDefaultOwnersDirectory();
		if (hier == null)
			return null;
		Collection<File> ret = new LinkedList<File>();
		ret.addAll(Arrays.asList(hier.listFiles()));
		return ret;
	}

	public HierarchicalDirectory getLocalCertsDirectory()
	{
		String prop = getProperty(InstallationConstants.LOCAL_CERTS_DIRECTORY_PROPERTY);
		if (prop == null) {
			// fall back again, since we didn't have the field.
			return Installation.getDeployment(new DeploymentName()).security().getSecurityDirectory();
		}
		if (_logger.isDebugEnabled())
			_logger.debug("found local certs dir as " + prop);

		return HierarchicalDirectory.openRootHierarchicalDirectory(new File(prop));
	}

	public HierarchicalDirectory getDefaultOwnersDirectory()
	{
		String prop = getProperty(InstallationConstants.LOCAL_CERTS_DIRECTORY_PROPERTY);
		if (prop == null) {
			// fall back again, since we didn't have the field.
			return Installation.getDeployment(new DeploymentName()).security().getSecurityDirectory()
				.lookupDirectory(InstallationConstants.OWNER_CERTS_DIRECTORY_NAME);
		}
		if (_logger.isDebugEnabled())
			_logger.debug("found owner certs dir as " + prop);

		return HierarchicalDirectory.openRootHierarchicalDirectory(new File(prop + "/"
			+ InstallationConstants.OWNER_CERTS_DIRECTORY_NAME));
	}

	public Identity getOwnerCertificate()
	{
		// hmmm: cache the owner certificate here!
		HierarchicalDirectory dir = getLocalCertsDirectory();
		if (dir == null) {
			_logger.warn("failure: in get owner cert, the default owners dir is null.");
			return null;
		}

		File[] found = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.getName().equals(InstallationConstants.OWNER_CERT_FILE_NAME);
			}
		});

		if ((found == null) || (found.length == 0)) {
			_logger.warn("failure: in get owner cert, the list of files in the owners cert dir is null.");
			return null;
		}

		if (_logger.isTraceEnabled())
			_logger.trace("found owner cert at " + found[0].getAbsolutePath());
		File file = found[0];
		if (file.exists()) {
			try {
				GeniiPath filePath = new GeniiPath("local:" + file.getAbsolutePath());
				return AclAuthZClientTool.downloadIdentity(filePath);
			} catch (Throwable cause) {
				_logger.warn("Unable to get administrator certificate.", cause);
			}
		}
		return null;
	}

	/**
	 * The primary and recommended way to retrieve the user state directory.
	 */
	static public String getUserDir()
	{
		String userDir = null;
		// see if we have a valid container properties and can retrieve the value that way.
		ContainerProperties cProperties = ContainerProperties.getContainerProperties();
		if (cProperties != null)
			userDir = cProperties.getUserDirectoryProperty();
		// well, see if we can just get the state directory from the environment.
		if (userDir == null)
			userDir = ApplicationBase.getUserDirFromEnvironment();
		// now, if we have something at all, try comparing it with our replacement property.
		userDir = replaceKeywords(userDir);
		// make sure we don't go away empty-handed.
		if (userDir == null)
			userDir = ApplicationBase.getDefaultUserDir();
		// by now we'll have a state directory path, even if we have to use the default.
		try {
			// load the state directory so we can get an absolute path and also verify its health.
			File userDirFile = new GuaranteedDirectory(userDir, true);
			return userDirFile.getCanonicalPath();
		} catch (Throwable cause) {
			throw new RuntimeException("Unable to access or create state directory.", cause);
		}
	}

	// hmmm: move this.
	/**
	 * supports replacing a few keywords (or one really, currently) with environment variables.
	 */
	public static String replaceKeywords(String pathToFix)
	{
		// test for well-known singular replacements first.
		if ((pathToFix != null) && pathToFix.equals(ApplicationBase.USER_DIR_PROPERTY_VALUE)) {
			// there's our sentinel for loading the state directory from the environment variables.
			// let's try to load it.
			pathToFix = ApplicationBase.getUserDirFromEnvironment();
			if (pathToFix != null)
				return pathToFix;
			// nothing in environment, so fall back to default state directory, since we know this.
			return ApplicationBase.getDefaultUserDir();
		}
		// test for generalized "env-NAME" patterns for other environment variables.
		// hmmm: not implemented.
		// if there were any changes to make, they have been made.
		return pathToFix;
	}

}
