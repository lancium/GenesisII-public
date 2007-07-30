package edu.virginia.vcgr.genii.client.notification;

import java.util.HashMap;

import org.morgan.util.GUID;

import edu.virginia.vcgr.genii.common.notification.Notify;

class NotificationTable
{
	private HashMap<GUID, INotificationListener> _listeners =
		new HashMap<GUID, INotificationListener>();
	
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
		
		listener.notify(subscription, notifyMessage.getSource(),
			notifyMessage.getTopic().toString(),
			notifyMessage.get_any());
	}
}