package edu.virginia.vcgr.genii.container.cservices.wsn;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicyTypes;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionsDatabase;
import edu.virginia.vcgr.genii.container.common.notification.WSNSubscriptionInformation;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.percall.ExponentialBackoffScheduler;
import edu.virginia.vcgr.genii.container.cservices.percall.PersistentOutcallContainerService;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;

public class WSNotificationContainerService extends AbstractContainerService
{
	static final public String SERVICE_NAME = "WS Notification Service";

	static private Log _logger = LogFactory.getLog(
		WSNotificationContainerService.class);
		
	private ExecutorService _executor;
	
	@Override
	protected void loadService() throws Throwable
	{
		_logger.info(String.format("Loading %s.", SERVICE_NAME));
		
		// Nothing to do at the moment.
	}

	@Override
	protected void startService() throws Throwable
	{
		_logger.info(String.format("Starting %s.", SERVICE_NAME));
		
		// Nothing to do at the moment.
	}
	
	public WSNotificationContainerService(Element configuration)
		throws JAXBException
	{
		super(SERVICE_NAME);
		
		int numThreads = 0;
		
		if (configuration != null)
		{
			JAXBContext context = JAXBContext.newInstance(
				WSNotificationConfiguration.class);
			Unmarshaller u = context.createUnmarshaller();
			WSNotificationConfiguration conf =
				(WSNotificationConfiguration)u.unmarshal(configuration);
			numThreads = conf.numThreads();
		} else
		{
			numThreads = 4;
		}
		
		_executor = Executors.newFixedThreadPool(numThreads);
	}
	
	public <Type extends NotificationMessageContents> 
		void publishNotification(String publisherKey,
			EndpointReferenceType publisherEPR, TopicPath topic,
			Type contents, GeniiAttachment attachment)
	{
		DatabaseConnectionPool pool = getConnectionPool();
		Connection conn = null;
		
		try
		{
			conn = pool.acquire(true);
			Collection<WSNSubscriptionInformation> subscriptions =
				SubscriptionsDatabase.subscriptionsForPublisher(
					conn, publisherKey, topic);
			
			for (WSNSubscriptionInformation subscription : subscriptions)
			{
				NotificationOutcallActor actor;
				
				actor = new NotificationOutcallActor(
					new NotificationMessageOutcallContent(
						subscription.subscriptionReference(),
						topic, publisherEPR, contents,
						subscription.additionalUserData()));
				
				boolean isPersistent= subscription.policies().containsKey(
						SubscriptionPolicyTypes.PersistentNotification);
				_logger.debug("WSNotificationContainerService: isPersistent=" + isPersistent +
						" attachment=" + (attachment != null));
				
				if (isPersistent)
				{
					actor.setPersistent(true);
					PersistentOutcallContainerService service = 
						ContainerServices.findService(
							PersistentOutcallContainerService.class);
					service.schedule(
						actor, new ExponentialBackoffScheduler(
							7L, TimeUnit.DAYS, null, null,
							1L, TimeUnit.MINUTES, 30L, TimeUnit.MILLISECONDS),
						subscription.consumerReference(), null, attachment);
				}
				else
				{
					_executor.submit(new NotificationWorker(
						subscription.consumerReference(), actor, attachment));
				}
			}
		} 
		catch (SQLException e)
		{
			_logger.warn("Unable to load subscriptions for publisher.", e);
		}
		catch (JAXBException e)
		{
			_logger.warn("Unable to load subscriptions for publisher.", e);
		}
		catch (IOException e)
		{
			_logger.warn("Unable to load subscriptions for publisher.", e);
		}
		finally
		{
			if (conn != null)
				pool.release(conn);
		}
	}
	
	private class NotificationWorker implements Runnable
	{
		private NotificationOutcallActor _actor;
		private EndpointReferenceType _target;
		private GeniiAttachment _attachment;
		
		private NotificationWorker(
			EndpointReferenceType target,
			NotificationOutcallActor actor,
			GeniiAttachment attachment)
		{
			_actor = actor;
			_target = target;
			_attachment = attachment;
		}
		
		@Override
		public void run()
		{
			try
			{
				_actor.enactOutcall(null, _target, _attachment);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to send notification.", cause);
			}
		}
	}
}