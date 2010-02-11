package edu.virginia.vcgr.genii.container.invoker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.client.stats.MethodDataPoint;
import edu.virginia.vcgr.genii.client.stats.MethodHistogramStatistics;
import edu.virginia.vcgr.genii.container.context.WorkingContext;

public class DatabaseHandler implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(DatabaseHandler.class);
	
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		Object result;
		boolean succeeded = false;
		
		MethodDataPoint mdp = ContainerStatistics.instance(
			).getMethodStatistics().startMethod(
				invocationContext.getTarget().getClass(),
				invocationContext.getMethod());
		MethodHistogramStatistics mhs = ContainerStatistics.instance(
			).getMethodHistogramStatistics();
		
		mhs.addActiveMethod();
		try
		{
			result = invocationContext.proceed();
			mdp.complete(true);
			succeeded = true;
			return result;
		}
		finally
		{
			mhs.removeActiveMethod();
			
			if (!succeeded)
			{
				_logger.warn(
					"An error occurred while invoking method.  " +
					"Setting the context to failed.");
				
				mdp.complete(false);
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