package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import java.util.Calendar;

import org.ws.addressing.EndpointReferenceType;

public interface Subscription
{
	public void cancel();

	public EndpointReferenceType subscriptionReference();

	public Calendar publisherCurrentTime();

	public Calendar publisherTerminationTime();
}