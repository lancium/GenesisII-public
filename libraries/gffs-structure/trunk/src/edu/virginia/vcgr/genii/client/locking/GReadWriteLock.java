package edu.virginia.vcgr.genii.client.locking;

public interface GReadWriteLock
{
	public GLock readLock();

	public GLock writeLock();
}