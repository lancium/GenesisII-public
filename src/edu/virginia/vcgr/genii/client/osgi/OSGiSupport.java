package edu.virginia.vcgr.genii.client.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.GuaranteedDirectory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import edu.virginia.vcgr.genii.client.ApplicationBase;

/**
 * provides the OSGi bundle loading and startup for the application.
 * 
 * @author Chris Koeritz
 */
public class OSGiSupport
{
	static private Log _logger = LogFactory.getLog(OSGiSupport.class);

	// osgi framework management.
	static private Framework framework;

	/**
	 * starts up the OSGi framework and loads the bundles required by our application.
	 * 
	 * @return true on success of loading bundles and startup
	 */
	static public Boolean setUpFramework()
	{
		String stateDir = ApplicationBase.getUserDirFromEnvironment();
		// make sure the state directory exists and has proper permissions.
		File makingDir;
		String errorMsg = "failed to create user state directory directory.";
		try {
			makingDir = new GuaranteedDirectory(stateDir, true);
			if (!makingDir.exists()) {
				_logger.error(errorMsg);
			}
		} catch (IOException e) {
			_logger.error(errorMsg);
		}

		String osgiDir = ApplicationBase.getOSGIDirFromEnvironment();
		if (osgiDir == null)
			osgiDir = stateDir + "/osgi_storage";

		URL url = OSGiSupport.class.getProtectionDomain().getCodeSource().getLocation();
		String path = url.getPath();
		if (path.endsWith(".jar")) {
			// we need to chop off the client jar file in the name.
			int lastSlash = path.lastIndexOf("/");
			path = path.substring(0, lastSlash);
		}
		_logger.info("path for code running is: " + path);
		String geniiInstallDir = path + "/..";

		String bundleDir = geniiInstallDir + "/bundles";

		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
		Map<String, String> config = new HashMap<String, String>();

		// Control where OSGi stores its persistent data:
		config.put(Constants.FRAMEWORK_STORAGE, osgiDir);

		// Request OSGi to clean its storage area on startup
		config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");

		config.put("osgi.install.area", bundleDir);
		_logger.info("using bundle source at: " + bundleDir);

		// could enable this if we want a remote console to manage osgi: config.put("osgi.console",
		// "4228");

		framework = frameworkFactory.newFramework(config);
		try {
			framework.start();
		} catch (Throwable cause) {
			_logger.warn("failed to load framework factory for OSGI", cause);
			return false;
		}
		BundleContext context = framework.getBundleContext();

		String loadingBundle = "";
		List<Bundle> installedBundles = new LinkedList<Bundle>();
		try {
			// load all of our bundles.
			for (String bun : OSGiConstants.genesis2ApplicationBundleList) {
				loadingBundle = bun;
				installedBundles.add(context.installBundle("file:" + bundleDir + "/" + loadingBundle));
			}
			loadingBundle = "and start installed bundles";
			for (Bundle bundle : installedBundles) {
				if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) == null)
					bundle.start();
			}
		} catch (BundleException cause) {
			_logger.error("failed to load " + loadingBundle, cause);
			return false;
		}

		return true;
	}

	/**
	 * stops the OSGi framework, which should be invoked if and only if program is shutting down.
	 */
	static public void shutDownFramework()
	{

		if (framework != null) {
			try {
				framework.getBundleContext().getBundle(0).stop();
				framework.waitForStop(0);
			} catch (Throwable cause) {
				_logger.error("osgi framework shutdown exception", cause);
			}
		}

	}

}
