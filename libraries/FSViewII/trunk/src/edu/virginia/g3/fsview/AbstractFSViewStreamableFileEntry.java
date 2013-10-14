package edu.virginia.g3.fsview;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractFSViewStreamableFileEntry<SessionType extends FSViewSession> extends
	AbstractFSViewFileEntry<SessionType> implements FSViewStreamableAccessFileEntry
{
	abstract protected OutputStream openOutpuStreamImpl() throws IOException;

	protected AbstractFSViewStreamableFileEntry(Class<SessionType> sessionTypeClass, SessionType session,
		FSViewDirectoryEntry parentEntry, String entryName)
	{
		super(sessionTypeClass, session, parentEntry, entryName, FSViewFileEntryType.StreamableAccessFile);
	}

	@Override
	public Long size()
	{
		// By default, streamable entries don't have a size.
		return null;
	}

	@Override
	final public OutputStream openOutputStream() throws IOException
	{
		if (session().isReadOnly())
			throw new IOException(String.format("File system %s is read-only!", session()));

		return openOutpuStreamImpl();
	}
}