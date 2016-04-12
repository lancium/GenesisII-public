package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.jsdl.FileSystem_Type;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileSystemHelper;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemManager;

public class ConfigurationManager
{	
	static private Log _logger = LogFactory.getLog(ConfigurationManager.class);

	static public final String _USER_DIR_PROPERTY = "edu.virginia.vcgr.genii.client.configuration.user-dir";

	static private final String _CLIENT_CONF_FILENAME = "client-config.xml";
	static private final String _SERVER_CONF_FILENAME = "server-config.xml";

	// initialized configuration manager.
	//(old: "cannot be re-initialized"; now it is allowed to be reinitialized)
	static private ConfigurationManager _manager = null;

	static private ArrayList<ConfigurationUnloadedListener> _unloadListeners = new ArrayList<ConfigurationUnloadedListener>();

	// Place to remember the user directory for holding things like session state
	private File _userDir;

	// Client and Container configuration files
	private XMLConfiguration _clientConf = null;
	private XMLConfiguration _serverConf = null;

	// denotes type of configuration for this process
	private Boolean _isClient = null;

	// only used for server role; tracks space available on file systems.
	static private Thread _filesystemPollingThread = null;

	private FilesystemManager _filesystemManager = null;

	static {
		FileSystem_Type.getTypeDesc().getFieldByName("uniqueId")
			.setXmlName(new QName("http://vcgr.cs.virginia.edu/genesisII/jsdl", "unique-id"));
	}

	/**
	 * Add a listener to be notified when the current configuration is unloaded
	 * 
	 * @param listener
	 */
	static synchronized public void addConfigurationUnloadListener(ConfigurationUnloadedListener listener)
	{
		_unloadListeners.add(listener);
	}

	/**
	 * Gets the default configuration directory, which is one directory below wherever directory is indicated by the install-dir system
	 * property.
	 */
	static public ConfigurationManager getCurrentConfiguration()
	{
		synchronized (ConfigurationManager.class) {
			if (_manager == null) {
				throw new RuntimeException("No configuration manager has been initialized");
			}

			return _manager;
		}
	}

	/**
	 * Creates a configuration manager. Can only be called once.
	 * 
	 * @param configurationDir
	 *            Place to where client/container config files are located. If null, will be filled in using the well-known spot from the
	 *            installation directory (as per the installation directory system property)
	 * @param userDir
	 *            Place to remember the user directory for holding things like session state
	 * @return
	 */
	static public ConfigurationManager initializeConfiguration(String userDir)
	{
		synchronized (ConfigurationManager.class) {
			if (_manager != null) {
				_logger.info("Dropping current ConfigurationManager and recreating.");
				reloadConfiguration();
			} else {
				_manager = new ConfigurationManager(userDir);
			}

			return _manager;
		}
	}

	/**
	 * Creates new configuration manager and sets it as new _manager using the role and user directory from the current manager
	 * 
	 * @return
	 */
	static public ConfigurationManager reloadConfiguration()
	{
		synchronized (ConfigurationManager.class) {
			if (_manager == null)
				throw new RuntimeException("Cannot call reloadConfiguration() before initializing configuration manager first.");

			/*
			 * notify interested parties that they might want to unload any cached items from the configuration(s). we make a copy of the list
			 * here in case listeners are added during the config reload.
			 */
			@SuppressWarnings("unchecked")
			ArrayList<ConfigurationUnloadedListener> listenersCopy = (ArrayList<ConfigurationUnloadedListener>) _unloadListeners.clone();
			for (ConfigurationUnloadedListener listener : listenersCopy) {
				listener.notifyUnloaded();
			}
			// drop that copy as soon as possible.
			listenersCopy = null;

			boolean isClient = _manager.isClientRole();
			File userDirFile = _manager.getUserDirectory();
			_manager = new ConfigurationManager(userDirFile.getAbsolutePath());
			if (isClient)
				_manager.setRoleClient();
			else
				_manager.setRoleServer();
			return _manager;
		}
	}

	protected ConfigurationManager(String userDir)
	{
		_userDir = new File(userDir);

		System.setProperty(_USER_DIR_PROPERTY, FileSystemHelper.sanitizeFilename(_userDir.getAbsolutePath()));

		try {
			Deployment deployment = Installation.getDeployment(new DeploymentName());

			_clientConf = new XMLConfiguration(deployment.getConfigurationFile(_CLIENT_CONF_FILENAME));
			File servConf = deployment.getConfigurationFile(_SERVER_CONF_FILENAME);
			if (servConf.exists())
				_serverConf = new XMLConfiguration(servConf);

			File fsConf = deployment.getConfigurationFile("filesystems.xml");
			if (fsConf.exists())
				_filesystemManager = new FilesystemManager(fsConf);
			else
				_filesystemManager = new FilesystemManager();
		} catch (IOException ioe) {
			throw new ConfigurationException(ioe.getLocalizedMessage(), ioe);
		}
	}

	public XMLConfiguration getContainerConfiguration()
	{
		if (_serverConf == null)
			throw new RuntimeException("failed to load server configuration on this deployment");

		return _serverConf;
	}

	public XMLConfiguration getClientConfiguration()
	{
		return _clientConf;
	}

	public File getUserDirectory()
	{
		return _userDir;
	}

	final public FilesystemManager filesystemManager()
	{
		return _filesystemManager;
	}

	synchronized private void setRole(Boolean isClient)
	{
		/*
		 * if (_isClient != null) throw new RuntimeException("Role already set -- can't reset.");
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

		if (_filesystemPollingThread == null) {
			_filesystemPollingThread = new Thread(new FilesystemPoller(_filesystemManager), "Filesystem Polling Thread");
			_filesystemPollingThread.setDaemon(true);
			_filesystemPollingThread.start();
		}
	}

	synchronized public XMLConfiguration getRoleSpecificConfiguration()
	{
		if (_isClient == null)
			throw new RuntimeException("Role not set.");

		if (_isClient.booleanValue())
			return getClientConfiguration();
		else
			return getContainerConfiguration();
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

	synchronized public boolean isServerRole()
	{
		if (_isClient == null)
			throw new RuntimeException("Role not set.");

		if (_isClient.booleanValue())
			return false;
		else
			return true;
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
			while (true) {
				try {
					_manager.enterPollingLoop();
				} catch (InterruptedException ie) {
					// Ignore
				}
			}
		}
	}
}
