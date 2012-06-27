package edu.virginia.vcgr.genii.container.common;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public interface AttributesPreFetcherFactory
{
	public AttributePreFetcher getPreFetcher(
		EndpointReferenceType target, ResourceKey rKey,
		ResourceForkService service) throws Throwable;
}