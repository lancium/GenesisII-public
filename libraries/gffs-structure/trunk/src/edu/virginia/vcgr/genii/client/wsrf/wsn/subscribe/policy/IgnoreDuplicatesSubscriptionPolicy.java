package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = GenesisIIConstants.GENESISII_NS, name = "ignore-duplicates")
public class IgnoreDuplicatesSubscriptionPolicy extends AbstractSubscriptionPolicy
{
	static final long serialVersionUID = 0L;

	public IgnoreDuplicatesSubscriptionPolicy()
	{
		super(SubscriptionPolicyTypes.IgnoreDuplicateEvents);
	}
}
