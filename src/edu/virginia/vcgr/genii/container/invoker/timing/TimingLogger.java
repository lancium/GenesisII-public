package edu.virginia.vcgr.genii.container.invoker.timing;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

class TimingLogger
{
	static private Log _logger = LogFactory.getLog(TimingLogger.class);
	
	private PrintStream _stream = null;
	
	TimingLogger(String logFileName)
	{
		File userDir =
			ConfigurationManager.getCurrentConfiguration().getUserDirectory();
		File logFile = new File(userDir, logFileName);
		
		try
		{
			_stream = new PrintStream(logFile);
		}
		catch (IOException ioe)
		{
			_logger.warn("Unable to open timing log file.", ioe);
		}
	}
	
	synchronized void log(Class<?> serviceClass, Method targetMethod,
		Map<String, List<Long>> events)
	{
		if (_stream == null)
			return;
		
		synchronized(events)
		{
			if (events.size() <= 0)
				return;
			
			_stream.format("%s::%s:\n", 
				serviceClass.getName(), targetMethod.getName());
			
			for (String eventName : events.keySet())
			{
				_stream.format("\t%s {", eventName);
				List<Long> timeList = events.get(eventName);
				synchronized(timeList)
				{
					boolean first = true;
					for (Long time : timeList)
					{
						if (!first)
						{
							_stream.format(", ");
							first = false;
						}
						
						_stream.format("%d", time);
					}
				}
				_stream.format("}\n");
			}
		}
		
		_stream.flush();
	}
}