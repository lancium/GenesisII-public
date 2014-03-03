package edu.virginia.g3.fsview;

import java.util.Calendar;

public abstract class AbstractFSViewEntry<SessionType extends FSViewSession>
		implements FSViewEntry {
	static private Calendar DEFAULT_CREATE_TIME;

	static {
		DEFAULT_CREATE_TIME = Calendar.getInstance();
		DEFAULT_CREATE_TIME.setTimeInMillis(0L);
	}

	private Class<SessionType> _sessionTypeClass;

	private FSViewSession _session;
	private FSViewEntryType _entryType;
	private String _entryName;
	private FSViewDirectoryEntry _parentEntry;

	abstract protected boolean canWriteImpl();

	final protected SessionType typedSession() {
		return _sessionTypeClass.cast(_session);
	}

	protected AbstractFSViewEntry(Class<SessionType> sessionTypeClass,
			SessionType session, FSViewDirectoryEntry parentEntry,
			String entryName, FSViewEntryType entryType) {
		_sessionTypeClass = sessionTypeClass;

		_session = session;
		_entryName = entryName;
		_entryType = entryType;
		_parentEntry = parentEntry;
	}

	@Override
	final public FSViewSession session() {
		return _session;
	}

	@Override
	final public FSViewEntryType entryType() {
		return _entryType;
	}

	@Override
	final public String entryName() {
		return _entryName;
	}

	@Override
	final public FSViewDirectoryEntry parent() {
		return _parentEntry;
	}

	@Override
	final public boolean canWrite() {
		if (session().isReadOnly())
			return false;

		return canWriteImpl();
	}

	@Override
	final public String toString() {
		if (_parentEntry == null)
			return "";

		String ret = _parentEntry.toString();
		if (ret.length() == 0)
			return entryName();
		else
			return ret + "/" + entryName();
	}

	@Override
	public Calendar createTime() {
		return (Calendar) DEFAULT_CREATE_TIME.clone();
	}

	@Override
	public Calendar lastAccessed() {
		Calendar ret = Calendar.getInstance();
		return ret;
	}

	@Override
	public Calendar lastModified() {
		Calendar ret = Calendar.getInstance();
		return ret;
	}
}