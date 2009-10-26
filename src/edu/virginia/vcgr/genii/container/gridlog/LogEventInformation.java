package edu.virginia.vcgr.genii.container.gridlog;

import org.apache.log4j.spi.LoggingEvent;

public class LogEventInformation
{
	private String _hostname;
	private LoggingEvent _event;
	
	LogEventInformation(String hostname, LoggingEvent event)
	{
		_hostname = hostname;
		_event = event;
	}
	
	final public String hostname()
	{
		return _hostname;
	}
	
	final public LoggingEvent event()
	{
		return _event;
	}
}