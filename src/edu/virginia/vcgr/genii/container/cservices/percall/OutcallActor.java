package edu.virginia.vcgr.genii.container.cservices.percall;

import java.io.Serializable;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

/**
 * This interface represents the ability to perform the outcall (or whatever action) that the client
 * wants to perform persistently until it works.
 * 
 * @author morgan
 */
public interface OutcallActor extends Serializable
{
	/**
	 * Go ahead and try to perform the outcall. If this method returns true, then assume that the
	 * outcall succeeded, otherwise, if it returns false or throws an exception, assume that it
	 * failed.
	 * 
	 * @param callingContext
	 *            The calling context to use when making the outcall.
	 * @param target
	 *            The target EPR to use as the target of the outcall.
	 * @return True for success, false for failure.
	 * @throws Throwable
	 */
	public boolean enactOutcall(ICallingContext callingContext, EndpointReferenceType target, GeniiAttachment attachment)
		throws Throwable;
}