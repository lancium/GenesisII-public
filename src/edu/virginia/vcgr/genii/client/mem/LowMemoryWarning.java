package edu.virginia.vcgr.genii.client.mem;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryNotificationInfo;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.util.Collection;
import java.util.LinkedList;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

public class LowMemoryWarning
{
	static private final double PERCENTAGE_THRESHOLD = 0.95;
	
	static public LowMemoryWarning INSTANCE = new LowMemoryWarning();
	
	private Collection<LowMemoryHandler> _handlers = 
		new LinkedList<LowMemoryHandler>();
	
	final private void fireLowMemoryWarning(long usedMemory, long maxMemory)
	{
		synchronized(_handlers)
		{
			for (LowMemoryHandler handler : _handlers)
				handler.lowMemoryWarning(usedMemory, maxMemory);
		}
	}
	
	private LowMemoryWarning()
	{
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		NotificationEmitter emitter = (NotificationEmitter)mbean;
		emitter.addNotificationListener(new NotificationListenerImpl(),
			new NotificationFilterImpl(), null);
		
		for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans())
		{
			if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported())
			{
				long maxMemory = pool.getUsage().getMax();
				long warningThreshold = (long)(
					maxMemory * PERCENTAGE_THRESHOLD);
				pool.setUsageThreshold(warningThreshold);
			}
		}
	}
	
	final public void addLowMemoryListener(LowMemoryHandler handler)
	{
		synchronized(_handlers)
		{
			_handlers.add(handler);
		}
	}
	
	final public void removeLowMemoryListener(LowMemoryHandler handler)
	{
		synchronized(_handlers)
		{
			_handlers.remove(handler);
		}
	}
	
	private class NotificationListenerImpl implements NotificationListener
	{
		@Override
		public void handleNotification(Notification n, Object hb)
		{
			CompositeData cd = (CompositeData)n.getUserData();
			MemoryNotificationInfo info = MemoryNotificationInfo.from(cd);
			MemoryUsage usage = info.getUsage();
			fireLowMemoryWarning(usage.getUsed(), usage.getMax());
		}	
	}
	
	static private class NotificationFilterImpl implements NotificationFilter
	{
		static final long serialVersionUID = 0L;
		
		@Override
		public boolean isNotificationEnabled(Notification n)
		{
			return n.getType().equals(
				MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED);
		}	
	}
}