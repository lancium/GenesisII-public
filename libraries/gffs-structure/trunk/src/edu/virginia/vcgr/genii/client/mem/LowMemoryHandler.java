package edu.virginia.vcgr.genii.client.mem;

public interface LowMemoryHandler
{
	public void lowMemoryWarning(long usedMemory, long maxMemory);
}