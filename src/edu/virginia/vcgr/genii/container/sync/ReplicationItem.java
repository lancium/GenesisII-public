package edu.virginia.vcgr.genii.container.sync;

import org.ws.addressing.EndpointReferenceType;

public class ReplicationItem
{
	public ResourceSyncRunner runner;
	public EndpointReferenceType localEPR;

	public ReplicationItem(ResourceSyncRunner runner, EndpointReferenceType localEPR)
	{
		this.runner = runner;
		this.localEPR = localEPR;
	}
}
