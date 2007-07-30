package edu.virginia.vcgr.genii.container.invoker;

public interface IAroundInvoker
{
	public Object invoke(InvocationContext invocationContext)
		throws Exception;
}