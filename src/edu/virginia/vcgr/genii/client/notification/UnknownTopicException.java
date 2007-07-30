
package edu.virginia.vcgr.genii.client.notification;

import java.rmi.RemoteException;

/**
 * This exception is raised when a service endpoint tries to raise a notification on
 * a topic that it hasn't registered yet.
 * 
 * @author mmm2a
 */
public class UnknownTopicException extends RemoteException
{
	static final long serialVersionUID = 0L;
	
	/**
	 * Create a  new unknown topic with the given topic name.
	 * 
	 * @param topicName The name of the topic that isn't known.
	 */
	public UnknownTopicException(String topicName)
	{
		super("Topic \"" + topicName + "\" is not a registered topic.");
	}
}