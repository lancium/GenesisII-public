package edu.virginia.vcgr.genii.container.queue;

public interface IJobResourceMatcher
{
	public boolean matches(JobRequest request, ResourceSlot slot);
}