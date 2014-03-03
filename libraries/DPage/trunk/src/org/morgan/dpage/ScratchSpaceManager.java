package org.morgan.dpage;

import java.io.File;
import java.io.FileNotFoundException;

public class ScratchSpaceManager {
	private int _count = 0;

	private File _parentDirectory;

	static private void remove(File entry) {
		if (entry.isDirectory())
			removeDirectory(entry);
		entry.delete();
	}

	static private void removeDirectory(File directory) {
		for (File entry : directory.listFiles())
			remove(entry);
	}

	public ScratchSpaceManager(File parentDirectory)
			throws FileNotFoundException {
		_parentDirectory = parentDirectory;
		if (!_parentDirectory.exists())
			_parentDirectory.mkdirs();

		if (!_parentDirectory.exists() || !_parentDirectory.isDirectory())
			throw new FileNotFoundException(String.format(
					"Couldn't find directory \"%s\".", _parentDirectory));

		removeDirectory(_parentDirectory);
	}

	synchronized ScratchDirectory newScratchDirectory()
			throws FileNotFoundException {
		while (true) {
			File entry = new File(_parentDirectory, String.format("scratch-%d",
					_count++));
			if (!entry.exists()) {
				entry.mkdirs();
				if (!entry.exists())
					throw new FileNotFoundException(String.format(
							"Unable to find directory \"%s\".", entry));

				return new ScratchDirectory(entry.getParentFile(),
						entry.getName());
			}
		}
	}
}