package edu.virginia.vcgr.genii.client.utils.flock;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class FileLock implements Closeable
{
	static private Log _logger = LogFactory.getLog(FileLock.class);

	private FileOutputStream _internalFile = null;
	private java.nio.channels.FileLock _internalLock = null;

	/**
	 * Creates a file lock that can be used for both reading and writing (by creating a new file with the same name as the input file but
	 * .lock at the end) and locking it.
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
		File lockFile = determineFileLock(fileToLock);

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
			throw new FileLockException("Unable to create lock for file \"" + fileToLock + "\".");
		} catch (IOException ioe) {
			throw new FileLockException("Unable to create lock for \"" + fileToLock + "\".", ioe);
		}
	}

	protected void finalize() throws IOException
	{
		close();
	}

	static private File determineFileLock(File file)
	{
		return new File(file.getAbsolutePath() + ".lock");
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

	static private final int MAX_LOCK_ATTEMPTS = 10;

	/**
	 * attempts to lock a file "toLock" using java nio locking. this must be later unlocked using the unlockFile method.
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
	 * attempts to lock the file fileToLock using our default number of attempts.
	 */
	static private FileLock acquireLock(File fileToLock) throws FileLockException
	{
		try {
			return new FileLock(fileToLock, MAX_LOCK_ATTEMPTS, GenesisIIConstants.DEFAULT_FILE_LOCK);
		} catch (InterruptedException ie) {
			String msg = "Unexpected interruption exception while locking config file: '" + fileToLock + "'";
			_logger.error(msg, ie);
			throw new FileLockException(msg, ie);
		}
	}
}