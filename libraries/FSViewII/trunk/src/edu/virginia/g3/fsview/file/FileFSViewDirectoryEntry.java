package edu.virginia.g3.fsview.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import edu.virginia.g3.fsview.AbstractFSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewFileEntry;

final class FileFSViewDirectoryEntry extends
		AbstractFSViewDirectoryEntry<FileFSViewSession> {
	private File _file;

	@Override
	final protected FSViewFileEntry createFileImpl(String name)
			throws IOException {
		File newFile = new File(_file, name);
		if (!newFile.createNewFile())
			throw new IOException(String.format("Unable to create file %s!",
					newFile));

		return (FSViewFileEntry) typedSession().wrapFile(newFile);
	}

	@Override
	final protected FSViewDirectoryEntry createDirectoryImpl(String name)
			throws IOException {
		File newFile = new File(_file, name);
		if (!newFile.mkdir())
			throw new IOException(String.format(
					"Unable to create directory %s!", newFile));

		return (FSViewDirectoryEntry) typedSession().wrapFile(newFile);
	}

	@Override
	final protected void deleteImpl(String name) throws IOException {
		if (!_file.delete())
			throw new IOException(String.format(
					"Unable to delete file system entry %s!", _file));
	}

	@Override
	final protected boolean canWriteImpl() {
		return _file.canWrite();
	}

	FileFSViewDirectoryEntry(FileFSViewSession session,
			FSViewDirectoryEntry parentEntry, String entryName, File file) {
		super(FileFSViewSession.class, session, parentEntry, entryName);

		_file = file;
	}

	@Override
	final public FSViewEntry[] listEntries() throws IOException {
		File[] files = _file.listFiles();
		FSViewEntry[] ret = new FSViewEntry[files.length];

		for (int lcv = 0; lcv < files.length; lcv++)
			ret[lcv] = typedSession().wrapFile(files[lcv]);

		return ret;
	}

	@Override
	final public boolean canRead() {
		return _file.canRead();
	}

	@Override
	final public FSViewEntry lookup(String name) throws IOException {
		File newFile = new File(_file, name);
		if (!newFile.exists())
			throw new FileNotFoundException(String.format(
					"Unable to locate file system entry %s!", newFile));

		return typedSession().wrapFile(newFile);
	}

	@Override
	final public Calendar lastModified() {
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(_file.lastModified());
		return ret;
	}
}