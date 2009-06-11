package edu.virginia.vcgr.genii.container.rfork;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public interface ResourceFork
{
	public String getForkPath();
	
	public void notify(EndpointReferenceType source,
		String topic, MessageElement []userData);
	
	public void destroy() throws ResourceException;
	public ResourceForkInformation describe();
}