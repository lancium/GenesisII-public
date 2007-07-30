package edu.virginia.vcgr.genii.client.naming;

import java.rmi.RemoteException;

/**
 * This exception is thrown when a string is used as a topic that doesn't match the
 * required topic syntax.
 * 
 * @author mmm2a
 */
public class NameResolutionFailedException extends RemoteException
{
	static final long serialVersionUID = 0L;
	
	/**
	 * Create a new NameResolutionFailedException.
	 */
	public NameResolutionFailedException()
	{
		super("Could not resolve name.");
	}
	
	/**
	 * Create a new InvalidTopicException with the given topic.
	 * 
	 * @param topicName The topic that isn't a valid topic expression.
	 */
	public NameResolutionFailedException(Throwable t)
	{
		super("Could not resolve name.", t);
	}
}