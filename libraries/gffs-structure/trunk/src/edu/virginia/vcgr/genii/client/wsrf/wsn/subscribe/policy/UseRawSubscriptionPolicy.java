package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy;

import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;

@XmlRootElement(namespace = WSRFConstants.WSN_BASE_NOT_NS, name = "UseRaw")
public class UseRawSubscriptionPolicy extends AbstractSubscriptionPolicy
{
	static final long serialVersionUID = 0L;

	public UseRawSubscriptionPolicy()
	{
		super(SubscriptionPolicyTypes.UseRawPolicy);
	}
}