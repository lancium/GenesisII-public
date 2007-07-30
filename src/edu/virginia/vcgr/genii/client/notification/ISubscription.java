package edu.virginia.vcgr.genii.client.notification;

/**
 * This interface is created by Notification servers and returned to clients when they
 * subscribe to a topic at a target.
 * 
 * @author mmm2a
 */
public interface ISubscription
{
	/**
	 * Cancel the subscription that this subscription instance represents.
	 */
	public void cancel();
}