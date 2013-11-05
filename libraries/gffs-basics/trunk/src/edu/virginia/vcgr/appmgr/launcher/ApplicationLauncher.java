package edu.virginia.vcgr.appmgr.launcher;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.version.Version;
import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

// import edu.virginia.vcgr.appmgr.update.Updater;

public class ApplicationLauncher
{
	static private Log _logger = LogFactory.getLog(ApplicationLauncher.class);

	static private final String APPLICATION_DESCRIPTION_PROPERTY = "edu.virginia.vcgr.appwatch.application-description";
	// static private final String UPDATE_FREQUENCY_PROPERTY =
	// "edu.virginia.vcgr.appwatcher.update-frequency";
	static private final String APPLICATION_CLASS_FLAG_PREFIX = "--application-class=";

	static private String _appNameFound = null;
	static private Version _appVersionFound = null;

	static private InheritableThreadLocal<ApplicationLauncherConsole> _console =
		new InheritableThreadLocal<ApplicationLauncherConsole>();

	static public ApplicationLauncherConsole getConsole()
	{
		return _console.get();
	}

	static private void usage(int exitCode)
	{
		System.err.format("USAGE:  ApplicationLauncher [%sapplication-class]\n"
			+ "	[-Dedu.virginia.vcgr.appwatch.application-description=]<application-description> " + "[application-args]\n",
			APPLICATION_CLASS_FLAG_PREFIX);
		System.exit(exitCode);
	}

	// static private void doUpdates(PrintStream log, ApplicationDescription appDesc)
	// {
	// boolean updatesEnabled = false;
	// if (!updatesEnabled) {
	// _logger.debug("Update mechanism disabled; deprecated feature.");
	// return;
	// }
	// try {
	// Updater updater = new Updater(appDesc);
	// updater.doUpdates(log);
	// } catch (Throwable cause) {
	// _logger.error("Unable to patch system.  Continuing with old version.");
	// }
	// }

	static private int runApplication(ApplicationDescription appDesc, String[] appArgs)
	{
		try {
			JarDescription description = new JarDescription(appDesc.getJarDescriptionFile());
			ClassLoader loader = description.createClassLoader();
			Thread.currentThread().setContextClassLoader(loader);
			GenesisClassLoader.classLoaderFactory().addLoader(loader);
			Class<?> cl = loader.loadClass(appDesc.getApplicationClassName());
			Method main = cl.getMethod("main", new Class[] { String[].class });
			main.invoke(null, new Object[] { appArgs });
		} catch (Throwable cause) {
			_logger.error("Error running application.", cause);
			return 1;
		}

		return 0;
	}

	static public String getAppName()
	{
		return _appNameFound;
	}

	static public Version getAppVersion()
	{
		return _appVersionFound;
	}

	static public void main(String[] args)
	{
		_logger.trace("entry => into main for ApplicationLauncher...");
		String appClass = null;
		int next = 0;

		try {
			if (args.length > 0 && args[0].startsWith(APPLICATION_CLASS_FLAG_PREFIX)) {
				appClass = args[0].substring(APPLICATION_CLASS_FLAG_PREFIX.length());
				next = 1;
			}

			String appDescFile = System.getProperty(APPLICATION_DESCRIPTION_PROPERTY);
			if (appDescFile == null) {
				if (next > args.length)
					usage(1);
				appDescFile = args[next++];
			}

			String[] appArgs = new String[args.length - next];
			System.arraycopy(args, next, appArgs, 0, appArgs.length);

			ApplicationDescription appDesc = new ApplicationDescription(appClass, appDescFile);
			_appNameFound = appDesc.getApplicationName();
			ApplicationLauncherConsole console = new ApplicationLauncherConsoleImpl(appDesc);
			_console.set(console);

			// if (appDesc.updateDisabled())
			// _logger.debug("Updates Disabled");
			// else {
			// long updateFrequency = Long.parseLong(System.getProperty(UPDATE_FREQUENCY_PROPERTY,
			// "-1"));
			// Version v = appDesc.getVersionManager().getCurrentVersion();
			// _appVersionFound = v;
			// if (!Version.EMPTY_VERSION.equals(v) || appDesc.isUpdateRequest()) {
			// _logger.info(String.format("Current version is %s.", v));
			// Calendar now = Calendar.getInstance();
			// Calendar lastUpdated = appDesc.getVersionManager().getLastUpdated();
			// if (updateFrequency < 0 || appDesc.isUpdateRequest()
			// || (now.getTimeInMillis() - lastUpdated.getTimeInMillis()) > updateFrequency) {
			// _logger.debug("Checking to see if updates exist.");
			// doUpdates(System.out, appDesc);
			// }
			// }

			// if (appDesc.isUpdateRequest())
			// System.exit(0);
			// }
			int result = runApplication(appDesc, appArgs);
			if (result != 0)
				System.exit(result);
		} catch (Throwable cause) {
			_logger.error("Unable to launch application.", cause);
			System.exit(1);
		}
	}

	static private class ApplicationLauncherConsoleImpl implements ApplicationLauncherConsole
	{
		private ApplicationDescription _appDesc;

		private ApplicationLauncherConsoleImpl(ApplicationDescription appDesc)
		{
			_appDesc = appDesc;
		}

		// @Override
		// public boolean doUpdates(PrintStream log)
		// {
		// try {
		// Version v = _appDesc.getVersionManager().getCurrentVersion();
		//
		// if (!Version.EMPTY_VERSION.equals(v)) {
		// _logger.info(String.format("Current version is %s.", v));
		// _logger.debug("Checking for updates.");
		// ApplicationLauncher.doUpdates(log, _appDesc);
		// }
		//
		// return true;
		// } catch (IOException ioe) {
		// _logger.error("Unable to update application...try again later.");
		// return false;
		// }
		// }

		// @Override
		// public Calendar lastUpdated()
		// {
		// try {
		// return _appDesc.getVersionManager().getLastUpdated();
		// } catch (Throwable cause) {
		// _logger.error("Unable to obtain last update time.", cause);
		// return null;
		// }
		// }

		@Override
		public Version currentVersion()
		{
			try {
				return _appDesc.getVersionManager().getCurrentVersion();
			} catch (Throwable cause) {
				return Version.EMPTY_VERSION;
			}
		}

	}
}
