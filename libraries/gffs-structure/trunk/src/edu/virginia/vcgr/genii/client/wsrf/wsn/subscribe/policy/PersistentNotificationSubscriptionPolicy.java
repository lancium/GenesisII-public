package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

@XmlRootElement(namespace = GenesisIIConstants.GENESISII_NS, name = "persistent-notification")
public class PersistentNotificationSubscriptionPolicy extends AbstractSubscriptionPolicy
{
	static final long serialVersionUID = 0L;

	public PersistentNotificationSubscriptionPolicy()
	{
		super(SubscriptionPolicyTypes.PersistentNotification);
	}
}
