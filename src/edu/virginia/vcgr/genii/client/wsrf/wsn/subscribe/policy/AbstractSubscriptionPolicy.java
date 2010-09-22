package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy;

import java.io.Serializable;

class AbstractSubscriptionPolicy implements SubscriptionPolicy, Serializable
{
	static final long serialVersionUID = 0L;
	
	private SubscriptionPolicyTypes _policyType;
	
	@SuppressWarnings("unused")
	private AbstractSubscriptionPolicy()
	{
		_policyType = null;
	}
	
	protected AbstractSubscriptionPolicy(SubscriptionPolicyTypes policyType)
	{
		_policyType = policyType;
	}
	
	@Override
	final public SubscriptionPolicyTypes policyType()
	{
		return _policyType;
	}
}