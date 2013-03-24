package edu.virginia.vcgr.genii.client.jni.gIIlib;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import edu.virginia.vcgr.fsii.FSFilesystem;
import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.path.FilesystemPathRepresentation;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import edu.virginia.vcgr.genii.client.gfs.cache.GenesisIICachedFilesystem;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.FilesystemHandle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class JNILibraryBase extends ApplicationBase
{
	static private Log _logger = LogFactory.getLog(JNILibraryBase.class);

	public static boolean USE_CACHE_FS = false;
	public static boolean isInitialized = false;
	public static final boolean DEBUG = true;

	static private FilesystemPathRepresentation PATHREP = UnixFilesystemPathRepresentation.INSTANCE;
	static private FSFilesystem _fs = null;
	static private FileHandleTable<FilesystemHandle> _openHandles = new FileHandleTable<FilesystemHandle>(1024);

	// Members for the mirroring test harness

	synchronized static public void tryToInitialize()
	{
		boolean didInit = false;
		if (!isInitialized) {
			initialize();
			didInit = true;
		}

		ICallingContext callingContext;
		try {
			callingContext = ContextManager.getExistingContext();
			ClientUtils.checkAndRenewCredentials(callingContext, new Date(), new SecurityUpdateResults());

			if (didInit) {
				if (USE_CACHE_FS) {
					_fs = new GenesisIICachedFilesystem(
						new GenesisIIFilesystem(callingContext.getCurrentPath().getRoot(), null));
				} else {
					_fs = new GenesisIIFilesystem(callingContext.getCurrentPath().getRoot(), null);
				}
			}
		} catch (Exception e) {
			_logger.info("JNILibraryError:  Problem with relogin", e);
		}
	}

	static private void initialize()
	{
		try {
			prepareClientApplication();
			isInitialized = true;
		} catch (RuntimeException e) {
			_logger.info("Application already started", e);
		}
	}

	/**
	 * Cleans up paths so that they are all consistent (important for Caching)
	 */
	static private String cleanupPath(String path)
	{
		String newPath = path;

		// All root directory pointers are '/'
		if (path == null || path.equals("") || path.equals("/"))
			newPath = "/";

		// All paths are absolute
		if (!newPath.startsWith("/"))
			newPath = "/" + newPath;

		// No paths end with '/'
		if (newPath.length() > 1 && newPath.endsWith("/"))
			newPath = newPath.substring(0, newPath.lastIndexOf('/'));

		return newPath;
	}

	static public String[] convertPath(String path)
	{
		if (!isValidPath(path))
			return null;
		path = cleanupPath(path);
		tryToInitialize();

		return PATHREP.parse(null, path);
	}

	static private Pattern DESKTOP_INI_PAT = Pattern.compile("^.*desktop\\.ini$", Pattern.CASE_INSENSITIVE);

	/**
	 * Checks for valid paths (filters out commonly unused names)
	 * 
	 * @param path
	 * @return
	 */
	static public boolean isValidPath(String path)
	{
		if (path != null) {
			if (path.contains(":") || DESKTOP_INI_PAT.matcher(path).matches()) {
				if (JNILibraryBase.DEBUG)
					System.out.println("GENESIS:  Path filtered out: " + path);
				return false;
			} else if (path.endsWith("Thumbs.db"))
				return false;
		}

		return true;
	}

	static protected FSFilesystem getFilesystem()
	{
		return _fs;
	}

	static protected FileHandleTable<FilesystemHandle> openHandles()
	{
		return _openHandles;
	}

	static public String toString(ArrayList<String> array, int start, int length)
	{
		if (array == null)
			return "null";

		StringBuilder builder = null;
		for (int lcv = start; lcv < start + length; lcv++) {
			String entry = array.get(lcv);

			if (builder == null)
				builder = new StringBuilder("{ ");
			else
				builder.append(", ");

			builder.append(entry);
		}

		builder.append(" }");
		return builder.toString();
	}

	static public String toString(ArrayList<String> array)
	{
		if (array == null)
			return "null";

		return toString(array, 0, array.size());
	}
}
