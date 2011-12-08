package edu.virginia.vcgr.genii.container.configuration;

import java.util.Properties;

import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.WebContainerConstants;

public class ContainerConfiguration
{
	static private final String _NOTIFICATION_POOL_SIZE =
		"edu.virginia.vcgr.genii.container.notification.work-pool-size";
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
	
	public ContainerConfiguration(ConfigurationManager manager)
	{
		_configuration = manager.getContainerConfiguration();
		
		_globalProperties =
			(Properties)_configuration.retrieveSection(
				GenesisIIConstants.GLOBAL_PROPERTY_SECTION_NAME);
		setupProperties(_globalProperties);
		
		DeploymentName name = new DeploymentName();
		String useSSLString = 
			Installation.getDeployment(name).webContainerProperties().getProperty(
				WebContainerConstants.USE_SSL_PROP);
		if (useSSLString != null && useSSLString.equalsIgnoreCase("true"))
			_sslInformation = 
				new SslInformation(Installation.getDeployment(name).security());
		else
			_sslInformation = null;
		
		String trustSelfSigned =
				Installation.getDeployment(name).webContainerProperties().getProperty(
						WebContainerConstants.TRUST_SELF_SIGNED);
		
		if (trustSelfSigned != null && trustSelfSigned.equalsIgnoreCase("true"))
			_trustSelfSigned = true;
		
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
	
	public boolean trustSelfSigned(){
		return _trustSelfSigned;
	}
	
	public SslInformation getSslInformation()
	{
		return _sslInformation;
	}
	
	private void setupProperties(Properties props)
	{
		String sListenPort = 
			Installation.getDeployment(new DeploymentName()).webContainerProperties().getProperty(
					WebContainerConstants.LISTEN_PORT_PROP, 
					_DEFAULT_LISTEN_PORT_VALUE);
		_listenPort = Integer.parseInt(sListenPort);
		
		String dListenPort =
			Installation.getDeployment(new DeploymentName()).webContainerProperties().getProperty(
				WebContainerConstants.DPAGES_PORT_PROP);
		if (dListenPort != null)
			_dpagesPort = Integer.valueOf(dListenPort);
		
		String sMaxThreads =
			Installation.getDeployment(new DeploymentName()).webContainerProperties().getProperty(
				WebContainerConstants.MAX_ACCEPT_THREADS_PROP,
				_DEFAULT_MAX_ACCEPT_THREADS);
		_maxThreads = Integer.parseInt(sMaxThreads);
		
		String notSize = props.getProperty(
			_NOTIFICATION_POOL_SIZE, _NOTIFICATION_POOL_SIZE_DEFAULT);
		_notificationPoolSize = Integer.parseInt(notSize);
	}
}