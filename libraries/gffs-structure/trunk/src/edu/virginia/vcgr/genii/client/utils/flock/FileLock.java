package edu.virginia.vcgr.genii.client.utils.flock;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.osgi.OSGiSupport;

/**
 * warning: this class only supports process synchronization and does not handle thread locking in same application.
 * 
 * hmmm: probably should get similar implementation to ThreadAndProcessSynchronizer. hmmm: can use the T&PSync class directly if we add notion
 * of max attempts and polling interval to that class.
 */
public class FileLock implements Closeable
{
	static private Log _logger = LogFactory.getLog(FileLock.class);

	private FileOutputStream _internalFile = null;
	private java.nio.channels.FileLock _internalLock = null;

	/**
	 * Creates a file lock that can be used for both reading and writing (by creating a new file based on the real file's name but ending in
	 * ".lock" in the state directory).
	 * 
	 * @param fileToLock
	 *            The file to create a lock for.
	 * @param maxAttempts
	 *            The maximum number of attempts to make at locking before giving up
	 * @param pollInterval
	 *            The interval (in milliseconds) to wait between attempts to lock the file before giving up. This value can be 0 indicating
	 *            infinity).
	 * @throws FileLockException
	 */
	public FileLock(File fileToLock, int maxAttempts, long pollInterval) throws FileLockException, InterruptedException
	{
		File lockFile = determineLockfileName(fileToLock);

		try {
			_internalFile = new FileOutputStream(lockFile);

			while (maxAttempts > 0) {
				try {
					if (pollInterval > 0)
						_internalLock = _internalFile.getChannel().tryLock();
					else
						_internalLock = _internalFile.getChannel().lock();

					if (_internalLock != null)
						return;
				} catch (IOException ioe) {
					// Error locking the file. Sleep and try again.
					Thread.sleep(pollInterval);
				}
			}

			try {
				_internalFile.close();
			} catch (Throwable cause) {
			}
			throw new FileLockException("Unable to create lock for file '" + fileToLock + "' using lock file '" + lockFile + "'.");
		} catch (IOException ioe) {
			throw new FileLockException("Unable to create lock for '" + fileToLock + "' using lock file '" + lockFile + "'.", ioe);
		}
	}

	protected void finalize() throws IOException
	{
		close();
	}

	static private File determineLockfileName(File file)
	{
		File toReturn = null;

		String path = file.getAbsolutePath();
		if (path.startsWith(System.getProperty("user.home"))) {
			/*
			 * special case for files in known home folder; we will make the lock right there, since that should work. assumption is that the
			 * file really should be fixed in that location, which means file locking could be cross-container, so the state dir is not an
			 * appropriate place to store the lock file.
			 */
			toReturn = new File(path + ".lock");
		} else {
			/*
			 * more standard case of a file somewhere in the file system, and where we do not expect to ever need to share the lock with a
			 * different container. it's just too bad if we do need to share the lock file with a different container for most arbitrary
			 * paths, since we cannot guarantee we can create the lock file in read-only locations such as the system-wide install directory.
			 */
			toReturn = OSGiSupport.chopUpPath(InstallationProperties.getUserDir(), new File(file.getAbsolutePath() + ".lock"), "flock");
		}
		if (_logger.isDebugEnabled())
			_logger.debug("lock file calculated as: '" + toReturn.getAbsolutePath() + "'");
		return toReturn;
	}

	@Override
	synchronized public void close() throws IOException
	{
		if (_internalLock != null) {
			try {
				_internalLock.release();
			} catch (Throwable cause) {
			}
			try {
				_internalFile.close();
			} catch (Throwable cause) {
			}
			_internalLock = null;
		}
	}

	static private final int DEFAULT_MAX_LOCK_ATTEMPTS = 10;

	/**
	 * attempts to lock a file "toLock" using java nio locking and a separate lock file. this must be later unlocked using the unlockFile
	 * method.
	 */
	static public FileLock lockFile(File toLock)
	{
		FileLock flock = null;
		try {
			flock = acquireLock(toLock);
			if (!toLock.isFile() || !toLock.canRead()) {
				if (_logger.isTraceEnabled())
					_logger.debug("either configuration file does not exist or is not readable: '" + toLock + "'");
				return flock;
			}
		} catch (FileLockException fle) {
			_logger.error("could not lock user configuration file '" + toLock + "'", fle);
			return flock;
		}
		return flock;
	}

	/**
	 * unlocks a previously locked file.
	 */
	static public void unlockFile(FileLock toUnlock)
	{
		StreamUtils.close(toUnlock);
	}

	/**
	 * attempts to lock the file fileToLock using our default number of attempts. the FileLock must be closed once activity on the file is
	 * finished (for example, with StreamUtils.close(flock);).
	 */
	static public FileLock acquireLock(File fileToLock) throws FileLockException
	{
		try {
			return new FileLock(fileToLock, DEFAULT_MAX_LOCK_ATTEMPTS, GenesisIIConstants.DEFAULT_FILE_LOCK);
		} catch (InterruptedException ie) {
			String msg = "Unexpected interruption exception while locking config file: '" + fileToLock + "'";
			_logger.error(msg, ie);
			throw new FileLockException(msg, ie);
		}
	}
}