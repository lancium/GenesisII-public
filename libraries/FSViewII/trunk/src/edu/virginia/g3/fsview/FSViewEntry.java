package edu.virginia.g3.fsview;

import java.util.Calendar;

public interface FSViewEntry {
	public FSViewSession session();

	public FSViewEntryType entryType();

	public String entryName();

	public FSViewDirectoryEntry parent();

	public boolean canRead();

	public boolean canWrite();

	public Calendar createTime();

	public Calendar lastAccessed();

	public Calendar lastModified();
}