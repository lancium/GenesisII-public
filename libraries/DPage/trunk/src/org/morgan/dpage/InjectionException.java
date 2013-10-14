package org.morgan.dpage;

public class InjectionException extends Exception
{
	static final long serialVersionUID = 0L;

	public InjectionException(String msg)
	{
		super(msg);
	}

	public InjectionException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}
