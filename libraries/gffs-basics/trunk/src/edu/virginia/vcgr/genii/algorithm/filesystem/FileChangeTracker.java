package edu.virginia.vcgr.genii.algorithm.filesystem;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple class that tracks whether a file's access time has changed since the last time it was checked. If the time has changed, the owner
 * will presumably want to react by re-reading the file or other appropriate actions.
 */
public class FileChangeTracker
{
	static private Log _logger = LogFactory.getLog(FileChangeTracker.class);

	private File _toWatch = null;
	private long _lastChangeTime = -1;

	/**
	 * constructs a change tracker for the file "toWatch". changes can be noticed by calling hasFileChanged(). this method expects the config
	 * file to already exist.
	 */
	public FileChangeTracker(File toWatch)
	{
		this(toWatch, false);
	}

	/**
	 * constructs a change tracker for the file "toWatch". changes can be noticed by calling hasFileChanged(). this also allows the file to be
	 * created automatically if it did not exist already.
	 */
	public FileChangeTracker(File toWatch, boolean createFile)
	{
		if (toWatch == null)
			throw new RuntimeException("null file passed to file change watcher");
		_toWatch = toWatch;
		if (_toWatch.exists()) {
			_lastChangeTime = _toWatch.lastModified();
		} else {
			if (_logger.isDebugEnabled()) {
				_logger.debug("file doesn't exist yet: '" + _toWatch.getAbsolutePath() + "'");
			}
			if (createFile) {
				// make our best attempt to create the parent dir in case it doesn't exist.
				_toWatch.getParentFile().mkdirs();
				// now try to create a new blank file as per the user's request.
				try {
					_toWatch.createNewFile();
				} catch (IOException e) {
					_logger.error("failed to create new config file '" + _toWatch.getAbsolutePath() + "'");
					return; // fail.
				}
			}
			// by now the file should exist, so check it's time.
			_lastChangeTime = _toWatch.lastModified();
		}
	}

	/**
	 * returns the last timestamp known for our tracked file.
	 */
	public long getLastChangeTime()
	{
		return _lastChangeTime;
	}

	/**
	 * returns the file that we're tracking changes on.
	 */
	public File getTrackedFile()
	{
		return _toWatch;
	}

	/**
	 * updates the time stamp when the caller *knows* that the file just changed.
	 */
	public void fileHasChanged()
	{
		_lastChangeTime = -1;
		// ignore the result, since we know we just triggered getting the updated stamp.
		hasFileChanged();
	}

	/**
	 * returns true if the file has changed since this watcher was created or since the last time it was checked. if the file did change, then
	 * the last change time is updated.
	 */
	public boolean hasFileChanged()
	{
		long currChangeTime = _toWatch.lastModified();
		if (currChangeTime > _lastChangeTime) {
			if (_logger.isDebugEnabled()) {
				_logger.debug("file has changed!: '" + _toWatch.getAbsolutePath() + "', old mod time was " + _lastChangeTime
					+ " and new time is " + currChangeTime);
			}
			_lastChangeTime = currChangeTime;
			return true;
		}
		return false;
	}

}
