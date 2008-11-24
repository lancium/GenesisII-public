package edu.virginia.vcgr.fsii.exceptions;

public class FSRuntimeException extends RuntimeException
{
	static final long serialVersionUID = 0L;
	
	public FSRuntimeException(FSException fse)
	{
		super(fse);
	}
}