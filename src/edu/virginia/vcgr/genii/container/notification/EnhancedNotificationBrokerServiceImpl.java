package edu.virginia.vcgr.genii.container.notification;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsn.base.DestroyPullPoint;
import org.oasis_open.wsn.base.DestroyPullPointResponse;
import org.oasis_open.wsn.base.GetMessages;
import org.oasis_open.wsn.base.GetMessagesResponse;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.Notify;
import org.oasis_open.wsn.base.SubscribeResponse;
import org.oasis_open.wsn.base.UnableToDestroyPullPointFaultType;
import org.oasis_open.wsn.base.UnableToGetMessagesFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.NotificationMessageHolder;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.DefaultSubscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeRequest;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.Subscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.TerminationTimeType;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.context.ClientConfig;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.notification.broker.EnhancedNotificationBrokerPortType;
import edu.virginia.vcgr.genii.notification.broker.IndirectSubscriptionEntryType;
import edu.virginia.vcgr.genii.notification.broker.IndirectSubscriptionType;
import edu.virginia.vcgr.genii.notification.broker.MessageMissedFaultType;
import edu.virginia.vcgr.genii.notification.broker.SubscriptionFailedFaultType;
import edu.virginia.vcgr.genii.notification.broker.TestNotificationRequest;
import edu.virginia.vcgr.genii.notification.broker.TestNotificationResponse;
import edu.virginia.vcgr.genii.notification.broker.UpdateForwardingPortResponse;
import edu.virginia.vcgr.genii.notification.broker.UpdateModeResponse;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = NotificationBrokerDBResourceProvider.class)
@ConstructionParametersType(NotificationBrokerConstructionParams.class)
public class EnhancedNotificationBrokerServiceImpl extends GenesisIIBase implements EnhancedNotificationBrokerPortType,
	NotificationBrokerTopics
{

	public static final String PORT_NAME = "EnhancedNotificationBrokerPortType";

	private static final long LIFETIME_TERMINATION_SAFETY_INTERVAL = 60 * 1000; // one minute
	private static final long SUBSCRIPTION_TERMINATION_SAFETY_INTERVAL = 30 * 1000; // thirty
																					// seconds

	private static Log _logger = LogFactory.getLog(EnhancedNotificationBrokerServiceImpl.class);

	public EnhancedNotificationBrokerServiceImpl() throws RemoteException
	{
		super(PORT_NAME);
	}

	public EnhancedNotificationBrokerServiceImpl(String serviceName) throws RemoteException
	{
		super(PORT_NAME);
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		HashMap<QName, Object> constructionParameters, Collection<MessageElement> resolverCreationParameters)
		throws ResourceException, BaseFaultType, RemoteException
	{

		super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParameters);

		NotificationBrokerDBResource resource = (NotificationBrokerDBResource) rKey.dereference();
		NotificationBrokerConstructionParams brokerConstructionParams = (NotificationBrokerConstructionParams) cParams;
		boolean activeMode = Boolean.TRUE.equals(brokerConstructionParams.getMode());
		EndpointReferenceType forwardingPort = brokerConstructionParams.getForwardingPort();
		String resourceId = rKey.getResourceKey();
		ClientConfig clientConfig = ClientConfig.getCurrentClientConfig();
		String clientId = (clientConfig == null) ? null : clientConfig.getClientId();
		resource.createNotificationBroker(resourceId, activeMode, 0, clientId, forwardingPort);

		long lifeTime = brokerConstructionParams.getScheduledTerminationTime();
		super.setScheduledTerminationTime(getTerminationTime(lifeTime), rKey);
	}

	@RWXMapping(RWXCategory.WRITE)
	@Override
	public UpdateModeResponse updateMode(boolean updateModeRequest) throws RemoteException
	{
		NotificationBrokerDBResource resource = (NotificationBrokerDBResource) ResourceManager.getCurrentResource()
			.dereference();
		resource.updateModeInDB(updateModeRequest);
		return null;
	}

	/*
	 * The purpose of test notifications is to verify whether or not the client can directly receive
	 * notification messages from the broker through its forwarding port. That is why the Notify
	 * message is created directly, instead of going through subscriptions and usual publication
	 * oriented notification mechanism. If you have used that process than it will both increase the
	 * number of verification related RPCs, and create the chance of race condition where the client
	 * will poll an test notification message and change its mode to direct notification.
	 */
	@RWXMapping(RWXCategory.EXECUTE)
	@Override
	public TestNotificationResponse testNotification(TestNotificationRequest testNotificationRequest) throws RemoteException
	{

		NotificationBrokerDBResource resource = (NotificationBrokerDBResource) ResourceManager.getCurrentResource()
			.dereference();

		resource.initializeResourceFromDB();
		EndpointReferenceType forwardingPort = resource.getForwardingPort();
		if (forwardingPort != null) {
			NotificationMessageHolder holder = new NotificationMessageHolder(forwardingPort, getMyEPR(false),
				TEST_NOTIFICAION_TOPIC, new TestNotificationMessageContents());
			try {
				ICallingContext callingContext = ContextManager.getExistingContext();
				Notify notification = new Notify(new NotificationMessageHolderType[] { holder.toAxisType() }, null);
				GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, forwardingPort, callingContext);
				common.notify(notification);
			} catch (Exception ex) {
				_logger.warn("failed to send a test notification message", ex);
			}
		} else {
			_logger.info("requested a dummy notification where there was no forwaring port");
		}
		return null;
	}

	@RWXMapping(RWXCategory.WRITE)
	@Override
	public IndirectSubscriptionEntryType[] createIndirectSubscriptions(IndirectSubscriptionType indirectSubscribeRequest)
		throws RemoteException, SubscriptionFailedFaultType
	{

		EndpointReferenceType publisher = indirectSubscribeRequest.getPublisher();
		long subscriptionLifeTime = indirectSubscribeRequest.getDuration();
		Duration duration = new Duration(subscriptionLifeTime);
		TerminationTimeType terminationTime = TerminationTimeType.newInstance(duration);
		final EndpointReferenceType myEPR = getMyEPR(true);
		IndirectSubscriptionEntryType[] response = new IndirectSubscriptionEntryType[3];

		try {
			// Subscribe to change in directory content represented by the RNS resource
			TopicQueryExpression topicFilter = RNSTopics.RNS_CONTENT_CHANGE_TOPIC.asConcreteQueryExpression();
			Subscription subscription = createSubscription(publisher, myEPR, topicFilter, terminationTime);
			EndpointReferenceType subscriptionReference = subscription.subscriptionReference();
			response[0] = new IndirectSubscriptionEntryType(new MessageElement[] { new MessageElement(
				NotificationBrokerConstants.INDIRECT_SUBSCRIPTION_TYPE,
				NotificationBrokerConstants.RNS_CONTENT_CHANGE_SUBSCRIPTION) }, subscriptionReference);

			// Subscribe to attributes update on byteIOs that are children of the directory
			// represented by the
			// RNS resource. This subscription works for only those byteIOs that are in the same
			// container.
			topicFilter = ByteIOTopics.BYTEIO_ATTRIBUTES_UPDATE_TOPIC.asConcreteQueryExpression();
			subscription = createSubscription(publisher, myEPR, topicFilter, terminationTime);
			subscriptionReference = subscription.subscriptionReference();
			response[1] = new IndirectSubscriptionEntryType(new MessageElement[] { new MessageElement(
				NotificationBrokerConstants.INDIRECT_SUBSCRIPTION_TYPE,
				NotificationBrokerConstants.BYTEIO_ATTRIBUTE_CHANGE_SUBSCRIPTION) }, subscriptionReference);

			// Subscribe to authorization parameter update on the RNS resources and byteIOs that are
			// children of
			// the directory represented by the RNS resource. For byteIOs this subscription works
			// for only those
			// that are in the same container as this RNS resource.
			topicFilter = GenesisIIBaseTopics.AUTHZ_CONFIG_UPDATE_TOPIC.asConcreteQueryExpression();
			subscription = createSubscription(publisher, myEPR, topicFilter, terminationTime);
			subscriptionReference = subscription.subscriptionReference();
			response[2] = new IndirectSubscriptionEntryType(new MessageElement[] { new MessageElement(
				NotificationBrokerConstants.INDIRECT_SUBSCRIPTION_TYPE,
				NotificationBrokerConstants.RESOURCE_AUTHORIZATION_CHANGE_SUBSCRIPTION) }, subscriptionReference);
		} catch (Exception ex) {
			_logger.info("Subscription request has been failed: " + ex.getMessage());
			final SubscriptionFailedFaultType subscriptionFault = new SubscriptionFailedFaultType(null, Calendar.getInstance(),
				publisher, null, new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(
					"Unable to create subscriptions.") }, null);
			throw subscriptionFault;
		}

		NotificationBrokerDBResource resource = (NotificationBrokerDBResource) ResourceManager.getCurrentResource()
			.dereference();

		long subscriptionEndTime = System.currentTimeMillis() + subscriptionLifeTime + SUBSCRIPTION_TERMINATION_SAFETY_INTERVAL;
		resource.storeSubscriptionTracesInDB(extractSubscriptionEPIs(response), publisher, subscriptionEndTime);

		return response;
	}

	@RWXMapping(RWXCategory.READ)
	@Override
	public GetMessagesResponse getUnreadMessages(BigInteger getUnreadMessagesRequest) throws RemoteException,
		MessageMissedFaultType
	{
		NotificationBrokerDBResource resource = (NotificationBrokerDBResource) ResourceManager.getCurrentResource()
			.dereference();
		resource.loadMessageIndexFromDB();
		int messageIndex = resource.getMessageIndex();
		int clientsMessageIndex = getUnreadMessagesRequest.intValue();
		NotificationBrokerMessageManager manager = NotificationBrokerMessageManager.getManager();
		List<OnHoldNotificationMessage> unsentMessages = manager.getMessageQueueOfBroker(resource.getKey());
		int countDifference = messageIndex - clientsMessageIndex;
		if (unsentMessages != null) {
			countDifference -= unsentMessages.size();
		}
		if (countDifference > 0) {
			// replenish the message queue so that the client can retrieve the messages using
			// the getMessages() method of the pull point interface, if it wants to.
			if (unsentMessages != null) {
				manager.setMessageQueueOfBroker(resource.getKey(), unsentMessages);
			}
			final MessageMissedFaultType messageMissedFault = new MessageMissedFaultType(null, Calendar.getInstance(), null,
				null, new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Messages are missing") }, null);
			throw messageMissedFault;
		}
		GetMessagesResponse response = manager.getMessagesResponseFromHeldMessages(unsentMessages, messageIndex);
		return response;
	}

	@Override
	public DestroyPullPointResponse destroyPullPoint(DestroyPullPoint destroyPullPointRequest) throws RemoteException,
		UnableToDestroyPullPointFaultType, ResourceUnknownFaultType
	{
		return null;
	}

	@RWXMapping(RWXCategory.READ)
	@Override
	public GetMessagesResponse getMessages(GetMessages getMessagesRequest) throws RemoteException,
		UnableToGetMessagesFaultType, ResourceUnknownFaultType
	{

		NotificationBrokerDBResource resource = (NotificationBrokerDBResource) ResourceManager.getCurrentResource()
			.dereference();
		NotificationBrokerMessageManager manager = NotificationBrokerMessageManager.getManager();
		List<OnHoldNotificationMessage> unsentMessages = manager.getMessageQueueOfBroker(resource.getKey());
		resource.loadMessageIndexFromDB();
		int messageIndex = resource.getMessageIndex();
		GetMessagesResponse response = manager.getMessagesResponseFromHeldMessages(unsentMessages, messageIndex);
		return response;
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.ENHANCED_NOTIFICATION_BROKER_PORT;
	}

	@RWXMapping(RWXCategory.WRITE)
	@Override
	public UpdateForwardingPortResponse updateForwardingPort(EndpointReferenceType forwardingPort) throws RemoteException
	{
		return null;
	}

	@RWXMapping(RWXCategory.EXECUTE)
	@Override
	public void notify(Notify msg) throws RemoteException
	{

		NotificationBrokerDBResource resource = (NotificationBrokerDBResource) ResourceManager.getCurrentResource()
			.dereference();
		resource.initializeResourceFromDB();

		EndpointReferenceType forwardingPort = resource.getForwardingPort();

		if (resource.isActiveMode() && forwardingPort != null) {
			try {
				ICallingContext callingContext = ContextManager.getExistingContext();
				GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, forwardingPort, callingContext);
				common.notify(msg);
			} catch (Exception ex) {
				_logger.warn("failed to forward notification message", ex);
			}
		}
	}

	private List<String> extractSubscriptionEPIs(IndirectSubscriptionEntryType[] subscriptionEntries)
	{
		List<String> subscriptionEPIs = new ArrayList<String>();
		for (IndirectSubscriptionEntryType entry : subscriptionEntries) {
			EndpointReferenceType subscriptionReference = entry.getSubscriptionReference();
			String subscriptionEPI = new WSName(subscriptionReference).getEndpointIdentifier().toString();
			subscriptionEPIs.add(subscriptionEPI);
		}
		return subscriptionEPIs;
	}

	private Subscription createSubscription(EndpointReferenceType publisherEPR, EndpointReferenceType brokerEPR,
		TopicQueryExpression topicFilter, TerminationTimeType terminationTime) throws Exception
	{

		boolean assumedNewContext = false;
		try {
			WorkingContext.temporarilyAssumeNewIdentity(publisherEPR);
			assumedNewContext = true;
			SubscribeRequest request = new SubscribeRequest(brokerEPR, topicFilter, terminationTime, null);
			SubscribeResponse response = subscribe(request.asRequestType());
			return new DefaultSubscription(response);
		} finally {
			if (assumedNewContext) {
				WorkingContext.releaseAssumedIdentity();
			}
		}
	}

	// A small time period is added with the lifetime suggested by the client to accommodate
	// any timing difference between the client and the container.
	private Calendar getTerminationTime(long suggestedLifeTime)
	{
		Calendar timeToDeath = Calendar.getInstance();
		timeToDeath.setTimeInMillis(System.currentTimeMillis() + suggestedLifeTime + LIFETIME_TERMINATION_SAFETY_INTERVAL);
		return timeToDeath;
	}
}
