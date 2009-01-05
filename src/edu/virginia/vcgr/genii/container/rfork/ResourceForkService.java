package edu.virginia.vcgr.genii.container.rfork;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public interface ResourceForkService
{
	public ResourceKey getResourceKey()
		throws ResourceUnknownFaultType, ResourceException;
	
	public EndpointReferenceType createForkEPR(
		String forkPath, ResourceForkInformation rif)
			throws ResourceUnknownFaultType, ResourceException;
}