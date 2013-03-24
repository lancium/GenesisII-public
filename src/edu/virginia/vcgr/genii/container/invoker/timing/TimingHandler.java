package edu.virginia.vcgr.genii.container.invoker.timing;

import edu.virginia.vcgr.genii.container.invoker.IAroundInvoker;
import edu.virginia.vcgr.genii.container.invoker.InvocationContext;

public class TimingHandler implements IAroundInvoker
{
	@Override
	public Object invoke(InvocationContext invocationContext) throws Exception
	{
		TimingSink sink = TimingSink.createTimingSink(invocationContext.getTarget().getClass(), invocationContext.getMethod());

		try {
			return invocationContext.proceed();
		} finally {
			sink.log();
		}
	}
}