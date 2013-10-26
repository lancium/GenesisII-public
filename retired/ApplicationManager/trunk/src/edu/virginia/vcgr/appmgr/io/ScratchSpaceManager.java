package edu.virginia.vcgr.appmgr.io;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ScratchSpaceManager
{
	static private Log _logger = LogFactory.getLog(ScratchSpaceManager.class);

	static private Random _random = new Random();

	private File _directory;

	static private void cleanoutOldEntries(File directory)
	{
		try {
			for (File target : directory.listFiles()) {
				try {
					IOUtils.recursiveDelete(target);
				} catch (Throwable cause) {
					_logger.error(String.format("Unable to delete entry \"%s\".", target));
				}
			}
		} catch (Throwable cause) {
			_logger.error(String.format("Unable to list contents of directory \"%s\".", directory));
		}
	}

	public ScratchSpaceManager(File directory) throws IOException
	{
		_directory = directory;
		if (!_directory.exists()) {
			if (!_directory.mkdirs())
				throw new IOException(String.format("Unable to create scratch directory \"%s\".", directory));
		}

		if (!_directory.isDirectory())
			throw new IOException(String.format("Path \"%s\" does not appear to be a directory.", _directory));

		cleanoutOldEntries(directory);
	}

	public File newDirectory() throws IOException
	{
		File ret = null;

		while (true) {
			synchronized (_directory) {
				ret = new File(_directory, String.format("tmpdir%d.tmp", _random.nextInt()));
				if (ret.mkdir())
					return ret;
			}
		}
	}

	public File createTemporaryFile() throws IOException
	{
		return File.createTempFile("temp", ".tmp", _directory);
	}

	public File backup(File original) throws IOException
	{
		File ret = null;

		while (true) {
			synchronized (_directory) {
				ret = new File(_directory, String.format("backup%d.tmp", _random.nextInt()));

				if (!ret.exists()) {
					if (!original.renameTo(ret))
						throw new IOException(String.format("Unable to backup file %s to directory %s.", original, _directory));
					return ret;
				}
			}
		}
	}

	public void move(File source, File target) throws IOException
	{
		if (!target.exists()) {
			if (!source.renameTo(target))
				throw new IOException(String.format("Unable to rename %s to %s.", source, target));
		} else {
			File backup = backup(target);
			if (!source.renameTo(target)) {
				if (!backup.renameTo(target))
					throw new IOException(String.format("Failed to replace %s and can't undo temporary work.", target));
				throw new IOException(String.format("Failed to replace %s, but restored original state.", target));
			}

			backup.delete();
		}
	}
}