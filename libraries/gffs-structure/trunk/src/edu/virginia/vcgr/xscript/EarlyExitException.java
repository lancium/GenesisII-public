package edu.virginia.vcgr.xscript;

public class EarlyExitException extends Exception
{
	static final long serialVersionUID = 0L;

	private Integer _exitCode;

	public EarlyExitException(int exitCode)
	{
		_exitCode = exitCode;
	}

	public Integer getExitCode()
	{
		return _exitCode;
	}
}