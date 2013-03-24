package edu.virginia.vcgr.genii.client.invoke;

import java.lang.reflect.Method;

public interface IFinalInvoker
{
	public Object finalInvoke(Object target, Method method, Object[] params) throws Throwable;
}