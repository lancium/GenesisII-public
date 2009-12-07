package edu.virginia.vcgr.genii.container.cservices.percall;

import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class DestroyActor implements OutcallActor
{
	static final long serialVersionUID = 0L;
	
	@Override
	public boolean enactOutcall(ICallingContext callingContext,
			EndpointReferenceType target) throws Throwable
	{
		GeniiCommon common = ClientUtils.createProxy(
			GeniiCommon.class, target, callingContext);
		common.destroy(new Destroy());
		return true;
	}
}