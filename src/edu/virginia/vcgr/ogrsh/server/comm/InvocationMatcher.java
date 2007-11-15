package edu.virginia.vcgr.ogrsh.server.comm;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.ogrsh.server.exceptions.OGRSHException;
import edu.virginia.vcgr.ogrsh.server.packing.DefaultOGRSHReadBuffer;

public class InvocationMatcher
{
	static private Log _logger = LogFactory.getLog(InvocationMatcher.class);
	
	static private class HandlerInformation
	{
		private String _invocationName;
		private Object _invocationObject;
		private Method _invocationMethod;
		
		public HandlerInformation(String invocationName, Object invocationObject,
			Method invocationMethod)
		{
			_invocationName = invocationName;
			_invocationObject = invocationObject;
			_invocationMethod = invocationMethod;
		}
		
		public String getInvocationName()
		{
			return _invocationName;
		}
		
		public Object getInvocationObject()
		{
			return _invocationObject;
		}
		
		public Method getInvocationMethod()
		{
			return _invocationMethod;
		}
	}
	
	private HashMap<String, HandlerInformation> _handlers =
		new HashMap<String, HandlerInformation>();
	
	public void addHandlerInstance(Object obj)
	{
		Class<?> cl = obj.getClass();
		for (Method m : cl.getMethods())
		{
			OGRSHOperation oper = m.getAnnotation(OGRSHOperation.class);
			if (oper != null)
			{
				String invocationName = oper.value();
				if (invocationName.length() == 0)
					invocationName = m.getName();
				
				_logger.trace("Adding method \"" +
					cl.getName() + "." + m.getName() + "\" as handler for function \""
					+ invocationName + "\".");
				
				_handlers.put(invocationName, 
					new HandlerInformation(invocationName, obj, m));
			}
		}
	}
	
	private long _lastEndTime = 0;
	public Object invoke(ByteBuffer request) throws OGRSHException
	{
		DefaultOGRSHReadBuffer requestReader = new DefaultOGRSHReadBuffer(request);
		String invocationName = null;
		long startTime = 0L;
		
		try
		{
			invocationName = String.class.cast(requestReader.readObject());
			startTime = System.currentTimeMillis();
			if (_lastEndTime > 0)
				System.err.println("\t\t" + (startTime - _lastEndTime) + " ms.");
			
			HandlerInformation hInfo = _handlers.get(invocationName);
			if (hInfo == null)
				throw new OGRSHException("Requested function \"" + invocationName 
					+ "\" is unknown.", OGRSHException.EXCEPTION_UNKNOWN_FUNCTION);
			
			Method m = hInfo.getInvocationMethod();
			Class<?> []paramTypes = m.getParameterTypes();
			Object[] params = new Object[paramTypes.length];
			for (int lcv = 0; lcv < params.length; lcv++)
				params[lcv] = requestReader.readObject();
			
			try
			{
				return m.invoke(hInfo.getInvocationObject(), params);
			}
			catch (InvocationTargetException ite)
			{
				Throwable cause = ite.getCause();
				if (cause instanceof OGRSHException)
					throw (OGRSHException)cause;
				
				_logger.error("Unknown exception occurred.", cause);
				throw new OGRSHException("Unknown exception occured.",
					OGRSHException.EXCEPTION_UNKNOWN);
			}
		}
		catch (IOException ioe)
		{
			_logger.warn("Corrupted request data found.", ioe);
			throw new OGRSHException("Corrupted request data found.",
				OGRSHException.EXCEPTION_CORRUPTED_REQUEST);
		}
		catch (IllegalAccessException iae)
		{
			_logger.error("Invalid handler found.", iae);
			throw new OGRSHException("Invalid handler found.",
				OGRSHException.EXCEPTION_UNKNOWN);
		}
		finally
		{
			_lastEndTime = System.currentTimeMillis();
			System.err.println(invocationName + ":\t" + (_lastEndTime - startTime) + " ms.");
		}
	}
}