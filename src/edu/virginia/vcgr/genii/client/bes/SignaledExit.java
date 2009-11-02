package edu.virginia.vcgr.genii.client.bes;

public class SignaledExit extends ExitCondition
{
	private Signals _signal;
	
	public SignaledExit(Signals signal)
	{
		_signal = signal;
	}
	
	public Signals signal()
	{
		return _signal;
	}
	
	@Override
	public String toString()
	{
		return _signal.toString();
	}
}