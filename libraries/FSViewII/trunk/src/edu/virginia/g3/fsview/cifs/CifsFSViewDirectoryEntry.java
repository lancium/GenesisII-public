package edu.virginia.g3.fsview.cifs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

import edu.virginia.g3.fsview.AbstractFSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewFileEntry;

final class CifsFSViewDirectoryEntry extends
		AbstractFSViewDirectoryEntry<CifsFSViewSession> {
	private SmbFile _entry;

	@Override
	final protected FSViewFileEntry createFileImpl(String name)
			throws IOException {
		SmbFile newFile = new SmbFile(_entry, name);
		newFile.createNewFile();
		return (FSViewFileEntry) typedSession()
				.wrapSmbFile(this, name, newFile);
	}

	@Override
	final protected FSViewDirectoryEntry createDirectoryImpl(String name)
			throws IOException {
		SmbFile newFile = new SmbFile(_entry, name);
		if (newFile.exists())
			throw new IOException(String.format("Path %s/%s already exists.",
					this, name));

		newFile.mkdir();
		return (FSViewDirectoryEntry) typedSession().wrapSmbFile(this, name,
				newFile);
	}

	@Override
	final protected void deleteImpl(String name) throws IOException {
		SmbFile oldFile = new SmbFile(_entry, name);
		oldFile.delete();
	}

	@Override
	final protected boolean canWriteImpl() {
		try {
			return _entry.canWrite();
		} catch (SmbException e) {
			return false;
		}
	}

	CifsFSViewDirectoryEntry(CifsFSViewSession session,
			FSViewDirectoryEntry parentEntry, String entryName, SmbFile entry) {
		super(CifsFSViewSession.class, session, parentEntry, entryName);

		_entry = entry;
	}

	@Override
	final public FSViewEntry[] listEntries() throws IOException {
		SmbFile[] entries = _entry.listFiles();
		FSViewEntry[] ret = new FSViewEntry[entries.length];
		for (int lcv = 0; lcv < entries.length; lcv++)
			ret[lcv] = typedSession().wrapSmbFile(this, entries[lcv].getName(),
					entries[lcv]);

		return ret;
	}

	@Override
	final public boolean canRead() {
		try {
			return _entry.canRead();
		} catch (SmbException e) {
			return false;
		}
	}

	@Override
	final public FSViewEntry lookup(String name) throws IOException {
		SmbFile file = new SmbFile(_entry, name);
		if (!file.exists())
			throw new FileNotFoundException(String.format(
					"Unable to locate samba file %s/%s", this, name));
		if (file.isDirectory() && !name.endsWith("/"))
			file = new SmbFile(_entry, name + "/");

		return typedSession().wrapSmbFile(this, name, file);
	}

	@Override
	final public Calendar createTime() {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(_entry.createTime());
			return calendar;
		} catch (SmbException e) {
			return super.createTime();
		}
	}

	@Override
	final public Calendar lastModified() {
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(_entry.lastModified());
			return calendar;
		} catch (SmbException e) {
			return super.lastModified();
		}
	}
}