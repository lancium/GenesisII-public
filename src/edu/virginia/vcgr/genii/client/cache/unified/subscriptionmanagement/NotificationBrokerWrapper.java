package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsn.base.GetMessages;
import org.oasis_open.wsn.base.GetMessagesResponse;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.Notify;
import org.oasis_open.wsn.base.UnableToGetMessagesFaultType;

import edu.virginia.vcgr.genii.client.cache.ResourceAccessMonitor;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.notification.broker.EnhancedNotificationBrokerPortType;
import edu.virginia.vcgr.genii.notification.broker.TestNotificationRequest;

/*
 * This is a wrapper class around the notification broker port-type that keep tracks of broker end-point's 
 * properties and manage periodic polling of notification messages when there is some NAT or firewall in 
 * the network. Apart from active or passive-polling modes, broker resource properties are stored to determine 
 * the lifetime of subscriptions made via the broker and to handle loss of messages.
 * */
public class NotificationBrokerWrapper
{

	private static Log _logger = LogFactory.getLog(NotificationBrokerWrapper.class);

	private static final int MAX_NUMBER_OF_TRIES_TO_CHANGE_MODE = 3;
	private static final long MODE_TEST_TRIAL_INTERVAL = 30 * 1000L; // thirty seconds

	private static final long NOTIFICATION_POLLING_SCHEDULE_TESTING_INTERVAL = 100L; // hundred
																						// milliseconds

	private boolean brokerInActiveMode;
	private String containerId;
	private EnhancedNotificationBrokerPortType brokerPortType;
	private int numberOfTriesToMakeActive;
	private Long lastModeTestTrialTimeInMillis;
	private int lastReceivedMessageIndex;
	private long brokerResourceTerminationTime;
	private long brokerCreationTime;

	// A flag to stop the notification poller thread safely when the broker is in polling mode.
	private boolean brokerDestroyed = false;

	public NotificationBrokerWrapper(EnhancedNotificationBrokerPortType brokerPortType, String containerId,
		long brokerResourceLifeTime, boolean mode, NotificationMultiplexer multiplexer)
	{
		this.brokerInActiveMode = mode;
		this.containerId = containerId;
		this.brokerPortType = brokerPortType;
		brokerResourceTerminationTime = System.currentTimeMillis() + brokerResourceLifeTime;
		brokerCreationTime = System.currentTimeMillis();
		this.lastReceivedMessageIndex = 0;
		new NotificationPoller().start();
	}

	public void setBrokerInActiveMode(boolean brokerInActiveMode)
	{
		this.brokerInActiveMode = brokerInActiveMode;
		if (brokerInActiveMode) {
			PollingIntervalDirectory.deletePollingTimeSettings(containerId);
		}
	}

	public EnhancedNotificationBrokerPortType getBrokerPortType()
	{
		return brokerPortType;
	}

	public void setBrokerPortType(EnhancedNotificationBrokerPortType brokerPortType)
	{
		this.brokerPortType = brokerPortType;
	}

	public boolean isBrokerModeVerified()
	{
		return brokerInActiveMode || (numberOfTriesToMakeActive >= MAX_NUMBER_OF_TRIES_TO_CHANGE_MODE);
	}

	public int getLastReceivedMessageIndex()
	{
		return lastReceivedMessageIndex;
	}

	public void setLastReceivedMessageIndex(int lastReceivedMessageIndex)
	{
		this.lastReceivedMessageIndex = lastReceivedMessageIndex;
	}

	public void updateReceivedMessageIndex(int newMessageIndex) throws Exception
	{
		if ((newMessageIndex - lastReceivedMessageIndex) > 1) {
			throw new Exception("some messages are missing");
		}
		lastReceivedMessageIndex = newMessageIndex;
	}

	public long getBrokerRemainingLifeTime()
	{
		return Math.max(0, (brokerResourceTerminationTime - System.currentTimeMillis()));
	}

