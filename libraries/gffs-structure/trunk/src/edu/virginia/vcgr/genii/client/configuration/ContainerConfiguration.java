package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.security.x509.CertEntry;
import edu.virginia.vcgr.genii.security.x509.SimpleKeystoreLoader;

public class ContainerConfiguration
{
	static private Log _logger = LogFactory.getLog(ContainerConfiguration.class);

	static private final String _NOTIFICATION_POOL_SIZE = "edu.virginia.vcgr.genii.container.notification.work-pool-size";
	static private final String _NOTIFICATION_POOL_SIZE_DEFAULT = "5";

	static private final String _DEFAULT_MAX_ACCEPT_THREADS = "16";
	static private final String _DEFAULT_LISTEN_PORT_VALUE = "18080";

	private XMLConfiguration _configuration;
	private int _maxThreads;
	private int _listenPort;
	private Integer _dpagesPort = null;
	private SslInformation _sslInformation = null;
	private int _notificationPoolSize;
	private Properties _globalProperties;
	private boolean _trustSelfSigned = false;

	static CertEntry _savedContainerCert = null;
	static Object _lockSavedCert = new Object();

	/*
	 * static holder for the "real" container configuration, owned by the container object. this
	 * will be null if the role is not actually for a container.
	 */
	private static ContainerConfiguration _theConConf = null;

	/**
	 * constructor uses the configuration manager to retrieve most properties. some are filled in by
	 * the container itself, such as the ssl information.
	 */
	public ContainerConfiguration(ConfigurationManager manager)
	{
		_configuration = manager.getContainerConfiguration();

		_globalProperties = (Properties) _configuration.retrieveSection(GenesisIIConstants.GLOBAL_PROPERTY_SECTION_NAME);
		setupProperties(_globalProperties);

		DeploymentName name = new DeploymentName();

		// we always create the ssl info now since we use features from it even if ssl is not
		// enabled.
		_sslInformation = new SslInformation(Installation.getDeployment(name).security());

		String trustSelfSigned =
			Installation.getDeployment(name).webContainerProperties().getProperty(WebContainerConstants.TRUST_SELF_SIGNED);

		if (trustSelfSigned != null && trustSelfSigned.equalsIgnoreCase("true"))
			_trustSelfSigned = true;
	}

	/**
	 * returns the statically held reference to the container's real configuration. this is null for
	 * a client.
	 */
	static public ContainerConfiguration getTheContainerConfig()
	{
		return _theConConf;
	}

	static public void setTheContainerConfig(ContainerConfiguration realConf)
	{
		_theConConf = realConf;
	}

	public Properties getGlobalProperties()
	{
		return _globalProperties;
	}

	public int getNotificationPoolSize()
	{
		return _notificationPoolSize;
	}

	public int getListenPort()
	{
		return _listenPort;
	}

	public Integer getDPagesPort()
	{
		return _dpagesPort;
	}

	public int getMaxAcceptorThreads()
	{
		return _maxThreads;
	}

	public boolean isSSL()
	{
		return _sslInformation != null;
	}

	public boolean trustSelfSigned()
	{
		return _trustSelfSigned;
	}

	public SslInformation getSslInformation()
	{
		return _sslInformation;
	}

	private void setupProperties(Properties props)
	{
		String sListenPort = InstallationProperties.getInstallationProperties().getContainerPort();
		if (sListenPort == null)
			sListenPort =
				Installation.getDeployment(new DeploymentName()).webContainerProperties()
					.getProperty(WebContainerConstants.LISTEN_PORT_PROP, _DEFAULT_LISTEN_PORT_VALUE);
		_listenPort = Integer.parseInt(sListenPort);

		String dListenPort =
			Installation.getDeployment(new DeploymentName()).webContainerProperties()
				.getProperty(WebContainerConstants.DPAGES_PORT_PROP);
		if (dListenPort != null)
			_dpagesPort = Integer.valueOf(dListenPort);

		String sMaxThreads =
			Installation.getDeployment(new DeploymentName()).webContainerProperties()
				.getProperty(WebContainerConstants.MAX_ACCEPT_THREADS_PROP, _DEFAULT_MAX_ACCEPT_THREADS);
		_maxThreads = Integer.parseInt(sMaxThreads);

		String notSize = props.getProperty(_NOTIFICATION_POOL_SIZE, _NOTIFICATION_POOL_SIZE_DEFAULT);
		_notificationPoolSize = Integer.parseInt(notSize);
	}

	// returns the container TLS certificate for outgoing connections.
	public static CertEntry getContainerTLSCert()
	{
		synchronized (_lockSavedCert) {
			if (_savedContainerCert != null)
				return _savedContainerCert;
		}

		try {
			if (ConfigurationManager.getCurrentConfiguration().isServerRole()) {
				ContainerConfiguration containerConf = ContainerConfiguration.getTheContainerConfig();
				if (containerConf == null) {
					_logger.error("failure: found that container configuration is null for a server role.");
				} else {
					SslInformation si = containerConf.getSslInformation();
					SimpleKeystoreLoader skl = new SimpleKeystoreLoader();
					File actualKeyStore = new File(si.getKeystoreFilename());
					if (_logger.isTraceEnabled())
						_logger.trace("loading keystore from file: " + actualKeyStore.getAbsolutePath());
					FileInputStream keyInput = new FileInputStream(actualKeyStore);

					CertEntry entry = skl.selectCert(keyInput, si.getKeystoreType(), si.getKeystorePassword(), false, null);
					if (entry != null) {
						if (_logger.isDebugEnabled())
							_logger.debug("selected this certificate for tls outcalls: " + entry._certChain[0].getSubjectDN());
						synchronized (_lockSavedCert) {
							_savedContainerCert = entry;
						}
						return entry;
					}
					_logger.error("failure: did not find the TLS certificate for container outcalls.");
				}
			}
		} catch (Throwable e) {
			_logger.error("failure: exception occurred while looking up container TLS certificate.");
		}
		return null;
	}
}