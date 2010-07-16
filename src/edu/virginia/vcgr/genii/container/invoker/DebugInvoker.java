package edu.virginia.vcgr.genii.container.invoker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.naming.NamingUtils;

public class DebugInvoker implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(DebugInvoker.class);
	
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		String description = "method " + 
			invocationContext.getMethod().getName() +
			" on class " + 
			invocationContext.getTarget().getClass().getName() + ".";
		
		_logger.trace("Calling " + description + " from a "
			+ (NamingUtils.isWSNamingAwareClient() ? "" : "non-") + "WS-Naming aware client.");
		
		long start = System.currentTimeMillis();
		
		try
		{
			Object obj = invocationContext.proceed();
			long stop = System.currentTimeMillis();
			
			_logger.debug(String.format("Successfully called %s in %d ms.",
				description, (stop - start)));
			
			return obj;
		}
		catch (Exception e)
		{
			long stop = System.currentTimeMillis();
			
			_logger.debug(String.format("Failed to call %s in %d ms.",
				description, (stop - start)), e);
			
			throw e;
		}
	}
}