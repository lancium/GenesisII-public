package edu.virginia.vcgr.genii.container.gridlog;

import java.util.Collection;

import org.apache.log4j.spi.LoggingEvent;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface GridLogResource extends IResource
{
	public Collection<LogEventInformation> listEvents(String loggerID, boolean sort)
		throws ResourceException;
	public void append(String loggerID, LoggingEvent event, String hostname)
		throws ResourceException;
}