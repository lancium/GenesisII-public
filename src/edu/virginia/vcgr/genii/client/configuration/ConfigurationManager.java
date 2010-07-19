package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.ggf.jsdl.FileSystem_Type;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemManager;

public class ConfigurationManager
{
	static public final String _USER_DIR_PROPERTY =
		"edu.virginia.vcgr.genii.client.configuration.user-dir";
	
	static private final String _CLIENT_CONF_FILENAME = "client-config.xml";
	static private final String _SERVER_CONF_FILENAME = "server-config.xml";
	
	// initialized configuration manager (cannot be re-initialized)
	static private ConfigurationManager _manager = null;
	
	static private ArrayList<ConfigurationUnloadedListener> _unloadListeners = 
		new ArrayList<ConfigurationUnloadedListener>();

	
	// Place to remember the user directory for holding things like session state
	private File _userDir;
	
	// Client and Container configuration files
	private XMLConfiguration _clientConf;
	private XMLConfiguration _serverConf;
	
	// denotes type of configuration for this process
	private Boolean _isClient = null;
	
	private FilesystemManager _filesystemManager = null;
	
	static
	{
		FileSystem_Type.getTypeDesc().getFieldByName(
			"uniqueId").setXmlName(new QName(
				"http://vcgr.cs.virginia.edu/genesisII/jsdl", "unique-id"));
	}

	/** 
	 * Add a listener to be notified when the current configuration is unloaded
	 * @param listener
	 */
	static synchronized public void addConfigurationUnloadListener(
			ConfigurationUnloadedListener listener) {
		
		_unloadListeners.add(listener);
	}
	
	/**
	 * Gets the default configuration directory, which is one directory below
	 * wherever directory is indicated by the install-dir system property. 
	 * @return
	 */
	/* jfk3w - 10/4/07.  Looks wrong and could not find a reference to it.  Commented out to test if really needed...
	static protected String getDefaultConfigDir() {
		return getInstallDir() + "/configuration";
	}
   */
	
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
	static public ConfigurationManager initializeConfiguration(String userDir)
	{
		synchronized (ConfigurationManager.class)
		{
			if (_manager != null)
			{
				throw new RuntimeException("A configuration manager has already been initialized");
			}
			
			_manager = new ConfigurationManager(userDir);
			
			return _manager;
		}
	}		

	/**
	 * Creates new configuration manager and sets it as new _manager using the role and user directory from the current manager 
	 * @return
	 */
	static public ConfigurationManager reloadConfiguration(String userDir)
	{
		synchronized (ConfigurationManager.class)
		{
			if (_manager == null)
				throw new RuntimeException("Cannot call reloadConfiguration() before initializing configuration manager first.");

			// notify interested parties that they might want to unload
			// any cached items from the configuration(s)
			for (ConfigurationUnloadedListener listener : _unloadListeners) {
				listener.notifyUnloaded();
			}
			
			_manager = new ConfigurationManager(userDir);
			return _manager;
		}
	}		

	protected ConfigurationManager(String userDir)
	{
		_userDir = new File(userDir);
		
		System.setProperty(_USER_DIR_PROPERTY, _userDir.getAbsolutePath());
		
		Deployment deployment = Installation.getDeployment(new DeploymentName());
		try
		{
			_clientConf = new XMLConfiguration(
				deployment.getConfigurationFile(_CLIENT_CONF_FILENAME));
			_serverConf = new XMLConfiguration(
				deployment.getConfigurationFile(_SERVER_CONF_FILENAME));
			
			File fsConf = deployment.getConfigurationFile("filesystems.xml");
			if (fsConf.exists())
				_filesystemManager = new FilesystemManager(fsConf);
			else
				_filesystemManager= new FilesystemManager();
			
			Thread th = new Thread(new FilesystemPoller(_filesystemManager),
				"Filesystem Polling Thread");
			th.setDaemon(true);
			th.start();
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
	
	final public FilesystemManager filesystemManager()
	{
		return _filesystemManager;
	}
	
	synchronized private void setRole(Boolean isClient)
	{
		/*
		if (_isClient != null)
			throw new RuntimeException("Role already set -- can't reset.");
		*/
		
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
	
	static private class FilesystemPoller implements Runnable 
	{
		private FilesystemManager _manager;
		
		private FilesystemPoller(FilesystemManager manager)
		{
			_manager = manager;
		}
		
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					_manager.enterPollingLoop();
				}
				catch (InterruptedException ie)
				{
					// Ignore
				}
			}
		}
	}
}