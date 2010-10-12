package edu.virginia.vcgr.genii.container.cservices.history;

import java.util.Calendar;
import java.util.Map;

import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;

class DefaultHistoryContext extends AbstractHistoryContext
{
	private HistoryContainerService _service;
	
	private String _resourceID;
	private HistoryEventSource _source;
	private Long _ttl = null;
	
	DefaultHistoryContext(String resourceID, Map<String, String> properties,
		HistoryEventCategory category, HistoryEventSource source, Long ttl)
	{
		super(properties, category);
		
		_service = ContainerServices.findService(
			HistoryContainerService.class);
		
		_resourceID = resourceID;
		_source = source;
		_ttl = ttl;
	}
	
	@Override
	final public HistoryEventToken logEvent(HistoryEventLevel level, 
		HistoryEventData data)
	{
		Calendar expirationTime = null;
		
		if (_ttl != null)
		{
			expirationTime = Calendar.getInstance();
			expirationTime.setTimeInMillis(
				expirationTime.getTimeInMillis() + _ttl);
		}
		
		return _service.addRecord(
			_resourceID, null, category(), level, properties(), _source,
			data, expirationTime);
	}
	
	@Override
	final public Object clone()
	{
		return new DefaultHistoryContext(
			_resourceID, properties(), category(), _source, _ttl);
	}
}