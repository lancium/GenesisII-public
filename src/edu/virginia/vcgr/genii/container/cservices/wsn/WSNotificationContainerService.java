package edu.virginia.vcgr.genii.container.cservices.wsn;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicyTypes;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionsDatabase;
import edu.virginia.vcgr.genii.container.common.notification.WSNSubscriptionInformation;
import edu.virginia.vcgr.genii.container.context.ClientConfig;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.percall.ExponentialBackoffScheduler;
import edu.virginia.vcgr.genii.container.cservices.percall.PersistentOutcallContainerService;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.notification.EnhancedNotificationBrokerServiceImpl;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerDBResource;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerDatabase;
import edu.virginia.vcgr.genii.container.notification.NotificationBrokerMessageManager;
import edu.virginia.vcgr.genii.container.notification.NotificationForwarder;

public class WSNotificationContainerService extends AbstractContainerService
{
	static final public String SERVICE_NAME = "WS Notification Service";

	static private Log _logger = LogFactory.getLog(WSNotificationContainerService.class);

	private ExecutorService _executor;
	private NotificationRateController _notificationRateController;

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

	public WSNotificationContainerService(Element configuration) throws JAXBException
	{
		super(SERVICE_NAME);

		int numThreads = 0;

		if (configuration != null) {
			JAXBContext context = JAXBContext.newInstance(WSNotificationConfiguration.class);
			Unmarshaller u = context.createUnmarshaller();
			WSNotificationConfiguration conf = (WSNotificationConfiguration) u.unmarshal(configuration);
			numThreads = conf.numThreads();
		} else {
			numThreads = 4;
		}

		_executor = Executors.newFixedThreadPool(numThreads);
		_notificationRateController = new NotificationRateController();
	}

	public <Type extends NotificationMessageContents> void publishNotification(String publisherKey,
		EndpointReferenceType publisherEPR, TopicPath topic, Type contents, GeniiAttachment attachment)
	{
		DatabaseConnectionPool pool = getConnectionPool();
		Connection conn = null;

		try {
			conn = pool.acquire(true);
			Collection<WSNSubscriptionInformation> subscriptions;

			if (_notificationRateController.notificationCanPass(publisherKey, topic, contents)) {
				subscriptions = SubscriptionsDatabase.subscriptionsForPublisher(conn, publisherKey, topic);
				forwardMessageToNotificationBrokers(Collections.singleton(publisherKey), publisherEPR, topic, contents, conn,
					subscriptions);
			} else
				return;

			if (contents.isUseIndirectPublishers()) {
				Set<String> indirectPublishers = getIndirectPublishers(publisherKey, contents, conn);
				if (indirectPublishers == null || indirectPublishers.isEmpty())
					return;
				Collection<WSNSubscriptionInformation> indirectSubscriptions = SubscriptionsDatabase
					.getSubscriptionsForIndirectPublishers(conn, indirectPublishers, topic);
				if (indirectSubscriptions != null) {
					subscriptions.addAll(indirectSubscriptions);
				}
				forwardMessageToNotificationBrokers(indirectPublishers, publisherEPR, topic, contents, conn, subscriptions);
			}

			for (WSNSubscriptionInformation subscription : subscriptions) {
				NotificationOutcallActor actor;

				actor = new NotificationOutcallActor(new NotificationMessageOutcallContent(
					subscription.subscriptionReference(), topic, publisherEPR, contents, subscription.additionalUserData()));

				boolean isPersistent = subscription.policies().containsKey(SubscriptionPolicyTypes.PersistentNotification);
				if (_logger.isDebugEnabled())
					_logger.debug("WSNotificationContainerService: isPersistent=" + isPersistent + " attachment="
						+ (attachment != null));

				if (isPersistent) {
					actor.setPersistent(true);
					PersistentOutcallContainerService service = ContainerServices
						.findService(PersistentOutcallContainerService.class);
					service.schedule(actor, new ExponentialBackoffScheduler(7L, TimeUnit.DAYS, null, null, 1L,
						TimeUnit.MINUTES, 30L, TimeUnit.MILLISECONDS), subscription.consumerReference(), null, attachment);
				} else {
					_executor.submit(new NotificationWorker(subscription.consumerReference(), actor, attachment));
				}
			}
		} catch (SQLException e) {
			_logger.warn("Unable to load subscriptions for publisher.", e);
		} catch (JAXBException e) {
			_logger.warn("Unable to load subscriptions for publisher.", e);
		} catch (IOException e) {
			_logger.warn("Unable to load subscriptions for publisher.", e);
		} finally {
			if (conn != null)
				pool.release(conn);
		}
	}

	public Integer getMessageIndexOfBroker(String clientId)
	{

		DatabaseConnectionPool pool = getConnectionPool();
		Connection conn = null;
		try {
			try {
				conn = pool.acquire(true);
				return NotificationBrokerDatabase.getMessageIndexOfBrokerByClientId(clientId, conn);
			} catch (SQLException e) {
				return null;
			}
		} finally {
			if (conn != null)
				pool.release(conn);
		}
	}

