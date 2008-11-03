package edu.virginia.vcgr.genii.client.notification;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.common.notification.Notify;

class NotificationTable
{
	static private Log _logger = LogFactory.getLog(NotificationTable.class);
	
	private HashMap<GUID, INotificationListener> _listeners =
		new HashMap<GUID, INotificationListener>();
	private HashMap<Pattern, INotificationHandler> _handlers =
		new HashMap<Pattern, INotificationHandler>();
	
	public void addEntry(Pattern topicPattern, INotificationHandler handler)
	{
		synchronized(_handlers)
		{
			_handlers.put(topicPattern, handler);
		}
	}
	
	public GUID addEntry(INotificationListener listener)
	{
		GUID key;
		synchronized(_listeners)
		{
			while (true)
			{
				key = new GUID();
				if (_listeners.containsKey(key))
					continue;
				_listeners.put(key, listener);
				break;
			}
		}
		
		return key;
	}
	
	public void remove(GUID subscriptionKey)
	{
		synchronized(_listeners)
		{
			_listeners.remove(subscriptionKey);
		}
	}
	
	public void notify(GUID subscriptionKey, ISubscription subscription,
		Notify notifyMessage)
	{
		INotificationListener listener;
		
		synchronized(_listeners)
		{
			listener = _listeners.get(subscriptionKey);
		}
		
		try
		{
			listener.notify(subscription, notifyMessage.getSource(),
				notifyMessage.getTopic().toString(),
				notifyMessage.get_any());
		}
		catch (Throwable cause)
		{
			_logger.warn("Exception thrown while handling notification.", 
				cause);
		}
	}
	
	public void notify(Notify notifyMessage)
	{
		Collection<INotificationHandler> handlers = 
			new LinkedList<INotificationHandler>();
		String topic = notifyMessage.getTopic().toString();
		
		synchronized(_handlers)
		{
			for (Map.Entry<Pattern, INotificationHandler> handler : 
				_handlers.entrySet())
			{
				if (handler.getKey().matcher(topic).matches())
					handlers.add(handler.getValue());
			}
		}
		
		for (INotificationHandler handler : handlers)
		{
			try
			{
				handler.notify(notifyMessage);
			}
			catch (Throwable cause)
			{
				_logger.warn("Exception thrown while handling notification.", 
					cause);
			}
		}
	}
}