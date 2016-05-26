package edu.virginia.vcgr.genii.client.security;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileSystemHelper;
import edu.virginia.vcgr.genii.client.InstallationProperties;

/**
 * provides support for an interprocess file lock that is also thread safe, which is more complex since java's FileLock objects do not
 * properly handle multi-threading, even with separate FileLock objects.
 */
public class ThreadAndProcessSynchronizer
{
	static private Log _logger = LogFactory.getLog(ThreadAndProcessSynchronizer.class);

	/**
	 * Holds onto the information needed for locking files.
	 */
	static class SynchPackage
	{
		public String _lockFile = null;
		public volatile FileChannel _fileLocker = null;

		SynchPackage(String lockFile)
		{
			_lockFile = lockFile;
		}
	}

	/*
	 * holds onto records for all file locks, and also serves as the object our critical section is based on.
	 */
	static volatile HashMap<String, SynchPackage> _lockRecords = new HashMap<String, SynchPackage>();

	/**
	 * locks the consistency lock file in a thread-safe and process-safe manner. any call to acquireLock *must* be followed eventually by a
	 * call to releaseLock.
	 */
	static public void acquireLock(String lockFile)
	{
		while (true) {
			synchronized (_lockRecords) {
				SynchPackage found = _lockRecords.get(lockFile);
				if (found == null) {
					if (_logger.isTraceEnabled())
						_logger.debug("acquireLock: creating new synch package for lock file: " + lockFile);
					found = new SynchPackage(lockFile);
					_lockRecords.put(lockFile, found);
				}

				if (found._fileLocker == null) {
					// it seems that we can safely grab the lock, unless someone else has it.
					FileChannel fc = lockConsistencyFile(found);
					if (fc != null) {
						found._fileLocker = fc;
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
	static public void releaseLock(String lockFile)
	{
		synchronized (_lockRecords) {
			SynchPackage found = _lockRecords.get(lockFile);
			if (found == null) {
				String msg = "attempted release on lockfile that hasn't been locked first: " + lockFile;
				_logger.error(msg);
				throw new RuntimeException(msg);
			}
			if (found._fileLocker == null) {
				String msg = "severe problem: a releaseLock was attempted with no existing lock";
				_logger.error(msg);
				throw new RuntimeException(msg);
			}
			unlockConsistencyFile(found);
			found._fileLocker = null;
		}
	}

	/*
	 * hmmm: these (getConsistencyLockFile, lockConsistencyFile and unlockConsistencyFile) should move to a more certificatey area, since
	 * these are only about certificates.
	 */
	/**
	 * returns the file name of the consistency lock file that is used for upgrading certificates.
	 */
	private static File getConsistencyLockFile(String lockFile)
	{
		lockFile = FileSystemHelper.sanitizeFilename(lockFile);
		if (lockFile.startsWith("/")) {
			// this looks like an absolute path, so we use it directly.
			return new File(lockFile);
		} else if (lockFile.substring(1, 2).equals(":")) {
			// looks like a windows path, so we'll use that directly also.
			return new File(lockFile);
		}
		String stateDir = InstallationProperties.getUserDir();
		return new File(stateDir + "/" + lockFile);
	}

	/**
	 * uses the FileChannel and FileLock support of nio to lock the file. the lock is held until the returned FileChannel is closed or the
	 * program exits.
	 */
	private static FileChannel lockConsistencyFile(SynchPackage lockPack)
	{
		File lockFile = getConsistencyLockFile(lockPack._lockFile);
		if (_logger.isTraceEnabled())
			_logger.debug("consistency file is: " + lockFile);
		FileSystem fs = FileSystems.getDefault();
		Path fp = fs.getPath(lockFile.getAbsolutePath());
		FileChannel fc = null;
		try {
			fc = FileChannel.open(fp, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.WRITE));
		} catch (IOException e) {
			_logger.error("failed to open consistency lock file for cert update properties", e);
			return null;
		}
		try {
			fc.lock();
			if (_logger.isTraceEnabled())
				_logger.debug("locked consistency file '" + lockPack._lockFile + "'");
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
	private static void unlockConsistencyFile(SynchPackage lockPack)
	{
		FileChannel toUnlock = lockPack._fileLocker;
		if (toUnlock == null) {
			_logger.error("null passed in for FileChannel to unlock");
			return;
		}
		try {
			toUnlock.close();
			if (_logger.isTraceEnabled())
				_logger.debug("unlocked consistency file '" + lockPack._lockFile + "'");
		} catch (IOException e) {
			_logger.error("failed to close consistency lock file", e);
		}
	}

}
