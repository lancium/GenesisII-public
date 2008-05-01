package edu.virginia.vcgr.genii.client.nativeq.pbs;

import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;

public class PBSQueueState implements NativeQueueState
{
	private String _state;
	private boolean _isFinal;
	private boolean _isError;
	
	private PBSQueueState(String state, boolean isFinal, boolean isError)
	{
		_state = state;
		_isFinal = isFinal;
		_isError = isError;
	}
	
	@Override
	public boolean isFinalState()
	{
		return _isFinal;
	}
	
	@Override
	public boolean isError()
	{
		return _isError;
	}
	
	public String toString()
	{
		return _state;
	}
	
	static public PBSQueueState fromStateSymbol(String symbol)
	{
		for (int lcv = 0; lcv < symbol.length(); lcv++)
		{
			switch (symbol.charAt(lcv))
			{
				case 'R' :
					return new PBSQueueState("Executing", false, false);
				case 'E' :
				case 'X' :
					return new PBSQueueState("Exiting", true, false);
				case 'Q' :
					return new PBSQueueState("Queued", false, false);
				case 'H' :
					return new PBSQueueState("Held", false, false);
			}
		}
		
		return new PBSQueueState("Unknown", true, true);
	}
}