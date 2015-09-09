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
		/*
		 * from slurm docs: Job state, compact form: PD (pending), R (running), CA (cancelled), CF(configuring), CG (completing), CD
		 * (completed), F (failed), TO (timeout), and NF (node failure). See the JOB STATE CODES section below for more information. (Valid
		 * for jobs only)
		 */
		if (symbol.equalsIgnoreCase("PD") || symbol.equalsIgnoreCase("CF") || symbol.equalsIgnoreCase("CG")) {
			// these are considered queued states, since we're not running the job yet.
			return new SLURMQueueState("Queued", false, false);
		} else if (symbol.equalsIgnoreCase("R")) {
			// running is the same as executing.
			return new SLURMQueueState("Executing", false, false);
		} else if (symbol.equalsIgnoreCase("CA") || symbol.equalsIgnoreCase("CD")) {
			// cancelled and completed are considered normal exits.
			return new SLURMQueueState("Exiting", true, false);
		} else if (symbol.equalsIgnoreCase("F") || symbol.equalsIgnoreCase("NF") || symbol.equalsIgnoreCase("TO")) {
			// failed, node failure, and timeout are considered failure exits.
			return new SLURMQueueState("Exiting", true, true);
		}
		return new SLURMQueueState("Unknown", true, true);
	}
}
