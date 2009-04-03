package edu.virginia.vcgr.genii.client.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.mortbay.log.LogFactory;

import edu.virginia.vcgr.genii.client.naming.WSName;

public class AttributeCache
{
	static private Log _logger = LogFactory.getLog(AttributeCache.class);
	
	static private Collection<AttributeCacheFlushListener> _listeners =
		new LinkedList<AttributeCacheFlushListener>();
	
	/**
	 * Flush the contents of the attribute cache.
	 * 
	 * @param endpoint The endpoint whose attributes you want to
	 * flush.  If this is null, then the attributes from ALL endpoints
	 * will be flushed.
	 * @param attributes The qnames of the attributes to flush from
	 * the cache.  If this parameter is null or empty, then all attributes
	 * are flush (for the given endpoint), otherwise, only those specified
	 * are flushed.
	 */
	static public void flush(WSName endpoint,
		QName...attributes)
	{
		Collection<AttributeCacheFlushListener> listeners;
		
		synchronized(_listeners)
		{
			listeners = new ArrayList<AttributeCacheFlushListener>(
				_listeners);
		}
		
		for (AttributeCacheFlushListener listener : listeners)
		{
			try
			{
				listener.flush(endpoint, attributes);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to call flush listener.", cause);
			}
		}
	}
	
	static public void addFlushListener(AttributeCacheFlushListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.add(listener);
		}
	}
	
	static public void removeFlushListener(AttributeCacheFlushListener listener)
	{
		synchronized(_listeners)
		{
			_listeners.remove(listener);
		}
	}
}