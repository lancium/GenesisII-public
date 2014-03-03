package edu.virginia.g3.fsview.cifs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import jcifs.smb.SmbRandomAccessFile;

import edu.virginia.g3.fsview.AbstractFSViewRandomAccessFileEntry;
import edu.virginia.g3.fsview.FSViewDirectoryEntry;
import edu.virginia.g3.fsview.utils.IOUtils;

final class CifsFSViewRandomAccessFileEntry extends
		AbstractFSViewRandomAccessFileEntry<CifsFSViewSession> {
	private SmbFile _entry;

	@Override
	final protected void truncateImpl(long newLength) throws IOException {
		SmbRandomAccessFile raf = null;

		try {
			raf = new SmbRandomAccessFile(_entry, "rw");
			raf.setLength(newLength);
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	@Override
	final protected void appendImpl(ByteBuffer content) throws IOException {
		SmbFileOutputStream out = null;

		try {
			out = new SmbFileOutputStream(_entry, true);
			IOUtils.write(out, content);
		} finally {
			IOUtils.close(out);
		}
	}

	@Override
	final protected void writeImpl(long offset, ByteBuffer source)
			throws IOException {
		SmbRandomAccessFile raf = null;

		try {
			raf = new SmbRandomAccessFile(_entry, "rw");
			raf.seek(offset);
			byte[] data = new byte[source.remaining()];
			source.get(data);
			raf.write(data);
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	@Override
	final protected boolean canWriteImpl() {
		try {
			return _entry.canWrite();
		} catch (SmbException e) {
			return false;
		}
	}

	CifsFSViewRandomAccessFileEntry(CifsFSViewSession session,
			FSViewDirectoryEntry parentEntry, String entryName, SmbFile entry) {
		super(CifsFSViewSession.class, session, parentEntry, entryName);

		_entry = entry;
	}

	@Override
	final public void read(long offset, ByteBuffer sink) throws IOException {
		SmbRandomAccessFile raf = null;

		try {
			raf = new SmbRandomAccessFile(_entry, "r");
			raf.seek(offset);
			byte[] data = new byte[sink.remaining()];
			int read;
			while (sink.hasRemaining()
					&& (read = raf.read(data, 0, sink.remaining())) >= 0)
				sink.put(data, 0, read);
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	@Override
	final public Long size() {
		try {
			return _entry.length();
		} catch (SmbException e) {
			return null;
		}
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