package edu.virginia.vcgr.genii.ui.persist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class PersistenceDirectory
{
	static private Log _logger = LogFactory.getLog(PersistenceDirectory.class);

	static private final String FILENAME_FORMAT = "file-%d.dat";
	static private final Pattern FILENAME_PATTERN = Pattern.compile("^file-(\\d+)\\.dat$");

	private Set<PersistenceKey> _keys = new HashSet<PersistenceKey>();

	private File _directory;
	private long _nextNumber;

	PersistenceDirectory(File directory) throws IOException
	{
		if (!directory.exists())
			directory.mkdirs();

		if (!directory.exists())
			throw new FileNotFoundException(String.format("Unable to find directory \"%s\".", directory));

		if (!directory.isDirectory())
			throw new IOException(String.format("Path \"%s\" does not appear to be a directory."));

		_directory = directory;
		_nextNumber = 0;

		for (File target : _directory.listFiles()) {
			try {
				Matcher matcher = FILENAME_PATTERN.matcher(target.getName());
				if (!matcher.matches())
					target.delete();
				else {
					long number = Long.parseLong(matcher.group(1));
					if (number >= _nextNumber)
						_nextNumber = number + 1;
					_keys.add(new PersistenceKey(target));
				}
			} catch (Throwable cause) {
				_logger.warn(String.format("Unable to load persistence directory entry:  %s", target), cause);
			}
		}
	}

	public void removeEntry(PersistenceKey key)
	{
		key.persistenceFile().delete();
		synchronized (_keys) {
			_keys.remove(key);
		}
	}

	public Set<PersistenceKey> keys()
	{
		Set<PersistenceKey> ret;

		synchronized (_keys) {
			ret = new HashSet<PersistenceKey>(_keys);
		}

		return ret;
	}

	public PersistenceKey addEntry(Persistable persistable) throws IOException
	{
		OutputStream out = null;
		File realTarget;
		File tmpTarget = File.createTempFile("tmp", ".tmp", _directory);
		boolean ret;

		try {
			out = new FileOutputStream(tmpTarget);
			ObjectOutputStream oos = new ObjectOutputStream(out);
			ret = persistable.persist(oos);
			oos.close();
			out.close();
			out = null;
			if (!ret)
				return null;

			synchronized (this) {
				realTarget = new File(_directory, String.format(FILENAME_FORMAT, _nextNumber++));
				if (!tmpTarget.renameTo(realTarget))
					throw new IOException("Unable to persist persistable.");
				tmpTarget = null;
			}
			return new PersistenceKey(realTarget);
		} finally {
			StreamUtils.close(out);
			if (tmpTarget != null)
				tmpTarget.delete();
		}
	}
}