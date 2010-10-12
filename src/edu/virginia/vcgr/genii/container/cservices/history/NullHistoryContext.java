package edu.virginia.vcgr.genii.container.cservices.history;

import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;

public class NullHistoryContext extends AbstractHistoryContext
{
	public NullHistoryContext()
	{
		super(null, null);
	}
	
	@Override
	final public HistoryEventToken logEvent(HistoryEventLevel level,
		HistoryEventData data)
	{
		return new VerbatimHistoryEventToken(null);
	}
	
	@Override
	final public Object clone()
	{
		return new NullHistoryContext();
	}
}