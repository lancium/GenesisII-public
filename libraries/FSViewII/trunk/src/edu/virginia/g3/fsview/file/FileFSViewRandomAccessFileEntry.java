package edu.virginia.g3.fsview.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Calendar;

import edu.virginia.g3.fsview.AbstractFSViewRandomAccessFileEntry;
import edu.virginia.g3.fsview.FSViewDirectoryEntry;
import edu.virginia.g3.fsview.utils.IOUtils;

final class FileFSViewRandomAccessFileEntry extends AbstractFSViewRandomAccessFileEntry<FileFSViewSession>
{
	private File _file;

	@Override
	final protected void truncateImpl(long newLength) throws IOException
	{
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(_file, "rw");
			raf.setLength(newLength);
		} finally {
			IOUtils.close(raf);
		}
	}

	@Override
	final protected void appendImpl(ByteBuffer content) throws IOException
	{
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(_file, true);
			out.getChannel().write(content);
		} finally {
			IOUtils.close(out);
		}
	}

	@Override
	final protected void writeImpl(long offset, ByteBuffer source) throws IOException
	{
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(_file, "rw");
			raf.seek(offset);
			raf.getChannel().write(source);
		} finally {
			IOUtils.close(raf);
		}
	}

	@Override
	final protected boolean canWriteImpl()
	{
		return _file.canWrite();
	}

	FileFSViewRandomAccessFileEntry(FileFSViewSession session, FSViewDirectoryEntry parentEntry, String entryName, File file)
	{
		super(FileFSViewSession.class, session, parentEntry, entryName);

		_file = file;
	}

	@Override
	final public void read(long offset, ByteBuffer sink) throws IOException
	{
		RandomAccessFile raf = null;

		try {
			raf = new RandomAccessFile(_file, "r");
			raf.seek(offset);
			raf.getChannel().read(sink);
		} finally {
			IOUtils.close(raf);
		}
	}

	@Override
	final public Long size()
	{
		return _file.length();
	}

	@Override
	final public boolean canRead()
	{
		return _file.canRead();
	}

	@Override
	final public Calendar lastModified()
	{
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(_file.lastModified());
		return ret;
	}
}