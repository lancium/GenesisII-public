package edu.virginia.vcgr.genii.client.bes;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;

public interface BESRP extends BESConstants
{
	@ResourceProperty(namespace = GENII_BES_NS, localname = POLICY_RESOURCE_PROPERTY_NAME, translator = BESPolicyRPTranslater.class)
	public BESPolicy getPolicy();

	@ResourceProperty(namespace = GENII_BES_NS, localname = POLICY_RESOURCE_PROPERTY_NAME, translator = BESPolicyRPTranslater.class)
	public void setPolicy(BESPolicy policy);

	@ResourceProperty(namespace = GENII_BES_NS, localname = THRESHOLD_RESOURCE_PROPERTY_NAME)
	public Integer getThreshold();

	@ResourceProperty(namespace = GENII_BES_NS, localname = THRESHOLD_RESOURCE_PROPERTY_NAME)
	public void setThreshold(Integer threshold);

	@ResourceProperty(namespace = GENII_BES_NS, localname = IS_ACCEPTING_NEW_ACTIVITIES_NAME)
	public Boolean isAcceptingNewActivities();

	@ResourceProperty(namespace = GENII_BES_NS, localname = IS_ACCEPTING_NEW_ACTIVITIES_NAME)
	public void isAcceptingNewActivities(Boolean isAccepting);
}