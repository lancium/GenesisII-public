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
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.security.axis.AclAuthZClientTool;
import edu.virginia.vcgr.genii.security.identity.Identity;

/**
 * This class supports queries for items normally found in the Security class (via the
 * KeystoreSecurityConstants). It also supports some crucial items that were previously in
 * ContainerProperties and WebContainerConstants. It should be used as a first resource that
 * provides a "more local" configuration than older style (split configuration) installs. This new
 * lookup process implements the new configuration style we've called "unified configuration" in the
 * installer design document. Many configuration items can now be overridden in the file called
 * "installation.properties" that lives in the state directory, which is managed by the new
 * container configuration scripts (see trunk/installer/scripts).
 * 
 * @author Chris Koeritz
 */
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
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;
		return null;
	}

	public String getContainerPort()
	{
		return getProperty(InstallationConstants.CONTAINER_PORT_PROPERTY);
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
		// TODO: cache the owner certificate here.
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
			_logger.warn("no owner certificate; the list of files in the owners cert dir is empty.");
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

	public File getSecurityFile(String filename)
	{
		File toReturn = getLocalCertsDirectory().lookupFile(filename);
		if (!toReturn.exists())
			return null;
		return toReturn;
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
		userDir = ApplicationBase.replaceKeywords(userDir);
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
}
