package edu.virginia.vcgr.genii.container.cservices.percall;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.StreamCorruptedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.TerminateActivitiesResponseType;
import org.ggf.bes.factory.TerminateActivitiesType;
import org.ggf.bes.factory.TerminateActivityResponseType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;

public class BESActivityTerminatorActor implements OutcallActor
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(BESActivityTerminatorActor.class);
	
	private EndpointReferenceType _activityEPR;
	
	public BESActivityTerminatorActor(EndpointReferenceType activityEPR)
	{
		_activityEPR = activityEPR;
	}
	
	@Override
	public boolean enactOutcall(ICallingContext callingContext,
			EndpointReferenceType target) throws Throwable
	{
		_logger.debug(
			"Persistent Outcall Actor attempting to kill a bes activity.");
		
		GeniiBESPortType bes = ClientUtils.createProxy(
			GeniiBESPortType.class, target, callingContext);
		ClientUtils.setTimeout(bes, 8 * 1000);
		TerminateActivitiesResponseType resp = 
			bes.terminateActivities(new TerminateActivitiesType(
				new EndpointReferenceType[] { _activityEPR }, null));
		if (resp != null)
		{
			TerminateActivityResponseType []resps = resp.getResponse();
			if (resps != null && resps.length == 1)
			{
				if (resps[0].isTerminated())
					return true;
				_logger.warn(
					"Response says that we didn't terminate the activity:  "+ 
					resps[0].getFault());
			}
		}
		
		_logger.warn("Tried to kill activity, but didn't get right number of response values back.");
		return false;
	}
	
	private void writeObject(ObjectOutputStream out)
		throws IOException
	{
		EPRUtils.serializeEPR(out, _activityEPR);
	}

	private void readObject(ObjectInputStream in)
    	throws IOException, ClassNotFoundException
	{
		_activityEPR = EPRUtils.deserializeEPR(in);
	}

	@SuppressWarnings("unused")
	private void readObjectNoData() 
    	throws ObjectStreamException
	{
		throw new StreamCorruptedException();
	}
}