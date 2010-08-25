package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

public interface SubscriptionFactory
{
	public SubscribeRequest createRequest(
		TopicQueryExpression topicFilter,
		TerminationTimeType terminationTime,
		AdditionalUserData additionalUserData,
		SubscriptionPolicy...policies);
	
	public Subscription subscribe(
		EndpointReferenceType publisher,
		TopicQueryExpression topicFilter,
		TerminationTimeType terminationTime,
		AdditionalUserData additionalUserData,
		SubscriptionPolicy...policies) throws SubscribeException;
}