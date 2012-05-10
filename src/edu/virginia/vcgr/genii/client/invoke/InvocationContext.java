package edu.virginia.vcgr.genii.client.invoke;

import java.lang.reflect.Method;
import java.util.Vector;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class InvocationContext
{
	private EndpointReferenceType _target;
	private ICallingContext _callingContext;
	private Vector<OperationHandler> _handlers;
	private IFinalInvoker _finalInvoker;
	private Method _finalMethod;
	private int _myStage;
	private Object []_params;
	
	InvocationContext(Vector<OperationHandler> handlers, EndpointReferenceType target,
		ICallingContext callingContext, Object []params, IFinalInvoker finalInvoker, Method finalMethod)
	{
		_target = target;
		_callingContext = callingContext;
		_handlers = handlers;
		_myStage = -1;
		_params = params;
		_finalInvoker = finalInvoker;
		_finalMethod = finalMethod;
	}
	
	private InvocationContext(InvocationContext old)
	{
		_target = old._target;
		_callingContext = old._callingContext;
		_handlers = old._handlers;
		_myStage = old._myStage + 1;
		_params = old._params;
		_finalInvoker = old._finalInvoker;
		_finalMethod = old._finalMethod;
	}
	
	public EndpointReferenceType getTarget()
	{
		return _target;
	}
	
	public ICallingContext getCallingContext()
	{
		return _callingContext;
	}
	
	public Object[] getParams() {
		return _params;
	}

	public void updateParams(Object[] newParams) {
		_params = newParams;
	}
	
	public Object proceed() throws Throwable
	{
		InvocationContext nextStage = new InvocationContext(this);
		if (nextStage._myStage >= nextStage._handlers.size())
			return _finalInvoker.finalInvoke(_finalInvoker, _finalMethod, _params);
		
		return nextStage._handlers.get(nextStage._myStage).handle(nextStage, _params);
	}
}