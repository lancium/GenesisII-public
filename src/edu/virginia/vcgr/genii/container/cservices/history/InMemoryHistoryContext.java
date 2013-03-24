package edu.virginia.vcgr.genii.container.cservices.history;

import java.util.Map;

import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;

class InMemoryHistoryContext extends AbstractHistoryContext
{
	private HistoryEventSource _source;
	private InMemoryHistoryEventSink _sink;

	InMemoryHistoryContext(InMemoryHistoryEventSink sink, HistoryEventSource source, Map<String, String> properties,
		HistoryEventCategory category)
	{
		super(properties, category);

		_source = source;
		_sink = sink;
	}

	@Override
	final public HistoryEventToken logEvent(HistoryEventLevel level, HistoryEventData data)
	{
		return _sink.add(category(), level, properties(), _source, data);
	}

	@Override
	final public Object clone()
	{
		return new InMemoryHistoryContext(_sink, _source, properties(), category());
	}
}