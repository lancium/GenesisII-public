package edu.virginia.vcgr.genii.client.nativeq.sge;

import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;

public class SGEQueueState implements NativeQueueState
{
	private String _state;
	private boolean _isFinal;
	private boolean _isError;
	
	private SGEQueueState(String state, boolean isFinal, boolean isError)
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
	
	@Override
	public String toString()
	{
		return _state;
	}
	
	static public SGEQueueState fromStateString(String string)
		throws NativeQueueException
	{
		if (string.equals("pending"))
			return new SGEQueueState("Pending", false, false);
		else if (string.equals("running"))
			return new SGEQueueState("Running", false, false);
		else if (string.equals("error"))
			return new SGEQueueState("Error", true, true);
		else if (string.equals("finished"))
			return new SGEQueueState("Finished", true, false);
		else if (string.equals("zombie"))
			return new SGEQueueState("Zombie", true, true);
		else
			throw new NativeQueueException(String.format(
				"%s is not a known SGE Queue Job State.", string));
	}
}