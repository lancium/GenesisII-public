package edu.virginia.vcgr.genii.container.queue;

import org.ws.addressing.EndpointReferenceType;

public class ResourceSlot
{
	private int _resourceID;
	private EndpointReferenceType _resourceEndpoint;
	
	public ResourceSlot(int resourceID, EndpointReferenceType resourceEndpoint)
	{
		_resourceID = resourceID;
		_resourceEndpoint = resourceEndpoint;
	}
	
	public int getResourceID()
	{
		return _resourceID;
	}
	
	public EndpointReferenceType getResourceEndpoint()
	{
		return _resourceEndpoint;
	}
}