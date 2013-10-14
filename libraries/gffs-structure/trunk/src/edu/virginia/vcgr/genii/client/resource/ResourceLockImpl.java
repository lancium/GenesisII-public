package edu.virginia.vcgr.genii.client.resource;

import java.util.concurrent.locks.ReentrantLock;

public class ResourceLockImpl extends ReentrantLock implements ResourceLock
{
	static final long serialVersionUID = 0L;

	public ResourceLockImpl()
	{
		super();
	}

	public ResourceLockImpl(boolean fair)
	{
		super(fair);
	}
}