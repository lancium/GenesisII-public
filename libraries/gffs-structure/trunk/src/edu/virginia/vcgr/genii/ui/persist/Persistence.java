package edu.virginia.vcgr.genii.ui.persist;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.morgan.util.configuration.ConfigurationException;

public class Persistence
{
	static private final String PERSISTENCE_DIRECTORY_NAME = ".genii-ui-persistence";

	static private Persistence _persistence;

	static {
		try {
			_persistence = new Persistence();
		} catch (IOException cause) {
			throw new ConfigurationException("Unable to open persistence directory.", cause);
		}
	}

	static public Persistence persistence()
	{
		return _persistence;
	}

	private File _superDirectory;
	private Map<String, PersistenceDirectory> _directories = new HashMap<String, PersistenceDirectory>();

	private Persistence() throws IOException
	{
		String home = System.getProperty("user.home");
		if (home == null)
			home = ".";
		_superDirectory = new File(home);
		_superDirectory = new File(_superDirectory, PERSISTENCE_DIRECTORY_NAME);
	}

	public PersistenceDirectory directory(String name) throws IOException
	{
		synchronized (_directories) {
			PersistenceDirectory directory = _directories.get(name);
			if (directory == null)
				_directories.put(name, directory = new PersistenceDirectory(new File(_superDirectory, name)));
			return directory;
		}
	}
}
