package edu.virginia.vcgr.genii.client.invoke;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Vector;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

public class InvocationInterceptorManager
{
	private HashMap<MethodDescription, Vector<OperationHandler>> _handlers = 
		new HashMap<MethodDescription, Vector<OperationHandler>>();
	
	public void addInterceptorClass(
		Object interceptorHandler) throws NoSuchMethodException
	{
		HashMap<MethodDescription, OperationHandler> handlers = 
			new HashMap<MethodDescription, OperationHandler>();
		
		Class<?> cl = interceptorHandler.getClass();
		analyzeClass(handlers, interceptorHandler, cl);
		
		for (MethodDescription md : handlers.keySet())
		{
			OperationHandler handler = handlers.get(md);
			
			synchronized(_handlers)
			{
				Vector<OperationHandler> vec = _handlers.get(md);
				if (vec == null)
					_handlers.put(md, vec = new Vector<OperationHandler>());
				vec.add(handler);
			}
		}
	}
	
	private void analyzeClass(HashMap<MethodDescription, OperationHandler> handlers,
		Object interceptor, Class<?> cl) throws NoSuchMethodException
	{
		if (cl.equals(Object.class))
			return;
		
		analyzeClass(handlers, interceptor, cl.getSuperclass());
		
		Method []methods = cl.getMethods();
		for (Method m : methods)
		{
			PipelineProcessor pProc = m.getAnnotation(PipelineProcessor.class);
			if (pProc != null)
			{
				addMethod(handlers, interceptor, cl, m, pProc);
			}
		}
	}
	
	private void addMethod(HashMap<MethodDescription, OperationHandler> handlers,
		Object interceptor, Class<?> cl, Method m, PipelineProcessor proc)
		throws NoSuchMethodException
	{
		Class<?> []interceptorPTypes = m.getParameterTypes();
		Class<?> []portTypePTypes;
		
		if (interceptorPTypes.length == 0)
			portTypePTypes = new Class<?>[0];
		else
		{
			if (!interceptorPTypes[0].equals(InvocationContext.class))
			{
				portTypePTypes = interceptorPTypes;
			} else
			{
				portTypePTypes = new Class<?>[interceptorPTypes.length - 1];
				for (int lcv = 0; lcv < portTypePTypes.length; lcv++)
					portTypePTypes[lcv] = interceptorPTypes[lcv + 1];
			}
		}
		
		Class<?> portType = proc.portType();
		
		if (!portType.isInterface())
			throw new RuntimeException("Port type class \"" + portType.getName() + "\" is not an interface.");
		
		String portMethod = proc.methodName();
		
		if (portMethod.length() == 0)
			portMethod = m.getName();
		
		portType.getMethod(portMethod, portTypePTypes);
		handlers.put(
			new MethodDescription(portMethod, portTypePTypes),
			new OperationHandler(interceptor, m, portTypePTypes.length == interceptorPTypes.length));
	}
	
	public Object invoke(EndpointReferenceType target, ICallingContext callingContext,
		IFinalInvoker finalObject, Method finalMethod, Object []finalParams) throws Throwable
	{
		Vector<OperationHandler> handlers;
		MethodDescription desc = new MethodDescription(finalMethod.getName(),
			finalMethod.getParameterTypes());
		
		synchronized(_handlers)
		{
			handlers = _handlers.get(desc);
			if (handlers == null)
				handlers = new Vector<OperationHandler>();
			else
				handlers = new Vector<OperationHandler>(handlers);
		}
		
		InvocationContext ctxt = new InvocationContext(handlers, target, callingContext, 
			finalParams, finalObject, finalMethod);
		return ctxt.proceed();
	}
}