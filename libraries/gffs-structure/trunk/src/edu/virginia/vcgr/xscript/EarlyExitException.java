package edu.virginia.vcgr.xscript;

public class EarlyExitException extends Exception
{
	static final long serialVersionUID = 0L;

	private int _exitCode;

	public EarlyExitException(int exitCode)
	{
		_exitCode = exitCode;
	}

	public int getExitCode()
	{
		return _exitCode;
	}
}