package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.io.PreferredFiles;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class ConfigurationManager
{
	static private final String _USER_DIR_PROPERTY =
		"edu.virginia.vcgr.genii.client.configuration.user-dir";
	
	static private final String _CLIENT_CONF_FILENAME = "client-config.xml";
	static private final String _SERVER_CONF_FILENAME = "server-config.xml";
	
	// initialized configuration manager (cannot be re-initialized)
	static private ConfigurationManager _manager = null;

	
	// Place to remember the user directory for holding things like session state
	private File _userDir;
	// Place where config files are located
	private File _configDir;

	// Client and Container configuration files
	private XMLConfiguration _clientConf;
	private XMLConfiguration _serverConf;
	
	// denotes type of configuration for this process
	private Boolean _isClient = null;
	

	/**
	 * Gets the default configuration directory, which is one directory below
	 * wherever directory is indicated by the install-dir system property. 
	 * @return
	 */
	static public String getInstallDir() {
		String installDir = System.getProperty(
				GenesisIIConstants.INSTALL_DIR_SYSTEM_PROPERTY);
		if (installDir == null) {
			throw new RuntimeException("Couldn't read \"" + 
				GenesisIIConstants.INSTALL_DIR_SYSTEM_PROPERTY + "\" system property.");
		}
		return installDir;
	}

	/**
	 * Gets the default configuration directory, which is one directory below
	 * wherever directory is indicated by the install-dir system property. 
	 * @return
	 */
	static protected String getDefaultConfigDir() {
		return getInstallDir() + "/configuration";
	}
	
	static public ConfigurationManager getCurrentConfiguration()
	{
		synchronized (ConfigurationManager.class)
		{
			if (_manager == null)
			{
				throw new RuntimeException("No configuration manager has been initialized");
			}
			
			return _manager;
		}
	}		
	
	static public ConfigurationManager initializeConfiguration(String userDir)
	{
		return initializeConfiguration(null, userDir);
	}		

	/**
	 * Creates a configuration manager.  Can only be called once. 
	 * @param configurationDir Place to where client/container config files
	 * 		are located.  If null, will be filled in using the well-known 
	 * 		spot from the installation directory (as per the installation 
	 * 		directory system property) 
	 * @param userDir Place to remember the user directory for holding 
	 * 		things like session state
	 * @return
	 */
	static public ConfigurationManager initializeConfiguration(
			String configurationDir, 
			String userDir)
	{
		if (configurationDir == null) {
			configurationDir = getDefaultConfigDir();			
		}
		
		synchronized (ConfigurationManager.class)
		{
			if (_manager != null)
			{
				throw new RuntimeException("A configuration manager has already been initialized");
			}
			
			try {
				_manager = new ConfigurationManager(configurationDir, userDir);
			}
			catch (ConfigurationException ce)
			{
				throw new RuntimeException(ce.getLocalizedMessage(), ce);
			}
			return _manager;
		}
	}		

	protected ConfigurationManager(String configurationDir, String userDir)
		throws ConfigurationException
	{
		_configDir = new File(configurationDir);
		_userDir = new File(userDir);
		
		System.setProperty(_USER_DIR_PROPERTY, _userDir.getAbsolutePath());
		
		try
		{
			_clientConf = new XMLConfiguration(new File(configurationDir, _CLIENT_CONF_FILENAME));
			_serverConf = new XMLConfiguration(new File(configurationDir, _SERVER_CONF_FILENAME));
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(ioe.getLocalizedMessage(), ioe);
		}
	}
	
	public XMLConfiguration getContainerConfiguration()
	{
		return _serverConf;
	}
	
	public XMLConfiguration getClientConfiguration()
	{
		return _clientConf;
	}
	
	public File getUserDirectory() {
		return _userDir;
	}
	
	public File getConfigDirectory() {
		return _configDir;
	}

	synchronized private void setRole(Boolean isClient)
	{
		if (_isClient != null)
			throw new RuntimeException("Role already set -- can't reset.");
		
		_isClient = isClient;
	}
	
	public void setRoleClient()
	{
		setRole(Boolean.TRUE);
	}
	
	public void setRoleServer()
	{
		setRole(Boolean.FALSE);
	}
	
	synchronized public XMLConfiguration getRoleSpecificConfiguration()
	{
		if (_isClient == null)
			throw new RuntimeException("Role not set.");
		
		if (_isClient.booleanValue())
			return _clientConf;
		else
			return _serverConf;
	}
	
	synchronized public boolean isClientRole()
	{
		if (_isClient == null)
			throw new RuntimeException("Role not set.");
		
		if (_isClient.booleanValue())
			return true;
		else
			return false;
	}
	
}