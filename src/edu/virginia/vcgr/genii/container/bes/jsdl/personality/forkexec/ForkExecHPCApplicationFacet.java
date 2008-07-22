package edu.virginia.vcgr.genii.container.bes.jsdl.personality.forkexec;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonPosixLikeHPCApplicationFacet;

class ForkExecHPCApplicationFacet extends CommonPosixLikeHPCApplicationFacet
{
	@Override
	public Object createFacetUnderstanding(Object parentUnderstanding)
		throws JSDLException
	{
		return new ForkExecApplicationUnderstanding();
	}
}