package edu.virginia.vcgr.genii.client.utils.flock;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileLock implements Closeable
{
	private FileOutputStream _internalFile = null;
	private java.nio.channels.FileLock _internalLock = null;
	
	/**
	 * Creates a file lock that can be used for both reading and writing (by creating a new file
	 * with the same name as the input file but .lock at the end) and locking it.
	 * 
	 * @param fileToLock The file to create a lock for.
	 * @param maxAttempts The maximum number of attempts to make at locking before giving up
	 * @param pollInterval The interval (in milliseconds) to wait between attempts to lock the file before giving
	 * up.  This value can be 0 indicating infinitee).
	 * @throws FileLockException
	 */
	public FileLock(File fileToLock, int maxAttempts, long pollInterval)
		throws FileLockException, InterruptedException
	{
		File lockFile = determineFileLock(fileToLock);
		
		try
		{
			_internalFile = new FileOutputStream(lockFile);
			
			while (maxAttempts > 0)
			{
				try
				{
					if (pollInterval > 0)
						_internalLock = _internalFile.getChannel().tryLock();
					else
						_internalLock = _internalFile.getChannel().lock();
			
					if (_internalLock != null)
						return;
				}
				catch (IOException ioe)
				{
					// Error locking the file.  Sleep and try again.
					Thread.sleep(pollInterval);
				}
			}
			
			try { _internalFile.close(); } catch (Throwable cause) {}
			throw new FileLockException("Unable to create lock for file \"" +
				fileToLock + "\".");
		}
		catch (IOException ioe)
		{
			throw new FileLockException("Unable to create lock for \"" +
				fileToLock + "\".", ioe);
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
		if (_internalLock != null)
		{
			try { _internalLock.release(); } catch (Throwable cause) {}
			try { _internalFile.close(); } catch (Throwable cause) {}
			_internalLock = null;
		}
	}
}