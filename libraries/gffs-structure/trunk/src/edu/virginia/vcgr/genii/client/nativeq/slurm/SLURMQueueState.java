package edu.virginia.vcgr.genii.client.nativeq.slurm;

import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;

public class SLURMQueueState implements NativeQueueState
{
	private String _state;
	private boolean _isFinal;
	private boolean _isError;

	private SLURMQueueState(String state, boolean isFinal, boolean isError)
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

	static public SLURMQueueState fromStateSymbol(String symbol)
	{
		for (int lcv = 0; lcv < symbol.length(); lcv++) {
			switch (symbol.charAt(lcv)) {
				case 'R':
				case 'E':
					return new SLURMQueueState("Executing", false, false);
				case 'X':
				case 'C':
					return new SLURMQueueState("Exiting", true, false);
				case 'Q':
					return new SLURMQueueState("Queued", false, false);
				case 'H':
					return new SLURMQueueState("Held", false, false);
			}
		}

		return new SLURMQueueState("Unknown", true, true);
	}
}
