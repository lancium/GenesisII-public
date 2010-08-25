package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

@XmlRootElement(namespace = GenesisIIConstants.GENESISII_NS, name = "batch-events")
public class BatchEventsSubscriptionPolicy extends DurationBasedSubscriptionPolicy
{
	static final long serialVersionUID = 0L;
	
	public BatchEventsSubscriptionPolicy(Duration batchDuration)
	{
		super(SubscriptionPolicyTypes.BatchEvents, batchDuration);
	}
}