package edu.virginia.vcgr.genii.container.byteio;

import java.util.Calendar;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class DefaultRandomByteIOAttributePreFetcher 
	extends RandomByteIOAttributePreFetcher
{
	public DefaultRandomByteIOAttributePreFetcher(IRByteIOResource resource)
	{
		super(resource);
	}
	
	public DefaultRandomByteIOAttributePreFetcher(EndpointReferenceType target) 
		throws ResourceException, ResourceUnknownFaultType
	{
		this((IRByteIOResource)ResourceManager.getTargetResource(
			target).dereference());
	}
	
	@Override
	protected Calendar getAccessTime() throws Throwable
	{
		return getResource().getAccessTime();
	}

	@Override
	protected Calendar getCreateTime() throws Throwable
	{
		return getResource().getCreateTime();
	}

	@Override
	protected Calendar getModificationTime() throws Throwable
	{
		return getResource().getModTime();
	}

	@Override
	protected Long getSize() throws Throwable
	{
		return getResource().getCurrentFile().length();
	}
}