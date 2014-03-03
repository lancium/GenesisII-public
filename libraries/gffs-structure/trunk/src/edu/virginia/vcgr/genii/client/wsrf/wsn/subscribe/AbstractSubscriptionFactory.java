package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

public abstract class AbstractSubscriptionFactory implements SubscriptionFactory
{
	static public SubscribeRequest createRequest(EndpointReferenceType consumer, TopicQueryExpression topicFilter,
		TerminationTimeType terminationTime, AdditionalUserData additionalUserData, SubscriptionPolicy... policies)
	{
		return new SubscribeRequest(consumer, topicFilter, terminationTime, additionalUserData, policies);
	}

	protected abstract EndpointReferenceType getConsumerReference();

	@Override
	final public SubscribeRequest createRequest(TopicQueryExpression topicFilter, TerminationTimeType terminationTime,
		AdditionalUserData additionalUserData, SubscriptionPolicy... policies)
	{
		return createRequest(getConsumerReference(), topicFilter, terminationTime, additionalUserData, policies);
	}
}