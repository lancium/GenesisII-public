package edu.virginia.vcgr.genii.client.security;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.InstallationProperties;

/**
 * provides support for an interprocess file lock that is also thread safe, which is more complex
 * since java's FileLock objects do not properly handle multi-threading, even with separate FileLock
 * objects.  this currently only supports locking one specific consistency lock file.
 */
public class ThreadAndProcessSynchronizer
{
	static private Log _logger = LogFactory.getLog(CertUpdateHelpers.class);

	static volatile Object _threadSynchronizer = new Object();
	static volatile FileChannel _fileLocker = null;

	// filename used for consistency locking (when managing the certificate update properties).
	static public final String CONSISTENCY_LOCK_FILE = "consistency.lock";

	/**
	 * locks the consistency lock file in a thread-safe and process-safe manner. any call to
	 * acquireLock *must* be followed eventually by a call to releaseLock.
	 */
	static public void acquireLock()
	{
		while (true) {
			synchronized (_threadSynchronizer) {
				if (_fileLocker == null) {
					// it seems that we can safely grab the lock, unless someone else has it.
					FileChannel fc = lockConsistencyFile();
					if (fc != null) {
						_fileLocker = fc;
						return;
					}
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// we'll just try again in next loop.
			}
		}
	}

	/**
	 * unlocks the consistency lock file from a previously held lock.
	 */
	static public void releaseLock()
	{
		synchronized (_threadSynchronizer) {
			if (_fileLocker == null) {
				String msg = "severe problem: a releaseLock was attempted with no existing lock";
				_logger.error(msg);
				throw new RuntimeException(msg);
			}
			unlockConsistencyFile(_fileLocker);
			_fileLocker = null;
		}
	}

	/**
	 * returns the file name of the consistency lock file that is used for upgrading certificates.
	 */
	private static File getConsistencyLockFile()
	{
		String stateDir = InstallationProperties.getUserDir();
		File lockFile = new File(stateDir + "/" + CONSISTENCY_LOCK_FILE);
		return lockFile;
	}

	/**
	 * uses the FileChannel and FileLock support of nio to lock the file. the lock is held until the
	 * returned FileChannel is closed or the program exits.
	 */
	private static FileChannel lockConsistencyFile()
	{
		File lockFile = getConsistencyLockFile();
		_logger.debug("consistency file is: " + lockFile);
		FileSystem fs = FileSystems.getDefault();
		Path fp = fs.getPath(lockFile.getAbsolutePath());
		_logger.debug("path object for that file is: " + fp.toAbsolutePath());
		FileChannel fc = null;
		try {
			_logger.debug("about to try opening file");
			fc = FileChannel.open(fp, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
			_logger.debug("opened consistency file okay");
		} catch (IOException e) {
			_logger.error("failed to open consistency lock file for cert update properties", e);
			return null;
		}
		try {
			_logger.debug("about to lock consistency file");
			fc.lock();
			_logger.debug("locked consistency file okay");
		} catch (IOException e) {
			_logger.error("failed to lock consistency lock for cert update properties", e);
			try {
				fc.close();
			} catch (Exception e2) {
			}
			return null;
		}
		return fc;
	}

	/**
	 * unlocks a lock held on a file.
	 */
	private static void unlockConsistencyFile(FileChannel toUnlock)
	{
		if (toUnlock == null) {
			_logger.error("null passed in for FileChannel to unlock");
			return;
		}
		try {
			toUnlock.close();
		} catch (IOException e) {
			_logger.error("failed to close consistency lock file", e);
		}
	}

}
