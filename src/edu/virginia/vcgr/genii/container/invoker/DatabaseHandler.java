package edu.virginia.vcgr.genii.container.invoker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.context.WorkingContext;

public class DatabaseHandler implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(DatabaseHandler.class);
	
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		Object result;
		boolean succeeded = false;
		
		try
		{
			result = invocationContext.proceed();
			succeeded = true;
			return result;
		}
		finally
		{
			if (!succeeded)
			{
				_logger.warn(
					"An error occurred while invoking method.  " +
					"Setting the context to failed.");
				try
				{		
					WorkingContext.getCurrentWorkingContext().setFailed();
				}
				catch (Throwable t)
				{
				}
			}
		}
	}
}