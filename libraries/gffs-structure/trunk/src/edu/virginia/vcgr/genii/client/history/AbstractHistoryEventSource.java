package edu.virginia.vcgr.genii.client.history;

import java.io.Serializable;

public abstract class AbstractHistoryEventSource implements HistoryEventSource, Serializable
{
	static final long serialVersionUID = 0L;

	private HistoryEventSource _aka;
	private HistoryEventSource _knownTo;

	protected AbstractHistoryEventSource(HistoryEventSource knownTo, HistoryEventSource aka)
	{
		_aka = aka;
		_knownTo = knownTo;
	}

	protected AbstractHistoryEventSource(HistoryEventSource knownTo)
	{
		this(knownTo, null);
	}

	@Override
	final public HistoryEventSource alsoKnownAs(HistoryEventSource aka)
	{
		HistoryEventSource ret = _aka;

		_aka = aka;

		return ret;
	}

	@Override
	final public HistoryEventSource alsoKnownAs()
	{
		return _aka;
	}

	@Override
	public HistoryEventSource knownTo()
	{
		return _knownTo;
	}
}