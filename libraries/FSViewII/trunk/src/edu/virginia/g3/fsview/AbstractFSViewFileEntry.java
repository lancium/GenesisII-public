package edu.virginia.g3.fsview;

public abstract class AbstractFSViewFileEntry<SessionType extends FSViewSession> extends AbstractFSViewEntry<SessionType>
	implements FSViewFileEntry
{
	private FSViewFileEntryType _fileEntryType;

	protected AbstractFSViewFileEntry(Class<SessionType> sessionTypeClass, SessionType session,
		FSViewDirectoryEntry parentEntry, String entryName, FSViewFileEntryType fileEntryType)
	{
		super(sessionTypeClass, session, parentEntry, entryName, FSViewEntryType.File);

		_fileEntryType = fileEntryType;
	}

	@Override
	final public FSViewFileEntryType fileType()
	{
		return _fileEntryType;
	}
}