package edu.virginia.vcgr.genii.container.cservices.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis.description.JavaServiceDesc;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventSource;
import edu.virginia.vcgr.genii.client.history.SimpleStringHistoryEventSource;
import edu.virginia.vcgr.genii.client.history.WSNamingHistoryEventSource;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class HistoryContextFactory
{
	static private Log _logger = LogFactory.getLog(HistoryContextFactory.class);
	
	static final String CALLING_CONTEXT_PROPERTIES_KEY =
		"edu.virginia.vcgr.genii.container.cservices.history.properties-key";
	
	static private ICallingContext callingContext(ICallingContext context)
	{
		try
		{
			if (context == null)
				context = ContextManager.getCurrentContext();
			
			return context;
		}
		catch (Throwable cause)
		{
			_logger.warn(
				"Error attempting to get calling context for history.", cause);
			
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	static public Map<String, String> getContextProperties(
		ICallingContext callingContext)
	{
		Map<String, String> ret = new HashMap<String, String>();
		
		callingContext = callingContext(callingContext);
		if (callingContext != null)
		{
			Collection<Serializable> properties =
				callingContext.getProperty(CALLING_CONTEXT_PROPERTIES_KEY);
			if (properties != null)
			{
				for (Serializable value : properties)
				{
					Pair<String, String> pair =
						(Pair<String, String>)value;
					ret.put(pair.first(), pair.second());
				}
			}
		}
		
		return ret;
	}
	
	static public void addContextProperty(ICallingContext callingContext,
		String propertyName, String propertyValue)
	{
		callingContext = callingContext(callingContext);
		if (callingContext != null)
		{
			Map<String, String> current = getContextProperties(callingContext);
			current.put(propertyName, propertyValue);
			Collection<Serializable> list = new ArrayList<Serializable>(
				current.size());
			for (Map.Entry<String, String> entry : current.entrySet())
			{
				list.add(new Pair<String, String>(
					entry.getKey(), entry.getValue()));
			}
			
			callingContext.setProperty(CALLING_CONTEXT_PROPERTIES_KEY,
				list);
		}
	}
	
	static public String removeContextProperty(ICallingContext callingContext,
		String propertyName)
	{
		String ret = null;
		callingContext = callingContext(callingContext);
		if (callingContext != null)
		{
			Map<String, String> current = getContextProperties(callingContext);
			ret = current.remove(propertyName);
			Collection<Serializable> list = new ArrayList<Serializable>(
				current.size());
			for (Map.Entry<String, String> entry : current.entrySet())
			{
				list.add(new Pair<String, String>(
					entry.getKey(), entry.getValue()));
			}
			
			callingContext.setProperty(CALLING_CONTEXT_PROPERTIES_KEY,
				list);
		}
		
		return ret;
	}
	
	/**
	 * This factory method is somewhat complicated.  I need a bunch of 
	 * things which usually the default is fine.  However, because the 
	 * defaults may not be fine, the choices are to have a dozen constructors,
	 * have constructors that take nulls, or to allow folks to pass in 
	 * arbitrary objects like this.  The objects that may be given in the 
	 * constructor are as follows:
	 * 	<UL>
	 * 		<LI>An IResource or a ResourceKey, or a String</LI>
	 * 		<LI>An ICallingContext</LI>
	 * 		<LI>A HistoryEventSource, or an EPR or WSName</LI>
	 * 		<LI>A Long representing the time to live</LI>
	 * 		<LI>An InMemoryHistoryEventSink if the context is going to log to an in-memory list</LI>
	 * 	</UL>
	 * @param contextObjects See above for description.
	 */
	static public HistoryContext createContext(HistoryEventCategory category,
		Object...contextObjects)
	{
		ResourceKey rKey = null;
		ICallingContext callingContext = null;
		String resourceID = null;
		HistoryEventSource source = null;
		Map<String, String> properties = null;
		Long ttl = null;
		InMemoryHistoryEventSink sink = null;
		
		if (category == null)
			category = HistoryEventCategory.Default;
		
		for (Object contextObject : contextObjects)
		{
			if (contextObject instanceof String)
				resourceID = (String)contextObject;
			else if (contextObject instanceof IResource)
				rKey = ((IResource)contextObject).getParentResourceKey();
			else if (contextObject instanceof ResourceKey)
				rKey = (ResourceKey)contextObject;
			else if (contextObject instanceof ICallingContext)
				callingContext = (ICallingContext)contextObject;
			else if (contextObject instanceof HistoryEventSource)
				source = (HistoryEventSource)contextObject;
			else if (contextObject instanceof EndpointReferenceType)
				source = new WSNamingHistoryEventSource(
					(EndpointReferenceType)contextObject, null);
			else if (contextObject instanceof WSName)
				source = new WSNamingHistoryEventSource(
					(WSName)contextObject, null);
			else if (contextObject instanceof Long)
				ttl = (Long)contextObject;
			else if (contextObject instanceof InMemoryHistoryEventSink)
				sink = (InMemoryHistoryEventSink)contextObject;
			else
				_logger.warn(String.format("Don't know what to do with a " +
					"%s while constructing a HistoryContext.  Ignoring it.",
					contextObject.getClass()));
		}
		
		if (resourceID == null)
		{
			if (rKey == null)
			{
				try
				{
					rKey = ResourceManager.getCurrentResource();
					resourceID = rKey.getResourceKey();
				}
				catch (Throwable cause)
				{
					_logger.error(
						"Unable to create history context...." +
						"creating null context.");
					
					return new NullHistoryContext();
				}
			}
		}
		
		properties = getContextProperties(callingContext);
		
		if (source == null)
		{
			EndpointReferenceType epr = null;
			try
			{
				WorkingContext context =
					WorkingContext.getCurrentWorkingContext();
				epr = (EndpointReferenceType)context.getProperty(
					WorkingContext.EPR_PROPERTY_NAME);
				
				if (epr != null)
				{
					JavaServiceDesc desc = Container.findService(epr);
					if (desc != null)
					{
						Class<?> cl = desc.getImplClass();
						if (GenesisIIBase.class.isAssignableFrom(cl))
						{
							GenesisIIBase base = (GenesisIIBase)cl.newInstance();
							epr = base.getMyEPR(true);
						}
					}
				}
			}
			catch (Throwable cause)
			{
				_logger.warn(
					"Unable to get current EPR for source identification.", 
					cause);
			}
			
			if (epr != null)
				source = new WSNamingHistoryEventSource(epr, null);
			else
				source = new SimpleStringHistoryEventSource(
					"Unknown Source", null);
		}
		
		if (sink != null)
			return new InMemoryHistoryContext(sink, source, properties, category);
		else
			return new DefaultHistoryContext(resourceID, properties, 
				category, source, ttl);
	}
}