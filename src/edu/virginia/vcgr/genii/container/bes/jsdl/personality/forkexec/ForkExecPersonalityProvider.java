package edu.virginia.vcgr.genii.container.bes.jsdl.personality.forkexec;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonPersonalityProvider;

public class ForkExecPersonalityProvider extends CommonPersonalityProvider
{
	@Override
	public POSIXApplicationFacet getPOSIXApplicationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new ForkExecPOSIXApplicationFacet();
	}
	
	@Override
	public HPCApplicationFacet getHPCApplicationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new ForkExecHPCApplicationFacet();
	}
}