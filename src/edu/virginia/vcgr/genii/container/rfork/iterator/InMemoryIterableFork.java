package edu.virginia.vcgr.genii.container.rfork.iterator;

import java.io.IOException;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.iterator.IterableSnapshot;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public interface InMemoryIterableFork
{

	public IterableSnapshot splitAndList(EndpointReferenceType 
			endpointReferenceType, ResourceKey resourceKey) throws IOException;
	
	public IterableSnapshot splitAndList(String[] lookupRequest,
			EndpointReferenceType exemplarEPR, 
			ResourceKey resourceKey) throws IOException;

}
