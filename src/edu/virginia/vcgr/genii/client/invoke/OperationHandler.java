package edu.virginia.vcgr.genii.client.invoke;

import java.lang.reflect.*;

class OperationHandler
{
	private Object _handler;
	private Method _handlerMethod;

	private boolean _isFinalHandler;

	OperationHandler(Object handler, Method handlerMethod, boolean isFinalHandler)
	{
		_handler = handler;
		_handlerMethod = handlerMethod;
		_isFinalHandler = isFinalHandler;
	}

	Object handle(InvocationContext context, Object[] params) throws Throwable
	{
		if (_isFinalHandler) {
			return _handlerMethod.invoke(_handler, params);
		} else {
			Object[] p = new Object[params.length + 1];
			p[0] = context;
			System.arraycopy(params, 0, p, 1, params.length);
			try {
				return _handlerMethod.invoke(_handler, p);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
	}
}