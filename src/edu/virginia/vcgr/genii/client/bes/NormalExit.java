package edu.virginia.vcgr.genii.client.bes;

public class NormalExit extends ExitCondition
{
	private int _exitCode;
	
	public NormalExit(int exitCode)
	{
		_exitCode = exitCode;
	}
	
	@Override
	public String toString()
	{
		return Integer.toString(_exitCode);
	}
	
	public int exitCode()
	{
		return _exitCode;
	}
}