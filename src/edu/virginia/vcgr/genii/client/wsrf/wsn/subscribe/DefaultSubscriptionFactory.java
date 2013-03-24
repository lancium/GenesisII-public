package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class DefaultSubscriptionFactory extends AbstractSubscriptionFactory
{
	private EndpointReferenceType _consumerReference;

	@Override
	final protected EndpointReferenceType getConsumerReference()
	{
		return _consumerReference;
	}

	public DefaultSubscriptionFactory(EndpointReferenceType consumerReference)
	{
		_consumerReference = consumerReference;
	}

	@Override
	final public Subscription subscribe(EndpointReferenceType publisher, TopicQueryExpression topicFilter,
		TerminationTimeType terminationTime, AdditionalUserData additionalUserData, SubscriptionPolicy... policies)
		throws SubscribeException
	{
		try {
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, publisher);
			return new DefaultSubscription(common.subscribe(new SubscribeRequest(_consumerReference, topicFilter,
				terminationTime, additionalUserData, policies).asRequestType()));
		} catch (Throwable cause) {
			throw new SubscribeException("Unable to subscribe consumer.", cause);
		}
	}
}