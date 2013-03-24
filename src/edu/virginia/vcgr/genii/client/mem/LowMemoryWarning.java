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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LowMemoryWarning
{
	static private Log _logger = LogFactory.getLog(LowMemoryWarning.class);

	static private final double PERCENTAGE_THRESHOLD = 0.95;

	static public LowMemoryWarning INSTANCE = new LowMemoryWarning();

	private Collection<LowMemoryHandler> _handlers = new LinkedList<LowMemoryHandler>();

	final private void fireLowMemoryWarning(long usedMemory, long maxMemory)
	{
		synchronized (_handlers) {
			for (LowMemoryHandler handler : _handlers)
				handler.lowMemoryWarning(usedMemory, maxMemory);
		}
	}

	private LowMemoryWarning()
	{
		MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
		NotificationEmitter emitter = (NotificationEmitter) mbean;
		emitter.addNotificationListener(new NotificationListenerImpl(), new NotificationFilterImpl(), null);

		for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
			if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) {
				long maxMemory = pool.getUsage().getMax();
				long warningThreshold = (long) (maxMemory * PERCENTAGE_THRESHOLD);
				pool.setUsageThreshold(warningThreshold);
			}
		}
	}

	final public void addLowMemoryListener(LowMemoryHandler handler)
	{
		synchronized (_handlers) {
			_handlers.add(handler);
		}
	}

	final public void removeLowMemoryListener(LowMemoryHandler handler)
	{
		synchronized (_handlers) {
			_handlers.remove(handler);
		}
	}

	private class NotificationListenerImpl implements NotificationListener
	{
		@Override
		public void handleNotification(Notification n, Object hb)
		{
			ManagementFactory.getMemoryMXBean().gc();
			MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
			long threshold = (long) (usage.getMax() * PERCENTAGE_THRESHOLD);
			if (usage.getUsed() >= threshold)
				fireLowMemoryWarning(usage.getUsed(), usage.getMax());
			else
				_logger.info(String.format("We were about to run out of memory, but a forced "
					+ "garbage collection saved us.  " + "Current Memory usage is %.2f%%.", (double) usage.getUsed()
					/ (double) usage.getMax() * 100));
		}
	}

	static private class NotificationFilterImpl implements NotificationFilter
	{
		static final long serialVersionUID = 0L;

		@Override
		public boolean isNotificationEnabled(Notification n)
		{
			return n.getType().equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED);
		}
	}
}