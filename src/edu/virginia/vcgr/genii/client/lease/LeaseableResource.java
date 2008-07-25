package edu.virginia.vcgr.genii.client.lease;

public interface LeaseableResource<ResourceType>
{
	public void cancel();
	public ResourceType resource();
}