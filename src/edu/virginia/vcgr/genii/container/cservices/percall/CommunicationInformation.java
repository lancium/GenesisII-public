package edu.virginia.vcgr.genii.container.cservices.percall;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class CommunicationInformation
{
	public EndpointReferenceType targetEPR;
	public ICallingContext callingContext;
	public OutcallActor outcallActor;
	public GeniiAttachment attachment;	
}
