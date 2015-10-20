package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.appmgr.launcher.ApplicationDescription;
import edu.virginia.vcgr.genii.client.comm.socket.SocketConfigurer;

/**
 * client-side configuration properties. the configuration file for these properties is intended to be easily modifiable by the user.
 */
public class ClientProperties extends Properties
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(ClientProperties.class);

	/* ... */

	// the file where all of these settings can be found. this file is only expected in the state directory.
	static final private String CLIENT_PROPERTIES_FILENAME = "client.properties";

	// the field (property) name for the connection command.
	static final public String GRID_CONNECTION_COMMAND_PROPERTY = "edu.virginia.vcgr.genii.gridInitCommand";

	/*
	 * the amount of time that any particular client request is allowed to take before time expires.
	 */
	static private int DEFAULT_CLIENT_REQUEST_TIMEOUT = 1000 * 60 * 1; // currently one minute timeout by default.
	static final public String CLIENT_REQUEST_TIMEOUT_PROPERTY = "gffs.client_timeout";

	/*
	 * this is the maximum time that an operation can take before it is possible to consider adding it from the dead hosts pool. if it takes
	 * longer than this fairly short duration, then we consider that it is really down.
	 */
	final long DEFAULT_MAXIMUM_ALLOWABLE_CONNECTION_PAUSE = 5000L;
	public static String MAXIMUM_ALLOWABLE_CONNECTION_PAUSE_PROPERTY = "gffs.max_connection_pause";

	/*
	 * We'll wait this long for a connection failure before it's considered TOO long for the exponential back-off retry. This value should be
	 * larger than the per RPC timeout!
	 */
	static private final long DEFAULT_MAX_FAILURE_TIME_RETRY = DEFAULT_CLIENT_REQUEST_TIMEOUT * 2;
	static public String MAX_FAILURE_TIME_RETRY_PROPERTY = "gffs.max_failure_retry_time";

	// file name for the cache properties, found either in the lib directory or the state directory.
	static private final String CLIENT_CACHE_PROPERTIES_FILENAME = "cache.properties";

	// the property name for the name of the current deployment.
	static final public String GENII_DEPLOYMENT_NAME_PROPERTY_NAME = "gffs.deployment-name";

	// the property name for the currently configured deployments directory.
	static final String GENII_DEPLOYMENT_DIRECTORY_PROPERTY_NAME = "gffs.deployment-directory";

	// the name of the deployments directory in the installation.
	static final String DEPLOYMENTS_DIRECTORY_NAME = "deployments";

	// file name for the socket properties.
	static private final String CLIENT_SOCKET_PROPERTIES_FILENAME = "socket.properties";

	// the property name for the ssl socket factory cache size.
	static final private String SOCKET_CACHE_SIZE_PROPERTY_NAME = "gffs.socket_cache_size";

	// the default ssl socket cache size.
	static final private int DEFAULT_MAX_CACHE_ELEMENTS = 128;

	// property name for the error reporter address.
	static final private String ERROR_REPORT_TARGET_PROPERTY = "edu.virginia.vcgr.genii.ui.error.report-target";

	// if not found, this is the default error reporter.
	static final private String DEFAULT_ERROR_REPORT_TARGET = "http://vcgr.cs.virginia.edu/ui/report/reporter.php";

	//
	static private final String DEFAULT_MAXIMUM_SIMULTANEOUS_CONNECTIONS = "128";

	// property name for the maximum connections allowed by URI manager.
	static private final String MAXIMUM_SIMULTANEOUS_CONNECTIONS_PROPERTY =
		"edu.virginia.vcgr.genii.client.io.uri-manager.max-simultaneous-connections";

	/* ... */

	// our singular instance of this class.
	static private ClientProperties _realClientProperties = null;

	// loaded on demand but kept around to avoid reloading continually.
	private Properties _clientCacheProperties = null;

	// our singular instance of the socket configuration derived from our config file.
	private SocketConfigurer _clientSocketConfigurer = null;

	// holds the maximum elements for the socket cache, rather than reading it from file every time.
	static private Integer _maxSocketCacheElements = -1;

	/* ... */

	/**
	 * for all normal run-time classes, the properties are accessed this way.
	 */
	static public ClientProperties getClientProperties()
	{
		synchronized (ClientProperties.class) {
			if (_realClientProperties == null) {
				_realClientProperties = new ClientProperties();
			}
		}
		return _realClientProperties;
	}

	private ClientProperties()
	{
		File file = getClientPropertiesFile();
		if (file != null) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				load(in);
				if (_logger.isTraceEnabled()) {
					_logger.debug("successfully loaded installation properties.");
				}
			} catch (IOException e) {
				_logger.error("failed to load installation properties", e);
				return;
			} finally {
				StreamUtils.close(in);
			}
		}
	}

	/**
	 * the approach for client.properties is that we seek the file in the state directory first. if it's not found there, we fall back to the
	 * installation directory to find the file. if still not found, then we have no client properties established. this explicitly does not
	 * support finding settings in multiple files; instead, the entire client.properties file that is found is used, and no overrides are
	 * sought in other files.
	 */
	static private File getClientPropertiesFile()
	{
		// see if there's a client properties file in the state directory.
		File ret = new File(InstallationProperties.getUserDir(), CLIENT_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;

		// we need to see if there is a basic one in the install directory if we couldn't find one in state dir.
		ret = new File(ApplicationDescription.getInstallationDirectory() + "/lib", CLIENT_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;

		return null;
	}

	/* ... */

	/**
	 * looks up a help identifier "helpLink" in the property list. this identifier should be one of the well known ones in
	 * HelpLinkConfiguration.
	 */
	public String getHelpFileProperty(String helpLink)
	{
		// no override to deal with; we use the whole client.properties file when we find one.
		return getProperty(helpLink);
	}

	/* ... */

	/**
	 * returns the command line for a grid connect command (minus 'grid' and 'connect'). if this property has not been set, then this returns
	 * null. this will also first consult our installation properties in case we can locate an override there.
	 */
	public String getConnectionCommand()
	{
		String toReturn = InstallationProperties.getInstallationProperties().getProperty(GRID_CONNECTION_COMMAND_PROPERTY);
		if (toReturn == null)
			toReturn = getProperty(GRID_CONNECTION_COMMAND_PROPERTY);
		return toReturn;
	}

	/**
	 * retrieves the client timeout. this default is allowed to come from the installation properties file also.
	 */
	public int getClientTimeout()
	{
		String toReturn = InstallationProperties.getInstallationProperties().getProperty(CLIENT_REQUEST_TIMEOUT_PROPERTY);
		if (toReturn == null)
			toReturn = getProperty(CLIENT_REQUEST_TIMEOUT_PROPERTY);
		return Integer.parseInt(toReturn == null ? "" + DEFAULT_CLIENT_REQUEST_TIMEOUT : toReturn);
	}

	/**
	 * retrieves the maximum time allowed for an operation to connect before it's considered too long for exponential backoffs.
	 */
	public int getMaximumRetryTime()
	{
		String toReturn = InstallationProperties.getInstallationProperties().getProperty(MAX_FAILURE_TIME_RETRY_PROPERTY);
		if (toReturn == null)
			toReturn = getProperty(MAX_FAILURE_TIME_RETRY_PROPERTY);
		return Integer.parseInt(toReturn == null ? "" + DEFAULT_MAX_FAILURE_TIME_RETRY : toReturn);
	}

	/**
	 * returns the configured value for the longest a connection can be in a non-successful state before we decide it's worthy of including in
	 * the dead host list. note that it must still fail to connect; we just ignore connection failures if they take less than this time, since
	 * they are not painful to retry.
	 */
	public int getMaximumAllowableConnectionPause()
	{
		String toReturn = InstallationProperties.getInstallationProperties().getProperty(MAXIMUM_ALLOWABLE_CONNECTION_PAUSE_PROPERTY);
		if (toReturn == null)
			toReturn = getProperty(MAXIMUM_ALLOWABLE_CONNECTION_PAUSE_PROPERTY);
		return Integer.parseInt(toReturn == null ? "" + DEFAULT_MAXIMUM_ALLOWABLE_CONNECTION_PAUSE : toReturn);
	}

	/* ... */

	/**
	 * returns the file name of the client cache properties, if we can find one.
	 */
	public File getClientCachePropertyFile()
	{
		// state directory first.
		File ret = new File(InstallationProperties.getUserDir(), CLIENT_CACHE_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;

		// we need to see if there is a basic one in the install directory if we couldn't find one in state dir.
		ret = new File(ApplicationDescription.getInstallationDirectory() + "/lib", CLIENT_CACHE_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;

		return null;
	}

	/**
	 * worker method that does the actual property loading.
	 */
	private Properties loadClientCacheProperties()
	{
		FileInputStream fin = null;
		Properties ret = new Properties();

		try {
			File cacheProps = ClientProperties.getClientProperties().getClientCachePropertyFile();
			if (cacheProps == null) {
				throw new IOException("could not locate client cache properties file: " + CLIENT_CACHE_PROPERTIES_FILENAME);
			}
			fin = new FileInputStream(cacheProps);
			ret.load(fin);
			return ret;
		} catch (IOException ioe) {
			_logger.error("Unable to load cache configuration properties. Caching will be disabled.", ioe);
			return new Properties();
		} finally {
			StreamUtils.close(fin);
		}
	}

	/**
	 * loads the configuration properties used by the cache.
	 */
	synchronized public Properties getClientCacheProperties()
	{
		if (_clientCacheProperties == null) {
			_clientCacheProperties = loadClientCacheProperties();
		}
		return _clientCacheProperties;
	}

	/* ... */

	/**
	 * returns the file name of the client socket properties, if we can find one.
	 */
	public File getClientSocketPropertyFile()
	{
		// state directory first.
		File ret = new File(InstallationProperties.getUserDir(), CLIENT_SOCKET_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;

		// we need to see if there is a basic one in the install directory if we couldn't find one in state dir.
		ret = new File(ApplicationDescription.getInstallationDirectory() + "/lib", CLIENT_SOCKET_PROPERTIES_FILENAME);
		if (ret.exists() && ret.isFile() && ret.canRead())
			return ret;

		return null;
	}

	/**
	 * worker method that does the actual property loading.
	 */
	private SocketConfigurer loadClientSocketConfigurer()
	{
		Properties properties = new Properties();

		File confFile = getClientSocketPropertyFile();
		if ((confFile != null) && confFile.exists()) {
			FileInputStream fin = null;

			try {
				fin = new FileInputStream(confFile);
				properties.load(fin);
			} catch (IOException ioe) {
				_logger.warn("Unable to load client-socket properties.", ioe);
			} finally {
				StreamUtils.close(fin);
			}
		}

		return new SocketConfigurer(properties);
	}

	// hmmm: is this used to configure accepting sockets also??
	/**
	 * loads the configuration properties used by for configuring client (outgoing) sockets.
	 */
	synchronized public SocketConfigurer getClientSocketProperties()
	{
		if (_clientSocketConfigurer == null) {
			_clientSocketConfigurer = loadClientSocketConfigurer();
		}
		return _clientSocketConfigurer;
	}

	/* ... */

	/**
	 * returns the maximum number of cached ssl socket factory objects.
	 */
	synchronized public int getMaxSocketCacheElements()
	{
		if (_maxSocketCacheElements > -1)
			return _maxSocketCacheElements;

		_maxSocketCacheElements = Integer.parseInt(getProperty(SOCKET_CACHE_SIZE_PROPERTY_NAME, "" + DEFAULT_MAX_CACHE_ELEMENTS));

		if (_maxSocketCacheElements < 0)
			_maxSocketCacheElements = DEFAULT_MAX_CACHE_ELEMENTS;

		return _maxSocketCacheElements;
	}

	/* ... */

	/**
	 * returns the target for the UI's error reporting.
	 */
	public String getErrorReportTarget()
	{
		return getProperty(ERROR_REPORT_TARGET_PROPERTY, DEFAULT_ERROR_REPORT_TARGET);
	}

	/* ... */

	/**
	 * returns the maximum simultaneous connections allowed by the URI manager.
	 */
	public int getMaximumURIManagerConnections()
	{
		return Integer.parseInt(getProperty(MAXIMUM_SIMULTANEOUS_CONNECTIONS_PROPERTY, "" + DEFAULT_MAXIMUM_SIMULTANEOUS_CONNECTIONS));
	}

	/* ... */

}
