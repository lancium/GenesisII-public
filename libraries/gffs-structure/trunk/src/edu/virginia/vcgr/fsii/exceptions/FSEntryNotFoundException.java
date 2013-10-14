package edu.virginia.vcgr.fsii.exceptions;

public class FSEntryNotFoundException extends FSException
{
	static final long serialVersionUID = 0L;

	public FSEntryNotFoundException(String msg)
	{
		super(msg);
	}

	public FSEntryNotFoundException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}