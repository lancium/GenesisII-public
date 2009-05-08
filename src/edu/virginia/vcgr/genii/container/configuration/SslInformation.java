package edu.virginia.vcgr.genii.container.configuration;

import org.morgan.util.configuration.ConfigurationException;
import org.mortbay.jetty.security.SslSocketConnector;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.configuration.SecurityConstants;

public class SslInformation
{
	private String _keystoreFilename;
	private String _keystoreType;
	private String _keystorePassword;
	private String _keyPassword;
	
	public SslInformation(Security properties)
	{
		_keystoreFilename = properties.getProperty(
			SecurityConstants.Container.SSL_KEY_STORE_PROP);
		_keystoreType = properties.getProperty(
			SecurityConstants.Container.SSL_KEY_STORE_TYPE_PROP);
		_keystorePassword = properties.getProperty(
			SecurityConstants.Container.SSL_KEY_STORE_PASSWORD_PROP);
		_keyPassword = properties.getProperty(
			SecurityConstants.Container.SSL_KEY_PASSWORD_PROP);
		
		if (_keystoreFilename == null)
			throw new ConfigurationException("Required ssl property \"" + 
				SecurityConstants.Container.SSL_KEY_STORE_PROP + "\" not found.");
		if (_keystoreType == null)
			throw new ConfigurationException("Required ssl property \"" + 
				SecurityConstants.Container.SSL_KEY_STORE_TYPE_PROP + "\" not found.");
	}
	
	public void configure(ConfigurationManager manager, SslSocketConnector connector)
	{
		connector.setKeystore(
			(Installation.getDeployment(new DeploymentName()).security().getSecurityFile(
				_keystoreFilename)).getAbsolutePath());
		connector.setKeystoreType(_keystoreType);
		connector.setPassword(_keystorePassword);
		connector.setKeyPassword(_keyPassword);
		
		// request clients to authn with a cert
		connector.setWantClientAuth(true);
	}
}