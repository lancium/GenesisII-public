package edu.virginia.vcgr.genii.container.rfork;

import java.util.Calendar;

public interface ByteIOResourceFork extends ResourceFork
{
	public long size();

	public Calendar createTime();

	public Calendar modificationTime();

	public void modificationTime(Calendar newTime);

	public Calendar accessTime();

	public void accessTime(Calendar newTime);

	public boolean readable();

	public boolean writable();
}