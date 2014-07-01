package edu.virginia.vcgr.genii.container.invoker;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.client.stats.MethodDataPoint;
import edu.virginia.vcgr.genii.client.stats.MethodHistogramStatistics;
import edu.virginia.vcgr.genii.notification.broker.SubscriptionFailedFaultType;

public class DatabaseHandler implements IAroundInvoker
{
	static private Log _logger = LogFactory.getLog(DatabaseHandler.class);

	public Object invoke(InvocationContext invocationContext) throws AuthZSecurityException, SubscriptionFailedFaultType,
		IOException
	{
		Object result;
		boolean succeeded = false;

		if (invocationContext == null) {
			String msg = "failure: got a null invocation context!";
			_logger.error(msg);
			throw new AuthZSecurityException(msg);
		} else if (invocationContext.getTarget() == null) {
			String msg = "failure: got a null target in the invocation context!";
			_logger.error(msg);
			throw new AuthZSecurityException(msg);
		} else if (invocationContext.getMethod() == null) {
			String msg = "failure: got a null method in the invocation context!";
			_logger.error(msg);
			throw new AuthZSecurityException(msg);
		}

		MethodDataPoint mdp =
			ContainerStatistics.instance().getMethodStatistics()
				.startMethod(invocationContext.getTarget().getClass(), invocationContext.getMethod());
		MethodHistogramStatistics mhs = ContainerStatistics.instance().getMethodHistogramStatistics();

		mhs.addActiveMethod();
		try {
			result = invocationContext.proceed();
			mdp.complete(true);
			succeeded = true;
			return result;
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				// for invocation target problems, drop a level of reporting, since it is always useless.
				Throwable cause = e.getCause();
				if (cause instanceof Exception)
					e = cause;
			}
			
			if ((e.getCause() != null) && (e.getCause() instanceof SubscriptionFailedFaultType)) {
				_logger.info("subscription exception in dbhandler, rethrowing: " + e.getCause().getMessage());
			} else {
				_logger.error("exception occurred in dbhandler invoke: " + e.getMessage(), e);
			}
			throw new IOException(e.getLocalizedMessage(), e);
		} finally {
			mhs.removeActiveMethod();

			if (!succeeded) {
				_logger.info("Did not succeed at invoking method " + mdp.toString() + ".  Marking context appropriately.");
				mdp.complete(false);
				try {
					WorkingContext.getCurrentWorkingContext().setFailed();
				} catch (Throwable t) {
				}
			}
		}
	}
}
