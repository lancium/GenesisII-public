package edu.virginia.vcgr.genii.client.cmd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;

public class ExceptionHandlerManager
{
	static private Log _logger = LogFactory.getLog(ExceptionHandlerManager.class);
	
	static private IExceptionHandler _handler = null;
	
	@SuppressWarnings("unchecked")
	synchronized static public IExceptionHandler getExceptionHandler()
	{
		if (_handler == null)
		{
			_handler = 
				(IExceptionHandler)NamedInstances.getClientInstances().lookup(
					"exception-handler");
			
			if (_handler == null)
			{
				_logger.error(
					"No exception handler configured for the client.  " +
					"Using Debug version.");
				_handler = new DebugExceptionHandler();
			}
		}
		
		return _handler;
	}
}