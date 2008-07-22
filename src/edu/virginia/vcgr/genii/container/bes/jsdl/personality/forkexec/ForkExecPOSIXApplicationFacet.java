package edu.virginia.vcgr.genii.container.bes.jsdl.personality.forkexec;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonPosixLikePOSIXApplicationFacet;

class ForkExecPOSIXApplicationFacet extends CommonPosixLikePOSIXApplicationFacet
{
	@Override
	public Object createFacetUnderstanding(Object partentUnderstanding)
		throws JSDLException
	{
		return new ForkExecApplicationUnderstanding();
	}
}