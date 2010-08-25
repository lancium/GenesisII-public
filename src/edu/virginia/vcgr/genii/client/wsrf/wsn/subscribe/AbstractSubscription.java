package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsn.base.SubscribeResponse;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public abstract class AbstractSubscription implements Subscription
{
	static private Log _logger = LogFactory.getLog(AbstractSubscription.class);
	
	private EndpointReferenceType _subscriptionReference;
	private Calendar _publisherCurrentTime;
	private Calendar _publisherTerminationTime;
	
	protected AbstractSubscription(SubscribeResponse response)
	{
		_subscriptionReference = response.getSubscriptionReference();
		_publisherCurrentTime = response.getCurrentTime();
		_publisherTerminationTime = response.getTerminationTime();
	}
	
	@Override
	final public EndpointReferenceType subscriptionReference()
	{
		return _subscriptionReference;
	}
	
	@Override
	final public Calendar publisherCurrentTime()
	{
		return _publisherCurrentTime;
	}

	@Override
	final public Calendar publisherTerminationTime()
	{
		return _publisherTerminationTime;
	}
	
	@Override
	public void cancel()
	{
		try
		{
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
				subscriptionReference());
			common.destroy(new Destroy());
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to cancel subscription with publisher."
				, cause);
		}
	}
}