	/*
	 * For notifications that use subscription to some other resources to be passed to the consumer,
	 * we first retrieved the resource-keys of the resources that are responsible for passing the
	 * notifications. If all those resources are blocked from sending/propagating notification
	 * messages, we then safely ignore the published notification message.
	 */
	private Set<String> getIndirectPublishers(String publisherKey, NotificationMessageContents contents, Connection conn)
	{

		Set<String> indirectPublishers = SubscriptionsDatabase.getIndirectPublishersKeys(publisherKey,
			contents.getIndirectPublishersRetrieveQuery(), conn);
		if (indirectPublishers == null || indirectPublishers.isEmpty())
			return null;

		Iterator<String> iterator = indirectPublishers.iterator();
		while (iterator.hasNext()) {
			String indirectPublisher = iterator.next();
			if (!_notificationRateController.isPublisherBlocked(indirectPublisher))
				continue;
			Long blockingTime = _notificationRateController.getBlockadeCreationTime(indirectPublisher);
			if (blockingTime == null)
				continue;
			if (contents.isIgnoreBlockedIndirectPublisher(blockingTime)) {
				iterator.remove();
			}
		}
		return indirectPublishers;
	}

	private <Type> void forwardMessageToNotificationBrokers(Set<String> publisherKeys,
		EndpointReferenceType originalPublisherEPR, TopicPath topic, NotificationMessageContents contents,
		Connection connection, Collection<WSNSubscriptionInformation> subscriptions)
	{
		try {
			List<String> brokerSubscribers = getAndFilterSubscriptionsToBroker(subscriptions);
			if (brokerSubscribers != null && !brokerSubscribers.isEmpty()) {

				ClientConfig clientConfig = ClientConfig.getCurrentClientConfig();
				String clientId = (clientConfig == null) ? null : clientConfig.getClientId();

				List<NotificationBrokerDBResource> brokerDBResources = NotificationBrokerDatabase.getBrokers(publisherKeys,
					brokerSubscribers, clientId, connection);

				NotificationMessageOutcallContent outcallContent = new NotificationMessageOutcallContent(null, topic,
					originalPublisherEPR, contents, null);
				processNotificationMessageForBrokers(brokerDBResources, outcallContent, connection);
			}
		} catch (Exception e) {
			_logger.warn("Exception occurred while forwarding notification message to brokers", e);
		}
	}

	private List<String> getAndFilterSubscriptionsToBroker(Collection<WSNSubscriptionInformation> subscriptions)
		throws ResourceException
	{

		if (subscriptions == null || subscriptions.isEmpty())
			return null;

		List<String> brokerResourceKeys = new ArrayList<String>();
		Iterator<WSNSubscriptionInformation> iterator = subscriptions.iterator();
		String brokerUrlInContainer = Container.getServiceURL(EnhancedNotificationBrokerServiceImpl.PORT_NAME);

		while (iterator.hasNext()) {
			WSNSubscriptionInformation subscriptionInfo = iterator.next();
			EndpointReferenceType consumerReference = subscriptionInfo.consumerReference();
			if (brokerUrlInContainer.equalsIgnoreCase(consumerReference.getAddress().toString())) {
				AddressingParameters addressingParameters = new AddressingParameters(consumerReference.getReferenceParameters());
				brokerResourceKeys.add(addressingParameters.getResourceKey());
				iterator.remove();
			}
		}
		return brokerResourceKeys;
	}

	/*
	 * This method captures the details of handling broker-based notification messages. First it
	 * separates the brokers that can support out-calls from those that need message polling. Then
	 * it store the change-log for all brokers, update the message index for all of them, push the
	 * message in queues of pull-point type brokers, and finally use out-calls to notify active
	 * loggers.
	 */
	public void processNotificationMessageForBrokers(List<NotificationBrokerDBResource> brokerList,
		NotificationMessageOutcallContent message, Connection connection) throws Exception
	{

		if (brokerList == null || brokerList.isEmpty())
			return;

		List<NotificationBrokerDBResource> activeNotificationBrokers = new ArrayList<NotificationBrokerDBResource>();
		List<String> resourceIdsOfPassiveBrokers = new ArrayList<String>();

		for (NotificationBrokerDBResource resource : brokerList) {
			if (resource.isActiveMode()) {
				activeNotificationBrokers.add(resource);
			} else {
				resourceIdsOfPassiveBrokers.add(resource.getKey());
			}
		}

		// Update the message indices of all the brokers so that a message lost can be detected by
		// the client.
		NotificationBrokerDatabase.increaseMessageIndicesOfBrokers(brokerList, connection);

		// Populate the message queue of all brokers that are in passive(pull-point) mode.
		NotificationBrokerMessageManager.getManager().placeMessageInBrokerQueues(message, resourceIdsOfPassiveBrokers);

		// Make out-calls to pass the notification message to the clients that can receive direct
		// call-backs
		// from this container.
		ICallingContext callingContext = ContextManager.getExistingContext();
		_executor.submit(new NotificationForwarder(message, activeNotificationBrokers, callingContext));
	}

	private class NotificationWorker implements Runnable
	{
		private NotificationOutcallActor _actor;
		private EndpointReferenceType _target;
		private GeniiAttachment _attachment;

		private NotificationWorker(EndpointReferenceType target, NotificationOutcallActor actor, GeniiAttachment attachment)
		{
			_actor = actor;
			_target = target;
			_attachment = attachment;
		}

		@Override
		public void run()
		{
			try {
				_actor.enactOutcall(null, _target, _attachment);
			} catch (Throwable cause) {
				_logger.warn("Unable to send notification.", cause);
			}
		}
	}
}