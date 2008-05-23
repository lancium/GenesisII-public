package edu.virginia.vcgr.genii.container.configuration;

import java.util.Properties;

import javax.xml.namespace.QName;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class ContainerConfiguration
{
	static private final String _NOTIFICATION_POOL_SIZE =
		"edu.virginia.vcgr.genii.container.notification.work-pool-size";
	static private final String _NOTIFICATION_POOL_SIZE_DEFAULT = "5";
	
	static public QName SSL_PROPERTY_SECTION_NAME =
		new QName(GenesisIIConstants.GENESISII_NS, "ssl-properties");
	
	static final private String _LISTEN_PORT_PROPERTY =
		"edu.virginia.vcgr.genii.container.listen-port";
	static private final String _DEFAULT_LISTEN_PORT_VALUE = "18080";	
	
	private XMLConfiguration _configuration;
	private int _listenPort;
	private SslInformation _sslInformation = null;
	private int _notificationPoolSize;
	private Properties _globalProperties;
	
	public ContainerConfiguration(ConfigurationManager manager)
	{
		_configuration = manager.getContainerConfiguration();
		
		_globalProperties =
			(Properties)_configuration.retrieveSection(
				GenesisIIConstants.GLOBAL_PROPERTY_SECTION_NAME);
		setupProperties(_globalProperties);
		
		Properties sslProps = null;
		try
		{
			sslProps = (Properties)_configuration.retrieveSection(
				SSL_PROPERTY_SECTION_NAME);
			_sslInformation = new SslInformation(sslProps);
		}
		catch (ConfigurationException ce)
		{
			if (sslProps != null)
				throw ce;
		}
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
	
	public boolean isSSL()
	{
		return _sslInformation != null;
	}
	
	public SslInformation getSslInformation()
	{
		return _sslInformation;
	}
	
	private void setupProperties(Properties props)
	{
		String sListenPort = props.getProperty(
			_LISTEN_PORT_PROPERTY, _DEFAULT_LISTEN_PORT_VALUE);
		_listenPort = Integer.parseInt(sListenPort);
		
		String notSize = props.getProperty(
			_NOTIFICATION_POOL_SIZE, _NOTIFICATION_POOL_SIZE_DEFAULT);
		_notificationPoolSize = Integer.parseInt(notSize);
	}
}