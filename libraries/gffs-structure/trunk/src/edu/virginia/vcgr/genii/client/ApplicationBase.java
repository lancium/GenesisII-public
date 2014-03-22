package edu.virginia.vcgr.genii.client;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.ConnectTool;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.GridEnvironment;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.logging.LoggingContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.KeystoreManager;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.osgi.OSGiSupport;
import edu.virginia.vcgr.genii.security.CertificateValidatorFactory;
import edu.virginia.vcgr.genii.security.utils.SecurityUtilities;

public class ApplicationBase
{
	static private Log _logger = LogFactory.getLog(ApplicationBase.class);

	// the optional environment variable that points at the installation location.
	// this becomes required if one is running the software under eclipse.
	static public final String INSTALLATION_DIR_ENVIRONMENT_VARIABLE = "GENII_INSTALL_DIR";

	// the environment variable whose value points at our state directory.
	static public final String USER_DIR_ENVIRONMENT_VARIABLE = "GENII_USER_DIR";

	// environment variable used to point at a different deployment than the one found in the
	// installation directory.
	static public final String DEPLOYMENT_DIR_ENVIRONMENT_VARIABLE = "GENII_DEPLOYMENT_DIR";

	// this property value can be used in certain property files as a placeholder that is
	// translated into the value of the user state directory variable (see above).
	static public final String USER_DIR_PROPERTY_VALUE = "env-GENII_USER_DIR";

	/**
	 * a simple search for the deployment directory in the environment variables. this does not have
	 * a default value implemented here, and must fall-back to a default elsewhere.
	 */
	static public String getDeploymentDirFromEnvironment()
	{
		return System.getenv(DEPLOYMENT_DIR_ENVIRONMENT_VARIABLE);
	}

	/*
	 * this provides a way for the osgi support to know we're running inside eclipse, which imposes
	 * a different structure on the locations of files.
	 */
	static public String getEclipseTrunkFromEnvironment()
	{
		return System.getenv(INSTALLATION_DIR_ENVIRONMENT_VARIABLE);
	}

	static public void setupUserDir(File userdir)
	{
		if (!userdir.exists()) {
			try {
				File userDirFile = new GuaranteedDirectory(userdir.getAbsolutePath(), true);
				if (!userDirFile.isDirectory()) {
					throw new RuntimeException("Path \"" + userdir.getAbsolutePath() + "\" is not a directory.");
				}
			} catch (Throwable e) {
				throw new RuntimeException("Unable to create directory \"" + userdir.getAbsolutePath() + "\".");
			}
		}
	}

	/**
	 * Prepares the static configuration manager.
	 */
	static protected void prepareServerApplication() throws AuthZSecurityException
	{
		LoggingContext.assumeNewLoggingContext();

		if (!OSGiSupport.setUpFramework()) {
			System.err.println("Exiting due to OSGi startup failure.");
			System.exit(1);
		}
		SecurityUtilities.initializeSecurity();

		CertificateValidatorFactory.setValidator(new SecurityUtilities(KeystoreManager.getResourceTrustStore()));

		GridEnvironment.loadGridEnvironment();

		ContainerStatistics.instance();

		ContainerProperties cProperties = ContainerProperties.getContainerProperties();
		String depName = cProperties.getDeploymentName();
		if (depName != null)
			System.setProperty(DeploymentName.DEPLOYMENT_NAME_PROPERTY, depName);
		String userDir = InstallationProperties.getUserDir();
		ConfigurationManager configurationManager = ConfigurationManager.initializeConfiguration(userDir);
		setupUserDir(configurationManager.getUserDirectory());
		configurationManager.setRoleServer();
	}

	/**
	 * Prepares the static configuration manager (using the config files in the explicitConfigDir).
	 * If explicitConfigDir is null, the GENII_CONFIG_DIR is inspected. If that is not present (or
	 * empty), the default configuration location located in a well-known spot from the installation
	 * directory (as per the installation system property) is used.
	 */
	static protected void prepareClientApplication()
	{
		String userDir = InstallationProperties.getUserDir();
		ConfigurationManager configurationManager = ConfigurationManager.initializeConfiguration(userDir);
		setupUserDir(configurationManager.getUserDirectory());
		configurationManager.setRoleClient();
	}

