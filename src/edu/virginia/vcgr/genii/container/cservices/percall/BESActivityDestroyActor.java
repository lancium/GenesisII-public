package edu.virginia.vcgr.genii.container.cservices.percall;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.DestroyActivitiesResponseType;
import org.ggf.bes.factory.DestroyActivitiesType;
import org.ggf.bes.factory.DestroyActivityResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class BESActivityDestroyActor implements OutcallActor
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(BESActivityDestroyActor.class);

	private EndpointReferenceType _activityEPR;

	public BESActivityDestroyActor(EndpointReferenceType activityEPR)
	{
		_activityEPR = activityEPR;
	}

	@Override
	public boolean enactOutcall(ICallingContext callingContext, EndpointReferenceType target, GeniiAttachment attachment) throws Throwable
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Persistent Outcall Actor attempting to destroy a bes activity.");

		GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, target, callingContext);

		// Now, go ahead and kill it.
		DestroyActivitiesResponseType resp = bes.destroyActivities(new DestroyActivitiesType(new EndpointReferenceType[] { _activityEPR }, null));
		if (resp != null) {
			DestroyActivityResponseType[] resps = resp.getResponse();
			if (resps != null && resps.length == 1) {
				if (resps[0].isDestroyed() || (resps[0].getFault() != null))
					return true;
				_logger.error("Response says that we didn't destroy the activity:  " + resps[0].getFault());
			}
		}

		_logger.error("Tried to destroy activity, but didn't get right number of response values back.");
		return false;
	}
}