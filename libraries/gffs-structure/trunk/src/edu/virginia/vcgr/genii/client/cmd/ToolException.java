package edu.virginia.vcgr.genii.client.cmd;

public class ToolException extends Exception
{
	static final long serialVersionUID = 0L;

	public ToolException(String message)
	{
		super(message);
	}

	public ToolException(String message, Throwable cause)
	{
		super(message, cause);
	}
}