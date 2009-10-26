package edu.virginia.vcgr.genii.container.cservices.gridlogger;

import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

import edu.virginia.vcgr.genii.client.gridlog.GridLogTarget;
import edu.virginia.vcgr.genii.client.gridlog.GridLogUtils;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;

public class GridLogDevice
{
	private GridLoggerContainerService _service;
	private Category _category;
	
	final private void logEvent(Collection<GridLogTarget> targets,
		Object message, Throwable cause)
	{
		LoggingEvent e = new LoggingEvent(
			_category.getName(), _category,
			System.currentTimeMillis(), Level.INFO, message, cause);
		
		_service.logEvent(e, targets);
	}
	
	public GridLogDevice(Class<?> logSource)
	{
		_category = Category.getInstance(logSource);
		_service = (GridLoggerContainerService)ContainerServices.findService(
			GridLoggerContainerService.SERVICE_NAME);
	}
	
	final public void log(Collection<GridLogTarget> targets,
		Object message, Throwable cause)
	{
		if (targets == null)
		{
			try
			{
				targets = GridLogUtils.getTargets();
			}
			catch (Throwable t)
			{
				targets = new Vector<GridLogTarget>();
			}
		}
		
		logEvent(targets, message, cause);
	}
	
	final public void log(Object message, Throwable cause)
	{
		log(null, message, cause);
	}
	
	final public void log(Collection<GridLogTarget> targets,
		Object message)
	{
		log(targets, message, null);
	}
	
	final public void log(Object message)
	{
		log(null, message, null);
	}
}