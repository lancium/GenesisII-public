package edu.virginia.vcgr.genii.client.dialog;

public class DialogException extends Exception
{
	static final long serialVersionUID = 0L;

	public DialogException(String msg)
	{
		super(msg);
	}

	public DialogException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}