package edu.virginia.vcgr.genii.container.cservices.percall;

import java.io.Serializable;
import java.util.Calendar;

/**
 * The attempt scheduler is an interface implemented by a persistent outcall agent or client that
 * creates a schedule of outcall attempts to be made.
 * 
 * @author morgan
 */
public interface AttemptScheduler extends Serializable
{
	/**
	 * This method is called by the PersistentOutcallContainerService any time a method call has
	 * been made and failed and the service needs to find out when it should next try and make the
	 * outcall.
	 * 
	 * @param now
	 *            The time from which the schedule is based
	 * @param numFailedAttempts
	 *            The number of attempts at the outcall that have thus far failed.
	 * @return The next time when the outcall should be attempted. This method returns null if we
	 *         are going to give up on making the out call.
	 */
	public Calendar nextAttempt(Calendar now, int numFailedAttempts);
}