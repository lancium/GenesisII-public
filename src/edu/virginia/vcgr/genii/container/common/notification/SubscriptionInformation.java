package edu.virginia.vcgr.genii.container.common.notification;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.common.notification.UserDataType;

public class SubscriptionInformation
{
	private String _subscriptionKey;
	private String _topic;
	private EndpointReferenceType _target;
	private UserDataType _userData;
	
	public SubscriptionInformation(String subscriptionKey, 
		String topic, EndpointReferenceType target, UserDataType userData)
	{
		_subscriptionKey = subscriptionKey;
		_topic = topic;
		_target = target;
		_userData = userData;
	}
	
	public String getSubscriptionKey()
	{
		return _subscriptionKey;
	}
	
	public String getTopic()
	{
		return _topic;
	}
	
	public EndpointReferenceType getTarget()
	{
		return _target;
	}
	
	public UserDataType getUserData()
	{
		return _userData;
	}
}