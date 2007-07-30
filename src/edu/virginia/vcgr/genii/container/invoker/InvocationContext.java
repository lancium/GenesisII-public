package edu.virginia.vcgr.genii.container.invoker;

import java.lang.reflect.Method;

import org.apache.axis.MessageContext;

public class InvocationContext
{
	private MessageContext _messageContext;
	private Object _target;
	private Method _method;
	private Object []_arguments;
	
	private IAroundInvoker []_handlers;
	private int _nextHandler = 0;
	
	InvocationContext(MessageContext messageContext,
		Object target, Method method, Object []arguments,
		IAroundInvoker []invokers)
	{
		_messageContext = messageContext;
		_target = target;
		_method = method;
		_arguments = arguments;
		
		_handlers = invokers;
	}
	
	public MessageContext getMessageContext()
	{
		return _messageContext;
	}
	
	public Object getTarget()
	{
		return _target;
	}
	
	public void setTarget(Object newTarget)
	{
		_target = newTarget;
	}
	
	public Method getMethod()
	{
		return _method;
	}
	
	public void setMethod(Method newMethod)
	{
		_method = newMethod;
	}
	
	public Object[] getArguments()
	{
		return _arguments;
	}
	
	public void setArguments(Object []newArguments)
	{
		_arguments = newArguments;
	}
	
	public Object proceed() throws Exception
	{
		if (_nextHandler >= _handlers.length)
			return _method.invoke(_target, _arguments);
		
		return _handlers[_nextHandler++].invoke(this);
	}
}