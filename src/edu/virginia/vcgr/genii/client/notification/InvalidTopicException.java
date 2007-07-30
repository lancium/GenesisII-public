package edu.virginia.vcgr.genii.client.notification;

import java.rmi.RemoteException;

/**
 * This exception is thrown when a string is used as a topic that doesn't match the
 * required topic syntax.
 * 
 * @author mmm2a
 */
public class InvalidTopicException extends RemoteException
{
	static final long serialVersionUID = 0L;
	
	/**
	 * Create a new InvalidTopicException with the given topic.
	 * 
	 * @param topicName The topic that isn't a valid topic expression.
	 */
	public InvalidTopicException(String topicName)
	{
		super("Topic string \"" + topicName + "\" is invalid.");
	}
}