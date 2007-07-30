package edu.virginia.vcgr.genii.container.invoker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DebugInvoker implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(DebugInvoker.class);
	
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		String description = "method " + 
			invocationContext.getMethod().getName() +
			" on class " + 
			invocationContext.getTarget().getClass().getName() + ".";
		
		_logger.debug("Calling " + description);
		try
		{
			Object obj = invocationContext.proceed();
			_logger.debug("Successfully called " + description);
			return obj;
		}
		catch (Exception e)
		{
			_logger.debug("Failed call to " + description, e);
			throw e;
		}
	}
}