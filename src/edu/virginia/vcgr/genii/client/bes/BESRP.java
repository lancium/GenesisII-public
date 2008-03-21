package edu.virginia.vcgr.genii.client.bes;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;
import edu.virginia.vcgr.genii.container.bes.BESPolicy;

public interface BESRP extends BESConstants
{
	@ResourceProperty(namespace = GENII_BES_NS, localname = "Policy",
		translator = BESPolicyRPTranslater.class)
	public BESPolicy getPolicy();
	
	@ResourceProperty(namespace = GENII_BES_NS, localname = "Policy",
		translator = BESPolicyRPTranslater.class)
	public void setPolicy(BESPolicy policy);
}
