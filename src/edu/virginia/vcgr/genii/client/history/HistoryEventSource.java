package edu.virginia.vcgr.genii.client.history;

public interface HistoryEventSource
{
	public HistoryEventSource alsoKnownAs();
	public HistoryEventSource alsoKnownAs(HistoryEventSource aka);
	
	public HistoryEventSource knownTo();
	
	public Object identity();
}