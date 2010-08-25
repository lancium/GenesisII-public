package edu.virginia.vcgr.genii.container.common.notification;

import java.util.EnumMap;
import java.util.Map;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicyTypes;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

public class WSNSubscriptionInformation
{
	private EndpointReferenceType _subscriptionReference;
	private EndpointReferenceType _consumerReference;
	private TopicQueryExpression _topicFilter;
	private Map<SubscriptionPolicyTypes, SubscriptionPolicy> _policies;
	private AdditionalUserData _additionalUserData;
	
	WSNSubscriptionInformation(EndpointReferenceType subscriptionReference,
		EndpointReferenceType consumerReference,
		TopicQueryExpression topicFilter,
		Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies,
		AdditionalUserData additionalUserData)
	{
		_subscriptionReference = subscriptionReference;
		_consumerReference = consumerReference;
		_topicFilter = topicFilter;
		_policies = policies;
		_additionalUserData = additionalUserData;
		
		if (_policies == null)
			_policies = 
				new EnumMap<SubscriptionPolicyTypes, SubscriptionPolicy>(
					SubscriptionPolicyTypes.class);
	}
	
	final public EndpointReferenceType subscriptionReference()
	{
		return _subscriptionReference;
	}
	
	final public EndpointReferenceType consumerReference()
	{
		return _consumerReference;
	}
	
	final public TopicQueryExpression topicFilter()
	{
		return _topicFilter;
	}
	
	final public Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies()
	{
		return _policies;
	}
	
	final public AdditionalUserData additionalUserData()
	{
		return _additionalUserData;
	}
}