package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

public class HierarchicalDirectory
{
	private String _dirName;
	private Vector<File> _dependentEntries;

	private HierarchicalDirectory(String dirName, Collection<File> dependentEntries)
	{
		_dirName = dirName;

		if (dependentEntries == null)
			throw new IllegalArgumentException("Dependent directories cannot be null.");

		_dependentEntries = new Vector<File>(dependentEntries.size());
		for (File entry : dependentEntries) {
			if (entry.exists() && entry.isDirectory())
				_dependentEntries.add(entry);
		}
	}

	final public boolean exists()
	{
		return _dependentEntries.size() > 0;
	}

	final public File lookupFile(String filename)
	{
		File file;

		for (File dir : _dependentEntries) {
			file = new File(dir, filename);
			if (file.exists() && file.isFile())
				return file;
		}

		return new File(_dependentEntries.firstElement(), filename);
	}

	final public HierarchicalDirectory lookupDirectory(String directoryName)
	{
		Collection<File> dirs = new Vector<File>(_dependentEntries.size());
		for (File dir : _dependentEntries)
			dirs.add(new File(dir, directoryName));

		return new HierarchicalDirectory(directoryName, dirs);
	}

	final public File[] listFiles(FileFilter filter)
	{
		Map<String, File> fileMap = new HashMap<String, File>();

		for (File dir : _dependentEntries) {
			for (File file : dir.listFiles()) {
				if (!fileMap.containsKey(file.getName())) {
					if (filter == null || filter.accept(file))
						fileMap.put(file.getName(), file);
				}
			}
		}

		return fileMap.values().toArray(new File[fileMap.size()]);
	}

	final public File[] listFiles()
	{
		return listFiles(null);
	}

	final public String getName()
	{
		return _dirName;
	}

	static public HierarchicalDirectory openRootHierarchicalDirectory(File originalDirectory)
	{
		String name = originalDirectory.getName();
		Collection<File> dirs = new LinkedList<File>();
		dirs.add(originalDirectory);

		while (true) {
			originalDirectory = DeploymentConf.basedOn(originalDirectory);
			if (originalDirectory == null)
				break;
			dirs.add(originalDirectory);
		}

		return new HierarchicalDirectory(name, dirs);
	}
}