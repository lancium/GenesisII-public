package edu.virginia.vcgr.genii.container.context;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.context.ContextException;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public class WorkingContext implements Closeable, Cloneable
{
	static private Log _logger = LogFactory.getLog(WorkingContext.class);
	
	static public final String TARGETED_SERVICE_NAME = "targeted-service-name";
	static public final String EPR_PROPERTY_NAME = "endpoint-reference-type";
	static public final String CURRENT_RESOURCE_KEY = "current-resource-key";
	static public final String CALLING_CONTEXT_KEY = "calling-context-key";
	static public final String CURRENT_CONTEXT_KEY = "current-context-key";
	static public final String MESSAGE_CONTEXT_KEY = "message-context-key";
	static public final String CERT_CHAIN_KEY = "cert-chain-key"; 
	static public final String EPI_KEY = "epi-key"; 
		
	static private ThreadLocal<WorkingContext> _currentWorkingContext =
		new ThreadLocal<WorkingContext>();
	
	private boolean _closed = false;
	private boolean _succeeded = true;
	
	static public boolean hasCurrentWorkingContext()
	{
		return _currentWorkingContext.get() != null;
	}
	
	static public WorkingContext getCurrentWorkingContext()
		throws ContextException
	{
		WorkingContext ret = _currentWorkingContext.get();
		if (ret == null)
			throw new ContextException("Working context is null.");
		
		return ret;
	}
	
	static public void setCurrentWorkingContext(WorkingContext ctxt)
	{
		WorkingContext ret = _currentWorkingContext.get();
		_currentWorkingContext.set(ctxt);
		
		if (ret != null)
			StreamUtils.close(ret);
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
		
		for (Object obj : _properties.values())
		{
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
		c._properties.remove(CURRENT_RESOURCE_KEY);
		return c;
	}
}