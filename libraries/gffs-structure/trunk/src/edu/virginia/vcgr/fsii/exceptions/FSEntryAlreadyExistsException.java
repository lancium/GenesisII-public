package edu.virginia.vcgr.fsii.exceptions;

public class FSEntryAlreadyExistsException extends FSException
{
	static final long serialVersionUID = 0L;

	public FSEntryAlreadyExistsException(String msg)
	{
		super(msg);
	}

	public FSEntryAlreadyExistsException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}