	public enum GridStates {
		// we were not connected, and we knew what to do, but that failed.
		CONNECTION_FAILED,
		// no breadcrumbs were left for how to get connected.
		CONNECTION_MEANS_UNKNOWN,
		// connection to the grid was already okay.
		CONNECTION_ALREADY_GOOD,
		// there was no connection, but we have established one. shell must reload.
		CONNECTION_GOOD_NOW
	}

	static public GridStates establishGridConnection(Writer output, Writer error, Reader input)
	{
		ICallingContext callContext = null;
		try {
			callContext = ContextManager.getCurrentContext();
			if (callContext == null)
				callContext = new CallingContextImpl(new ContextType());
		} catch (Throwable e) {
			_logger.error("could not load or create calling context.");
		}
		if (callContext == null) {
			_logger.error("failed to build calling context.");
			return GridStates.CONNECTION_MEANS_UNKNOWN;
		}

		RNSPath currdir = callContext.getCurrentPath();
		if (currdir != null)
			return GridStates.CONNECTION_ALREADY_GOOD;

		String connectCmd = ContainerProperties.getContainerProperties().getConnectionCommand();
		if (_logger.isDebugEnabled())
			_logger.debug("grid connection command is: " + connectCmd);
		if ((connectCmd == null) || connectCmd.isEmpty()) {
			if (_logger.isDebugEnabled())
				_logger.debug("Did not find grid connection property; unknown how to get on grid.");
			return GridStates.CONNECTION_MEANS_UNKNOWN;
		}
		if (_logger.isDebugEnabled())
			_logger.debug("trying grid connection with parameters: " + connectCmd);

		// split up our line which is expected to be two quoted strings. those are our arguments.
		Pattern quoter = Pattern.compile("\" \"");
		String[] parameters = quoter.split(connectCmd, 2);
		if (parameters.length != 2) {
			_logger.error("did not find the grid connection command line in the proper format.  bailing out.");
			return GridStates.CONNECTION_MEANS_UNKNOWN;
		}
		parameters[0] = parameters[0].substring(1); // take off initial quote on first parm.
		parameters[1] = parameters[1].substring(0, parameters[1].length() - 1); // take off last
																				// quote on second
																				// parm.
		if (_logger.isDebugEnabled())
			_logger.debug("got arguments: [0]=" + parameters[0] + " [1]=" + parameters[1]);

		ConnectTool ct = new ConnectTool();
		try {
			for (int i = 0; i < parameters.length; i++)
				ct.addArgument(parameters[i]);
			int retVal = ct.run(output, error, input);
			if (retVal != 0)
				_logger.error("grid connection failed with return value=" + retVal);
		} catch (ReloadShellException e) {
			if (_logger.isDebugEnabled())
				_logger.debug("got newly connected; reloading grid shell");
			return GridStates.CONNECTION_GOOD_NOW;
		} catch (Throwable e) {
			// issue already printed by BaseGridTool.
		}
		return GridStates.CONNECTION_FAILED;
	}

	// loads the value for the genesis user state directory from the environment.
	static String getUserDirFromEnvironment()
	{
		return System.getenv(USER_DIR_ENVIRONMENT_VARIABLE);
	}

	// a default for the state directory, if one cannot be found elsewhere.
	static String getDefaultUserDir()
	{
		return String.format("%s/%s", System.getProperty("user.home"), GenesisIIConstants.GENESISII_STATE_DIR_NAME);
	}

	/**
	 * supports replacing a few keywords (or one really, currently) with environment variables.
	 * 
	 * (this must not use InstallationProperties.getUserDir, since that is based on this.)
	 */
	public static String replaceKeywords(String pathToFix)
	{
		// test for well-known singular replacements first.
		if ((pathToFix != null) && pathToFix.equals(ApplicationBase.USER_DIR_PROPERTY_VALUE)) {
			// there's our sentinel for loading the state directory from the environment variables.
			// let's try to load it.
			pathToFix = ApplicationBase.getUserDirFromEnvironment();
			if (pathToFix != null)
				return pathToFix;
			// nothing in environment, so fall back to default state directory, since we know this.
			return ApplicationBase.getDefaultUserDir();
		}
		// test for generalized "env-NAME" patterns for other environment variables.
		/*
		 * no other env variables implemented yet.
		 */
		return pathToFix;
	}
}
