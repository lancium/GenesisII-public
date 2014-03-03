package edu.virginia.g3.fsview;

import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class AbstractFSViewDirectoryEntry<SessionType extends FSViewSession>
		extends AbstractFSViewEntry<SessionType> implements
		FSViewDirectoryEntry {
	protected AbstractFSViewDirectoryEntry(Class<SessionType> sessionTypeClass,
			SessionType session, FSViewDirectoryEntry parentEntry,
			String entryName) {
		super(sessionTypeClass, session, parentEntry, entryName,
				FSViewEntryType.Directory);
	}

	abstract protected FSViewFileEntry createFileImpl(String name)
			throws IOException;

	abstract protected FSViewDirectoryEntry createDirectoryImpl(String name)
			throws IOException;

	abstract protected void deleteImpl(String name) throws IOException;

	@Override
	public FSViewEntry lookup(String name) throws IOException {
		for (FSViewEntry entry : listEntries())
			if (entry.entryName().equals(name))
				return entry;

		throw new FileNotFoundException(String.format(
				"Unable to locate entry %s in %s.", name, this));
	}

	@Override
	final public FSViewFileEntry createFile(String name) throws IOException {
		if (session().isReadOnly())
			throw new IOException(String.format("File system %s is read-only!",
					session()));

		return createFileImpl(name);
	}

	@Override
	final public FSViewDirectoryEntry createDirectory(String name)
			throws IOException {
		if (session().isReadOnly())
			throw new IOException(String.format("File system %s is read-only!",
					session()));

		return createDirectoryImpl(name);
	}

	@Override
	final public void delete(String name) throws IOException {
		if (session().isReadOnly())
			throw new IOException(String.format("File system %s is read-only!",
					session()));

		deleteImpl(name);
	}
}