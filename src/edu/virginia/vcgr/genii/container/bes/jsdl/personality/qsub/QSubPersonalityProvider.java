package edu.virginia.vcgr.genii.container.bes.jsdl.personality.qsub;

import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.jsdl.personality.HPCApplicationFacet;
import edu.virginia.vcgr.genii.client.jsdl.personality.POSIXApplicationFacet;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.CommonPersonalityProvider;

public class QSubPersonalityProvider extends CommonPersonalityProvider
{
	@Override
	public POSIXApplicationFacet getPOSIXApplicationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new QSubPOSIXApplicationFacet();
	}
	
	@Override
	public HPCApplicationFacet getHPCApplicationFacet(
		Object currentUnderstanding) throws JSDLException
	{
		return new QSubHPCApplicationFacet();
	}
}