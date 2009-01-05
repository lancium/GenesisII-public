package edu.virginia.vcgr.genii.container.common;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;

public interface AttributesPreFetcherFactory
{
	public AttributePreFetcher getPreFetcher(
		EndpointReferenceType target) throws Throwable;
}