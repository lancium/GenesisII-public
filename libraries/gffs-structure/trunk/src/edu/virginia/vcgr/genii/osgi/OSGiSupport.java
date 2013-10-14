package edu.virginia.vcgr.genii.osgi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
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

	// OSGi framework management.
	static private Framework _framework;
	static private String bundleDir;

	/**
	 * starts up the OSGi framework and loads the bundles required by our application.
	 * 
	 * @return true on success of loading bundles and startup
	 */
	static public Boolean setUpFramework()
	{
		/*
		 * we are trying to build both a user-specific and installation-specific storage area for
		 * the OSGi bundles at run-time. this folder can get "corrupted" if the installer has
		 * upgraded the installation, in that eclipse equinox OSGi won't load. this can be solved
		 * manually by cleaning out the storage area, but that's pretty crass. instead, we will try
		 * to clean it out once, and if that fails, then we really do need to fail.
		 */
		String username = System.getProperty("user.name");
		URL url = OSGiSupport.class.getProtectionDomain().getCodeSource().getLocation();
		File pathChow = new File(url.getPath());
		if (_logger.isTraceEnabled())
			_logger.trace("gotta path of: " + pathChow);
		String justDir = pathChow.getParent().replace('/', '-');
		// let's not forget ugly paths windows might hand us.
		justDir = justDir.replace('\\', '-');
		justDir = justDir.replace(':', '-');
		if (_logger.isTraceEnabled())
			_logger.trace("gotta chopped path of: " + justDir);
		String tmpDir = System.getProperty("java.io.tmpdir");
		tmpDir = tmpDir.replace('\\', '/');
		File osgiStorageDir = new File(tmpDir + "/osgi-genII-" + username + "/" + justDir);
		if (_logger.isDebugEnabled())
			_logger.debug("osgi storage area is: " + osgiStorageDir.getAbsolutePath());
		osgiStorageDir.mkdirs();

		// see if we're running under eclipse or know our installation directory.
		String bundleSourcePath = ApplicationBase.getEclipseTrunkFromEnvironment();
		String saveDrive = ""; // only used for windows.
		if (bundleSourcePath != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("install-dir-based startup bundle path: " + bundleSourcePath);
			if (bundleSourcePath.charAt(1) == ':') {
				// we have a dos path again, let's save the important bits.
				saveDrive = bundleSourcePath.substring(0, 2);
			}
		} else {
			// okay, that was a bust. see if we can intuit our location from living in a jar.
			bundleSourcePath = url.getPath();
			if (_logger.isTraceEnabled())
				_logger.trace("got source path as: " + bundleSourcePath);
			if (bundleSourcePath.endsWith(".jar")) {
				// we need to chop off the jar file part of the name.
				int lastSlash = bundleSourcePath.lastIndexOf("/");
				bundleSourcePath = bundleSourcePath.substring(0, lastSlash);
				if (_logger.isTraceEnabled())
					_logger.trace("truncated path since inside jar: " + bundleSourcePath);
			}
			if (bundleSourcePath.charAt(2) == ':') {
				// this is most likely a DOS path.
				if (bundleSourcePath.charAt(0) == '/') {
					bundleSourcePath = bundleSourcePath.substring(1);
					// keep track of the drive letter on windows.
					saveDrive = bundleSourcePath.substring(0, 2);
				}
			}
			bundleSourcePath = bundleSourcePath.concat("/..");
			if (_logger.isDebugEnabled())
				_logger.debug("jar-intuited startup bundle path: " + bundleSourcePath);
		}

		bundleDir = bundleSourcePath + "/bundles";
		try {
			bundleDir = new URI(bundleDir).normalize().getPath();
		} catch (URISyntaxException e) {
			_logger.warn("failure to normalize path to bundles.", e);
		}
		if (saveDrive.length() > 0) {
			// concatenate drive letter if we had figured that out.
			bundleDir = saveDrive + bundleDir;
			// on windows we must make the case identical or eclipse has all sorts of problems from
			// mismatches.
			bundleDir = bundleDir.toLowerCase();
		}

		Map<String, String> config = new HashMap<String, String>();

		// Control where OSGi stores its persistent data:
		config.put(Constants.FRAMEWORK_STORAGE, osgiStorageDir.getAbsolutePath());

		// Request OSGi to clean its storage area on startup
		config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "false");

		config.put("osgi.install.area", bundleDir);
		if (_logger.isDebugEnabled())
			_logger.debug("using bundle source at: " + bundleDir);

		/*
		 * could enable this if we want a remote console to manage OSGi: config.put("osgi.console",
		 * "4228");
		 */

		ArrayList<Bundle> loadedBundles = new ArrayList<Bundle>();

		BundleContext context = initializeFrameworkFactory(config, loadedBundles);
		if (context == null) {
			_logger.warn("first attempt to start OSGi failed; now retrying with a clean-up step.");
			try {
				FileUtils.deleteDirectory(osgiStorageDir);
			} catch (IOException e) {
				// if we can't clean up that directory, we can't fix this problem.
				_logger.error("failed to clean up the OSGi storage area: " + osgiStorageDir.getAbsolutePath());
				return false;
			}
			osgiStorageDir.mkdirs();
			loadedBundles.clear();
			context = initializeFrameworkFactory(config, loadedBundles);
			if (context != null) {
				_logger.info("recovered from ill OSGi storage area.  all is well.");
			}
		}
		if (context == null) {
			_logger.error("second attempt to start OSGi failed after cleanup; bailing out.");
			return false;
		}

		// now start all of the bundles.
		for (Bundle bun : loadedBundles) {
			if (_logger.isTraceEnabled())
				_logger.trace("starting bundle: " + bun.getSymbolicName());
			try {
				if (bun.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
					bun.start();
				}
			} catch (Throwable e) {
				_logger.error("failed to start bundle: " + bun.getSymbolicName(), e);
				return false;
			}
		}

		return true;
	}

	/**
	 * a simple wrapper to try to get the framework running. if this fails, null is returned.
	 */
	static private BundleContext initializeFrameworkFactory(Map<String, String> config, ArrayList<Bundle> loadedBundles)
	{
		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();
		_framework = frameworkFactory.newFramework(config);
		try {
			_framework.init();
			_framework.start();
		} catch (Throwable cause) {
			_logger.error("failed to load framework factory for OSGi", cause);
			shutDownFramework();
			return null;
		}

		BundleContext context = _framework.getBundleContext();
		// load all of our bundles.
		for (String bunName : OSGiConstants.genesis2ApplicationBundleList) {
			if (_logger.isTraceEnabled())
				_logger.trace("loading bundle: " + bunName);
			Bundle currentBundle = null;
			try {
				currentBundle = context.installBundle("file:" + bundleDir + "/" + bunName);
			} catch (Throwable e) {
				_logger.error("failed to load bundle: " + bunName, e);
				shutDownFramework();
				return null;
			}
			loadedBundles.add(currentBundle);
		}

		return context;
	}

	/**
	 * stops the OSGi framework, which should be invoked if and only if program is shutting down.
	 */
	static public void shutDownFramework()
	{
		if (_framework != null) {
			try {
				_framework.getBundleContext().getBundle(0).stop();
				_framework.waitForStop(0);
			} catch (Throwable cause) {
				_logger.error("OSGi framework shutdown exception", cause);
			}
			_framework = null;
		}

	}

}