	public void testBrokerMode()
	{
		if (lastModeTestTrialTimeInMillis != null) {
			long currentTimeInMillis = System.currentTimeMillis();
			if (currentTimeInMillis - lastModeTestTrialTimeInMillis > MODE_TEST_TRIAL_INTERVAL)
				return;
		}
		try {
			brokerPortType.testNotification(new TestNotificationRequest());
			numberOfTriesToMakeActive++;
			lastModeTestTrialTimeInMillis = System.currentTimeMillis();
		} catch (RemoteException e) {
			if (_logger.isDebugEnabled())
				_logger.debug("failed to test the broker for notification");
		}
	}

	public boolean isBrokerActive()
	{
		return brokerInActiveMode;
	}

	public boolean isBrokerJustCreated()
	{
		return (System.currentTimeMillis() - brokerCreationTime) < PollingIntervalDirectory.MINIMUM_POLLING_INTERVAL;
	}

	public boolean brokerExpired()
	{
		return (System.currentTimeMillis() >= brokerResourceTerminationTime);
	}

	public void destroyBroker()
	{

		// This line is important as the container automatically deletes the broker if a timeout
		// occurs. In that case, calling this method will result in an error. Rather this method
		// is supposed to be called when broker is extant and we want an early termination. To
		// avoid any mishap that may arise from an unintended use, the following conditional is
		// applied.
		if (brokerDestroyed || brokerExpired())
			return;

		try {
			if (_logger.isDebugEnabled())
				_logger.debug("destroying notification broker");
			brokerPortType.destroy(new Destroy());
		} catch (Exception ex) {
			if (_logger.isDebugEnabled())
				_logger.debug("Failed to destroy the broker manually, relying on resource timeout.");
		}
		brokerDestroyed = true;
	}

	private class NotificationPoller extends Thread
	{

		@Override
		public void run()
		{

			// Every cache management related thread that load or store information from the Cache
			// should have
			// unaccounted access to both CachedManager and RPCs to avoid getting mingled with Cache
			// access and
			// RPCs initiated by some user action. This is important to provide accurate statistics
			// on per container
			// resource usage.
			ResourceAccessMonitor.getUnaccountedAccessRight();

			PollingIntervalDirectory.storePollingTimeSettings(new PollingTimeSettings(containerId));

			while (!brokerInActiveMode && !brokerDestroyed && !brokerExpired()) {
				try {
					if (PollingIntervalDirectory.isPollingDue(containerId, NOTIFICATION_POLLING_SCHEDULE_TESTING_INTERVAL)) {

						GetMessagesResponse response = brokerPortType.getMessages(new GetMessages());
						PollingIntervalDirectory.notifyAboutPolling(containerId);

						if (response != null) {
							NotificationMessageHolderType[] notificationMessage = response.getNotificationMessage();
							if (notificationMessage != null && notificationMessage.length != 0) {
								Notify notification = new Notify(notificationMessage, response.get_any());
								NotificationBrokerDirectory.pushNotificationMessageToMultiplexerQueue(notification);
							} else {
								if (_logger.isTraceEnabled())
									_logger.trace("vain notification pulling request");
							}
						}
					} else
						Thread.sleep(NOTIFICATION_POLLING_SCHEDULE_TESTING_INTERVAL);
				} catch (InterruptedException e) {
					if (_logger.isDebugEnabled())
						_logger.debug("notification poller is interrupted");
				} catch (UnableToGetMessagesFaultType e) {
					if (_logger.isDebugEnabled())
						_logger.debug("Couldn't pull notification messages. Conservatively resetting the cache.");
					CacheManager.resetCachingSystem();
				} catch (ResourceUnknownFaultType e) {
					if (_logger.isDebugEnabled())
						_logger.debug("There is some problem with the broker. Conservatively resetting the cache.", e);
					CacheManager.resetCachingSystem();
				} catch (RemoteException e) {
					if (_logger.isDebugEnabled())
						_logger.debug("Unexpected RPC error. Conservatively resetting the cache.", e);
					CacheManager.resetCachingSystem();
				} catch (Exception e) {
					if (_logger.isDebugEnabled())
						_logger.debug("Unexpected error. Conservatively resetting the cache.", e);
					CacheManager.resetCachingSystem();
				}
			}
		}
	}
}
