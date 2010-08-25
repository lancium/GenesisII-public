package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

public class TopicException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public TopicException(String msg)
	{
		super(msg);
	}
	
	public TopicException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}