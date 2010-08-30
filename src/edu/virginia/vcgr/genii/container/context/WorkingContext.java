package edu.virginia.vcgr.genii.container.context;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import org.apache.axis.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextException;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class WorkingContext implements Closeable, Cloneable
{
	static private Log _logger = LogFactory.getLog(WorkingContext.class);
	
	static public final String TARGETED_SERVICE_NAME = "targeted-service-name";
	static public final String EPR_PROPERTY_NAME = "endpoint-reference-type";
	static public final String CURRENT_RESOURCE_KEY = "current-resource-key";
	static public final String CURRENT_CONTEXT_KEY = "current-context-key";
	static public final String MESSAGE_CONTEXT_KEY = "message-context-key";
	static public final String CERT_CHAIN_KEY = "cert-chain-key"; 
	static public final String EPI_KEY = "epi-key"; 
		
	static private ThreadLocal<Stack<WorkingContext>> _currentWorkingContext =
		new ThreadLocal<Stack<WorkingContext>>();
	
	private boolean _closed = false;
	private boolean _succeeded = true;
	
	static public boolean hasCurrentWorkingContext()
	{
		return (_currentWorkingContext.get() != null)
			&& (!_currentWorkingContext.get().isEmpty());
	}
	
	static public WorkingContext getCurrentWorkingContext()
		throws ContextException
	{
		Stack<WorkingContext> stack = _currentWorkingContext.get();
		if (stack == null || stack.isEmpty())
			throw new ContextException("Working context is null.");
		
		WorkingContext ret = stack.peek();
		
		return ret;
	}
	
	static public void setCurrentWorkingContext(WorkingContext ctxt)
	{
		Stack<WorkingContext> stack = _currentWorkingContext.get();
		Stack<WorkingContext> newStack = new Stack<WorkingContext>();
		_currentWorkingContext.set(newStack);
		newStack.push(ctxt);
		
		if (stack != null)
		{
			while (!stack.isEmpty())
			{
				StreamUtils.close(stack.pop());
			}
		}
	}
	
	static public void temporarilyAssumeNewIdentity(EndpointReferenceType newTarget)
		throws ContextException
	{
		Stack<WorkingContext> stack = _currentWorkingContext.get();
		if (stack == null || stack.isEmpty())
		{
			_currentWorkingContext.set(stack = new Stack<WorkingContext>());
			stack.push(new WorkingContext());
		}
		
		try
		{
			String serviceName = EPRUtils.extractServiceName(newTarget);
			WorkingContext newContext = stack.peek();
			if (newContext != null)
				newContext = (WorkingContext)newContext.clone();
			else
				newContext = new WorkingContext();
			newContext.setProperty(EPR_PROPERTY_NAME, newTarget);
			newContext.setProperty(TARGETED_SERVICE_NAME, serviceName);
			stack.push(newContext);
		}
		catch (AxisFault af)
		{
			throw new ContextException(
				"Unable to extract service name from EPR.", af);
		}
	}
	
	static public void releaseAssumedIdentity() throws ContextException
	{
		Stack<WorkingContext> stack = _currentWorkingContext.get();
		if (stack == null || stack.isEmpty())
			throw new ContextException("Working context is null.");
		WorkingContext old = stack.pop();
		StreamUtils.close(old);
		if (stack.isEmpty())
			throw new ContextException("Working context stack underflow.");
	}
	
	private HashMap<String, Object> _properties = new HashMap<String, Object>();
	
	public Object getProperty(String propertyName)
	{
		return _properties.get(propertyName);
	}
	
	public void setProperty(String propertyName, Object value)
	{
		Object obj = _properties.get(propertyName);
		if (obj != null)
		{
			try
			{
				if (obj instanceof ResourceKey)
					((ResourceKey)obj).dereference().rollback();
			}
			catch (Throwable t)
			{
				_logger.error(
					"Error committing/rolling-back database state.", t);
			}
			
			if (obj instanceof Closeable)
			{
				StreamUtils.close((Closeable)obj);
			}
		}
		_properties.put(propertyName, value);
	}
	
	public void removeProperty(String propertyName)
	{
		_properties.remove(propertyName);
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			StreamUtils.close(this);
		}
		finally
		{
			super.finalize();
		}
	}
	
	synchronized public void close() throws IOException
	{
		if (_closed)
			return;
		
		_closed = true;
		
		for (Map.Entry<String, Object> entry : _properties.entrySet())
		{
			Object obj = entry.getValue();
			
			try
			{
				if (obj instanceof ResourceKey)
				{
					if (_succeeded)
						((ResourceKey)obj).dereference().commit();
					else
						((ResourceKey)obj).dereference().rollback();
				}
			}
			catch (Throwable t)
			{
				_logger.error(
					"Error committing/rolling-back database state.", t);
			}
			
			if (obj instanceof Closeable)
			{
				StreamUtils.close((Closeable)obj);
			}
		}
		
		_properties.clear();
	}
	
	public void setFailed()
	{
		_succeeded = false;
	}
	
	@SuppressWarnings("unchecked")
	public Object clone()
	{
		WorkingContext c = new WorkingContext();
		c._properties = (HashMap<String, Object>)_properties.clone();
		Collection<String> removeSet = new LinkedList<String>();
		for (Map.Entry<String, Object> entry : c._properties.entrySet())
		{
			if (entry.getValue() instanceof ResourceKey)
				removeSet.add(entry.getKey());
		}
		
		for (String key : removeSet)
			c._properties.remove(key);
		
		return c;
	}
}