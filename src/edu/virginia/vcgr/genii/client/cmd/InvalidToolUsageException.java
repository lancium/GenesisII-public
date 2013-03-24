package edu.virginia.vcgr.genii.client.cmd;

public class InvalidToolUsageException extends ToolException
{
	static final long serialVersionUID = 0L;

	static final private String _INVALID_USAGE_MSG = "Invalid tool usage!";

	public InvalidToolUsageException()
	{
		super(_INVALID_USAGE_MSG);
	}

	public InvalidToolUsageException(String additionalMessage)
	{
		super(_INVALID_USAGE_MSG + "  " + additionalMessage);
	}
}