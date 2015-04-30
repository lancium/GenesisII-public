package edu.virginia.vcgr.genii.client.configuration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileChangeTracker;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.utils.flock.FileLock;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

/**
 * Manages storing configuration items as a list of string value pairs (where each value is also of type string). These config files often
 * reside in the user state directory, but can be stored anywhere. Each file is tracked by an associated "lock" file to ensure the file is not
 * corrupted by multiple writers. The configuration items are cached locally, so this is only appropriate for fairly short files (less than
 * say 500 entries). The configuration is re-read if the file changes. The configuration is written immediately after a new property value is
 * set.
 */
public class UserConfigurationFile
{
	static private Log _logger = LogFactory.getLog(UserConfigurationFile.class);

	// the most number of times we'll try to grab the file lock.
	static private final int MAX_LOCK_ATTEMPTS = 10;

	// the file where our configuration items are stored.
	private FileChangeTracker _configFile = null;

	Properties _configItems = new Properties();

	public UserConfigurationFile(String configFile)
	{
		_configFile = new FileChangeTracker(new File(configFile), true);
		boolean foundFile = readConfiguration();
		if (!foundFile) {
			// we don't see the config file, so we'll try to create it now.
			boolean wroteBlank = writeConfiguration();
			if (!wroteBlank) {
				_logger.error("failed to create initial blank configuration file '" + _configFile.getTrackedFile() + "'");
			}
		}
	}

	/**
	 * higher level getProperty will re-read the file if we notice it's changed.
	 */
	public String getProperty(String key)
	{
		checkTimestampAndReadIfNeeded();
		return _configItems.getProperty(key);
	}

	/**
	 * higher level setProperty automatically writes the new configuration to the config file.
	 */
	public void setProperty(String key, String value)
	{
		_configItems.setProperty(key, value);
		writeConfiguration();
	}

	/**
	 * returns our current cached list of properties. note that it is preferred to call getProperty and setProperty individually, since then
	 * setProperty can properly update the config file for changes in the configuration.
	 */
	public Properties getProperties()
	{
		checkTimestampAndReadIfNeeded();
		return _configItems;
	}

	public void checkTimestampAndReadIfNeeded()
	{
		if (_configFile.hasFileChanged()) {
			/*
			 * future: we really want a merge configuration method here; we don't want to lose changes we had just coz someone else wrote the
			 * file. requires better tracking per individual item.
			 */
			readConfiguration();
		}
	}

	/**
	 * re-reads the information in the configuration file.
	 */
	public boolean readConfiguration()
	{
		FileLock flock = null;
		try {
			// lock our configuration file.
			flock = acquireLock(_configFile.getTrackedFile());
			if (!_configFile.getTrackedFile().isFile() || !_configFile.getTrackedFile().canRead()) {
				_logger.error("either the configuration file does not exist or is not readable: '" + _configFile.getTrackedFile() + "'");
				return false;
			}
			_configItems = new Properties();
			InputStream in = null;
			try {
				in = new FileInputStream(_configFile.getTrackedFile());
				_configItems.load(in);
			} catch (IOException e) {
				_logger.error("failure loading user configuration file '" + _configFile.getTrackedFile() + "'", e);
				return false;
			} finally {
				StreamUtils.close(in);
			}
		} catch (FileLockException fle) {
			_logger.error("could not lock user configuration file '" + _configFile.getTrackedFile() + "'", fle);
			return false;
		} finally {
			// unlock the config file again.
			StreamUtils.close(flock);
		}
		return true;
	}

	/**
	 * writes the current configuration to the file.
	 */
	public boolean writeConfiguration()
	{
		FileLock flock = null;
		try {
			// lock our configuration file.
			flock = acquireLock(_configFile.getTrackedFile());
			if (!_configFile.getTrackedFile().isFile() || !_configFile.getTrackedFile().canWrite()) {
				_logger.error("either the configuration file does not exist or is not writable: '" + _configFile.getTrackedFile() + "'");
				return false;
			}
			OutputStream outfile = null;
			try {
				// open an output file on our file and dump the properties into it.
				outfile = new FileOutputStream(_configFile.getTrackedFile());

				if (_logger.isTraceEnabled()) {
					ByteArrayOutputStream bao = new ByteArrayOutputStream();
					PrintStream out = new PrintStream(bao);
					_configItems.list(out);
					_logger.debug("writing user config to file '" + _configFile.getTrackedFile().getAbsolutePath() + "' with contents:\n"
						+ bao.toString());
				}

				_configItems.store(outfile, "configuration file for gffs");
				outfile.flush();

				// update the timestamp for our config file, since we just wrote it.
				_configFile.fileHasChanged();
			} catch (IOException e) {
				_logger.error("failure loading user configuration file '" + _configFile.getTrackedFile() + "'", e);
				return false;
			} finally {
				StreamUtils.close(outfile);
			}
		} catch (FileLockException fle) {
			_logger.error("could not lock user configuration file '" + _configFile.getTrackedFile() + "'", fle);
			return false;
		} finally {
			// unlock the config file again.
			StreamUtils.close(flock);
		}
		// assuming error exits have already occurred by now.
		return true;
	}

	/**
	 * grabs a lock on the sentinel file for our configuration file.
	 */
	// hmmm: this is more general than for just user config files; move to a handier place.
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
