package edu.virginia.vcgr.genii.container.configuration;

import java.util.Properties;

import org.morgan.util.configuration.ConfigurationException;
import org.mortbay.http.SslListener;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class SslInformation
{
	static final private String _KEY_STORE_PROPERTY =
		"edu.virginia.vcgr.genii.container.security.ssl.key-store";
	static final private String _KEY_STORE_TYPE_PROPERTY =
		"edu.virginia.vcgr.genii.container.security.ssl.key-store-type";
	static final private String _KEY_STORE_PASS_PROPERTY =
		"edu.virginia.vcgr.genii.container.security.ssl.key-password";
	static final private String _KEY_PASS_PROPERTY =
		"edu.virginia.vcgr.genii.container.security.ssl.key-store-password";
	
	private String _keystoreFilename;
	private String _keystoreType;
	private String _keystorePassword;
	private String _keyPassword;
	
	public SslInformation(Properties properties)
		throws ConfigurationException
	{
		_keystoreFilename = properties.getProperty(_KEY_STORE_PROPERTY);
		_keystoreType = properties.getProperty(_KEY_STORE_TYPE_PROPERTY);
		_keystorePassword = properties.getProperty(_KEY_STORE_PASS_PROPERTY);
		_keyPassword = properties.getProperty(_KEY_PASS_PROPERTY);
		
		if (_keystoreFilename == null)
			throw new ConfigurationException("Required ssl property \"" + 
				_KEY_STORE_PROPERTY + "\" not found.");
		if (_keystoreType == null)
			throw new ConfigurationException("Required ssl property \"" + 
				_KEY_STORE_PROPERTY + "\" not found.");
	}
	
	public void configure(ConfigurationManager manager, SslListener listener)
	{
		listener.setKeystore(_keystoreFilename);
		listener.setKeystoreType(_keystoreType);
		listener.setPassword(_keystorePassword);
		listener.setKeyPassword(_keyPassword);
	}
}