package edu.virginia.g3.fsview;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public abstract class AbstractFSViewRandomAccessFileEntry<SessionType extends FSViewSession> extends
	AbstractFSViewFileEntry<SessionType> implements FSViewRandomAccessFileEntry
{
	abstract protected void truncateImpl(long newLength) throws IOException;

	abstract protected void appendImpl(ByteBuffer content) throws IOException;

	abstract protected void writeImpl(long offset, ByteBuffer source) throws IOException;

	protected AbstractFSViewRandomAccessFileEntry(Class<SessionType> sessionTypeClass, SessionType session,
		FSViewDirectoryEntry parentEntry, String entryName)
	{
		super(sessionTypeClass, session, parentEntry, entryName, FSViewFileEntryType.RandomAccessFile);
	}

	@Override
	final public void truncate(long newLength) throws IOException
	{
		if (session().isReadOnly())
			throw new IOException(String.format("File system %s is read-only!", session()));

		truncateImpl(newLength);
	}

	@Override
	final public void append(ByteBuffer content) throws IOException
	{
		if (session().isReadOnly())
			throw new IOException(String.format("File system %s is read-only!", session()));

		appendImpl(content);
	}

	@Override
	final public void write(long offset, ByteBuffer source) throws IOException
	{
		if (session().isReadOnly())
			throw new IOException(String.format("File system %s is read-only!", session()));

		writeImpl(offset, source);
	}

	final public InputStream openInputStream() throws IOException
	{
		return new RandomFileEntryInputStream(this);
	}

	final public OutputStream openOutputStream() throws IOException
	{
		return new RandomFileEntryOutputStream(this);
	}
}