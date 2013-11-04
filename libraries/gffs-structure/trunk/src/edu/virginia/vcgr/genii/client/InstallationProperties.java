package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.HierarchicalDirectory;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.configuration.KeystoreSecurityConstants;
import edu.virginia.vcgr.genii.client.configuration.WebContainerConstants;

public class InstallationProperties extends Properties
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(InstallationProperties.class);

	static final private String INSTALLATION_PROPERTIES_FILENAME = "installation.properties";

	static final private String TLS_KEY_PASSWORD_PROPERTY = KeystoreSecurityConstants.Container.SSL_KEY_PASSWORD_PROP;
	static final private String TLS_KEYSTORE_PASSWORD_PROPERTY =
		KeystoreSecurityConstants.Container.SSL_KEY_STORE_PASSWORD_PROP;
	static final private String TLS_KEYSTORE_FILE_PROPERTY = KeystoreSecurityConstants.Container.SSL_KEY_STORE_PROP;
	static final private String TLS_KEYSTORE_TYPE_PROPERTY = KeystoreSecurityConstants.Container.SSL_KEY_STORE_TYPE_PROP;
	static final private String CONTAINER_HOSTNAME_PROPERTY = Hostname._EXTERNAL_HOSTNAME_OVERRIDE_PROPERTY;
	static final private String CONTAINER_PORT_PROPERTY = WebContainerConstants.LISTEN_PORT_PROP;
	static final private String OWNER_CERTS_DIRECTORY = "edu.virginia.vcgr.genii.container.security.default-owners";
	static final private String SIGNING_KEYSTORE_FILE_PROPERTY =
		KeystoreSecurityConstants.Container.RESOURCE_IDENTITY_KEY_STORE_PROP;

	static private InstallationProperties _realInstallationProperties = new InstallationProperties();

	private boolean _existed = false;

	/**
	 * for all normal run-time classes, the installation properties are accessed this way.
	 */
	static public InstallationProperties getInstallationProperties()
	{
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
				_existed = true;
				_logger.debug("successfully loaded installation properties.");
			} catch (IOException e) {
				_logger.debug("failed to load installation properties.");
				return;
			} finally {
				StreamUtils.close(in);
			}
		}
	}

	public boolean existed()
	{
		return _existed;
	}

	static private File getInstallationPropertiesFile()
	{
		File ret = new File(ApplicationBase.getUserDir(), INSTALLATION_PROPERTIES_FILENAME);
			//ContainerProperties.getContainerProperties().getUserDirectory(), INSTALLATION_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;
		return null;
	}

	public String getTLSKeyPassword()
	{
		if (!existed())
			return null;
		return getProperty(TLS_KEY_PASSWORD_PROPERTY);
	}

	public String getTLSKeystorePassword()
	{
		if (!existed())
			return null;
		return getProperty(TLS_KEYSTORE_PASSWORD_PROPERTY);
	}

	// this member is an absolute path; do not try to use hierarchical deployment calls on it.
	public String getTLSKeystoreFile()
	{
		if (!existed())
			return null;
		return getProperty(TLS_KEYSTORE_FILE_PROPERTY);
	}

	public String getTLSKeystoreType()
	{
		if (!existed())
			return null;
		return getProperty(TLS_KEYSTORE_TYPE_PROPERTY);
	}

	public String getSigningKeystoreFile()
	{
		if (!existed())
			return null;
		return getProperty(SIGNING_KEYSTORE_FILE_PROPERTY);
	}

	public String getContainerHostname()
	{
		if (!existed())
			return null;
		return getProperty(CONTAINER_HOSTNAME_PROPERTY);
	}

	public String getContainerPort()
	{
		if (!existed())
			return null;
		return getProperty(CONTAINER_PORT_PROPERTY);
	}

	public HierarchicalDirectory getDefaultOwnersDirectory()
	{
		if (!existed())
			return null;
		String prop = getProperty(OWNER_CERTS_DIRECTORY);
		if (prop == null)
			return null;
		return new HierarchicalDirectory(prop, new ArrayList<File>());
	}

	public File getOwnerCertificate()
	{
		HierarchicalDirectory dir = getDefaultOwnersDirectory();
		if (dir == null)
			return null;
		File[] found = dir.listFiles(new FileFilter()
		{
			public boolean accept(File f)
			{
				return f.getName().equals("owner.cer");
			}
		});
		if (found == null)
			return null;
		return found[0];
	}

}
