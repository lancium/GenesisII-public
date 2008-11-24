package edu.virginia.vcgr.fsii.exceptions;

public class FSInvalidFileHandleException extends FSException
{
	static final long serialVersionUID = 0L;
	
	public FSInvalidFileHandleException(String msg)
	{
		super(msg);
	}
}