package edu.virginia.vcgr.genii.client.bes;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;
import edu.virginia.vcgr.genii.client.bes.BESPolicy;

public interface BESRP
{
	BESConstants bconsts = new BESConstants();

	@ResourceProperty(namespace = BESConstants.GENII_BES_NS, localname = BESConstants.POLICY_RESOURCE_PROPERTY_NAME,
		translator = BESPolicyRPTranslater.class)
	abstract public BESPolicy getPolicy();

	@ResourceProperty(namespace = BESConstants.GENII_BES_NS, localname = BESConstants.POLICY_RESOURCE_PROPERTY_NAME,
		translator = BESPolicyRPTranslater.class)
	abstract public void setPolicy(BESPolicy policy);

	@ResourceProperty(namespace = BESConstants.GENII_BES_NS, localname = BESConstants.THRESHOLD_RESOURCE_PROPERTY_NAME)
	abstract public Integer getThreshold();

	@ResourceProperty(namespace = BESConstants.GENII_BES_NS, localname = BESConstants.THRESHOLD_RESOURCE_PROPERTY_NAME)
	abstract public void setThreshold(Integer threshold);

	@ResourceProperty(namespace = BESConstants.GENII_BES_NS, localname = BESConstants.IS_ACCEPTING_NEW_ACTIVITIES_NAME)
	abstract public Boolean isAcceptingNewActivities();

	@ResourceProperty(namespace = BESConstants.GENII_BES_NS, localname = BESConstants.IS_ACCEPTING_NEW_ACTIVITIES_NAME)
	abstract public void isAcceptingNewActivities(Boolean isAccepting);
}