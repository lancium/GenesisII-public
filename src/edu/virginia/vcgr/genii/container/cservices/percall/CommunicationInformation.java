package edu.virginia.vcgr.genii.container.cservices.percall;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

class CommunicationInformation
{
	private EndpointReferenceType _epr;
	private ICallingContext _callingContext;
	
	CommunicationInformation(EndpointReferenceType epr,
		ICallingContext callingContext)
	{
		_epr = epr;
		_callingContext = callingContext;
	}
	
	final EndpointReferenceType epr()
	{
		return _epr;
	}
	
	final ICallingContext callingContext()
	{
		return _callingContext;
	}
}