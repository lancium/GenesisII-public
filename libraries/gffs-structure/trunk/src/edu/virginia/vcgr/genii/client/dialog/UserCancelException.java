package edu.virginia.vcgr.genii.client.dialog;

public class UserCancelException extends Exception
{
	static final long serialVersionUID = 0L;

	public UserCancelException()
	{
		super("User selected cancel.");
	}
}