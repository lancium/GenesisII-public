package edu.virginia.vcgr.genii.container.q2.resource;

import javax.xml.namespace.QName;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface IQueueResource extends IResource
{
	static public final QName QUEUE_EPR_CONSTRUCTION_PARAMTER =
		new QName("http://tempuri.org", "queue-epr");
	
	public void setEPR(EndpointReferenceType epr)
		throws ResourceException;
}