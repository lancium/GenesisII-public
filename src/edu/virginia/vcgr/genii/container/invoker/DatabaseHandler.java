package edu.virginia.vcgr.genii.container.invoker;

import java.security.GeneralSecurityException;

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

		if (invocationContext == null) {
			String msg = "failure: got a null invocation context!";
			_logger.error(msg);
			throw new GeneralSecurityException(msg);
		} else if (invocationContext.getTarget() == null) {
			String msg = "failure: got a null target in the invocation context!";
			_logger.error(msg);
			throw new GeneralSecurityException(msg);
		} else if (invocationContext.getMethod() == null) {
			String msg = "failure: got a null method in the invocation context!";
			_logger.error(msg);
			throw new GeneralSecurityException(msg);
		}

		MethodDataPoint mdp = ContainerStatistics.instance().getMethodStatistics()
			.startMethod(invocationContext.getTarget().getClass(), invocationContext.getMethod());
		MethodHistogramStatistics mhs = ContainerStatistics.instance().getMethodHistogramStatistics();

		mhs.addActiveMethod();
		try {
			result = invocationContext.proceed();
			mdp.complete(true);
			succeeded = true;
			return result;
		} catch (Exception e) {
			_logger.error("exception occurred in dbhandler invoke: " + e.getMessage(), e);
			throw e;
		} finally {
			mhs.removeActiveMethod();

			if (!succeeded) {
				_logger.error("Did not succeed at invoking method " + mdp.toString() + ".  Setting the context to failed.");
				mdp.complete(false);
				try {
					WorkingContext.getCurrentWorkingContext().setFailed();
				} catch (Throwable t) {
				}
			}
		}
	}
}
