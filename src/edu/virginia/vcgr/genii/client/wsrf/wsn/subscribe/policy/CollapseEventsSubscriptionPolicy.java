package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

@XmlRootElement(namespace = GenesisIIConstants.GENESISII_NS, name = "collapse-events")
public class CollapseEventsSubscriptionPolicy
	extends DurationBasedSubscriptionPolicy
{
	static final long serialVersionUID = 0L;
	
	private CollapseEventsSubscriptionPolicy()
	{
		super(SubscriptionPolicyTypes.CollapseEvents, null);
	}
	
	public CollapseEventsSubscriptionPolicy(Duration duration)
	{
		super(SubscriptionPolicyTypes.CollapseEvents, duration);
	}
}