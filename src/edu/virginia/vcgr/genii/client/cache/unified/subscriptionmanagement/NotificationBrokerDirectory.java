package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.wsn.base.Notify;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheUtils;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.NotificationHelper;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.notification.broker.EnhancedNotificationBrokerPortType;
import edu.virginia.vcgr.genii.notification.factory.BrokerWithForwardingPortCreateRequestType;
import edu.virginia.vcgr.genii.notification.factory.EnhancedNotificationBrokerFactoryPortType;
import edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType;

/*
 * This class keeps track of all working notification brokers registered on different GenesisII containers
 * the client is accessing.
 * */
public class NotificationBrokerDirectory {

	private static Log _logger = LogFactory.getLog(NotificationBrokerDirectory.class);
	private static final long LIFETIME_OF_BROKER = 60 * 60 * 1000L; // one hour

	private static Map<String, String> containerIdToBrokerFactoryMapping;
	
	// This set is used to reduce redundant string processing for already available factory URLs and 
	// to avoid updating the containerIdToBrokerFactoryMapping map unnecessarily.
	private static Set<String> factoryUrls;
	
	// This set keeps track of containers where a previous attempt to create a notification broker has
	// been failed. We adopt a conservative approach of not trying to create another broker in those 
	// container as the most likely cost of creation failure is lack of permission and any subsequent
	// call is supposed fail.
	private static Set<String> unaccesibleContainers;
	
	// An extra level of indirection is used to cope up with the case where the container is restarted
	// at a different port without client's notice. Note that in such a scenario existing notification
	// brokers will continue to work properly -- assuming that we have designed the brokers to be fault
	// tolerant -- but the factory URL will change. However, if we do not have any notification broker
	// created before the restart then without indirectly mapping container ID to factory EPR using two
	// different maps we will get stuck.
	private static Map<String, EndpointReferenceType> factoryUrlToServiceEPRMapping;
	
	// Unlike the factory we can safely map the broker end-points with container IDs as once we get the
	// end-point -- theoretically -- communication should continue even if the container experiences 
	// transient failure.
	private static Map<String, NotificationBrokerWrapper> containerIdToBrokerMapping;
	
	private static NotificationMultiplexer notificationMultiplexer;
	
	static {
		containerIdToBrokerFactoryMapping = new HashMap<String, String>();
		factoryUrls = new HashSet<String>();
		factoryUrlToServiceEPRMapping = new HashMap<String, EndpointReferenceType>();
		containerIdToBrokerMapping = 
				Collections.synchronizedMap(new HashMap<String, NotificationBrokerWrapper>());
		unaccesibleContainers = new HashSet<String>();
	}
	
	public static void storeBrokerFactoryAddress(MessageElement factoryConfigElement) {
		
		String factoryUrl = factoryConfigElement.getValue();
		int indexOfContainerIdParameter = factoryUrl.indexOf(EPRUtils.GENII_CONTAINER_ID_PARAMETER);

		if (indexOfContainerIdParameter == -1) return;
		if (factoryUrls.contains(factoryUrl)) return;

		//assumed URL pattern is https://.../$CONTAINER_ID_PARAM=VALUE...
		int containerIdBeginAt = 
			indexOfContainerIdParameter + EPRUtils.GENII_CONTAINER_ID_PARAMETER.length() + 1;
		int containerIDEndAt = factoryUrl.lastIndexOf("&", containerIdBeginAt);
		String containerId = (containerIDEndAt == -1) ? factoryUrl.substring(containerIdBeginAt) 
				: factoryUrl.substring(containerIdBeginAt, containerIDEndAt);
		
		factoryUrls.add(factoryUrl);
		containerIdToBrokerFactoryMapping.put(containerId, factoryUrl);
	}
	
	public static NotificationBrokerWrapper getNotificationBrokerForEndpoint(EndpointReferenceType epr, 
			EndpointReferenceType forwardingPort) {
		
		String containerId = CacheUtils.getContainerId(epr);
		if (containerId == null) return null;
		
		if (unaccesibleContainers.contains(containerId)) return null;
		
		if (containerIdToBrokerMapping.containsKey(containerId)) {
			NotificationBrokerWrapper brokerWrapper = containerIdToBrokerMapping.get(containerId);
			if (brokerWrapper.brokerExpired()) {
				brokerWrapper.destroyBroker();
				containerIdToBrokerMapping.remove(containerId);
			} else return brokerWrapper;
		}
		
		String factoryUrl = getNotificationBrokerFacotoryUrl(epr, containerId);
		if (factoryUrl == null) return null;
		
		try {
			EndpointReferenceType factoryEPR = factoryUrlToServiceEPRMapping.get(factoryUrl);
			if (factoryEPR == null) {
				factoryEPR = EPRUtils.makeEPR(factoryUrl);
				factoryUrlToServiceEPRMapping.put(factoryUrl, factoryEPR);
			}
			EnhancedNotificationBrokerPortType brokerPortType = createNewBroker(forwardingPort, factoryEPR);
			NotificationBrokerWrapper wrapper = new NotificationBrokerWrapper(brokerPortType, 
					containerId, LIFETIME_OF_BROKER, false, notificationMultiplexer);
			containerIdToBrokerMapping.put(containerId, wrapper);
			return wrapper;
		} catch (Exception ex) {
			_logger.debug("could not create notification broker: " + ex.getMessage());
			unaccesibleContainers.add(containerId);
		}
		return null;
	}
	
	public static NotificationBrokerWrapper getExistingRepresentativeBroker(EndpointReferenceType targetResource) {
		String containerId = CacheUtils.getContainerId(targetResource);
		if (containerId == null) return null;
		return containerIdToBrokerMapping.get(containerId);
	}
	
	public static NotificationBrokerWrapper getExistingRepresentativeBroker(String containerId) {
		return containerIdToBrokerMapping.get(containerId);
	}
	
	// This method is invoked by the notification message receivers. If it receives a notification message from 
	// notification broker of a particular container, that indicates that direct notifications will work. So the
	// status of the broker is updated to active mode, which informs it to deliver notifications using out-calls 
	// instead of storing them and waiting for a pull request.
	public static void updateBrokerModeToActive(EndpointReferenceType brokerEPR) {
		String containerId = CacheUtils.getContainerId(brokerEPR);
		if (containerId == null) {
			_logger.info("Could not retrieve containerId from broker EPR!");
			return;
		}
		NotificationBrokerWrapper brokerWrapper = containerIdToBrokerMapping.get(containerId);
		try {
			EnhancedNotificationBrokerPortType brokerPortType = brokerWrapper.getBrokerPortType();
			brokerPortType.updateMode(true);
			brokerWrapper.setBrokerInActiveMode(true);
		} catch (RemoteException e) {
			_logger.info("Failed to put the broker in active mode.");
		}
	}
	
	public static void registerNotificationMulitplexer(NotificationMultiplexer multiplexer) {
		notificationMultiplexer = multiplexer;
	}
	
	public static void pushNotificationMessageToMultiplexerQueue(Notify notification) {
		if (notificationMultiplexer == null) {
			throw new RuntimeException("no notification multiplexer is registered in notification broker directory");
		}
		NotificationHelper.notify(notification, notificationMultiplexer);
	}
	
	public static void clearDirectory() {
		for (NotificationBrokerWrapper broker : containerIdToBrokerMapping.values()) {
			broker.destroyBroker();
		}
		containerIdToBrokerMapping.clear();
	}
	
	private static EnhancedNotificationBrokerPortType createNewBroker(EndpointReferenceType forwardingPort,
			EndpointReferenceType factoryEPR) throws ResourceException,
			GenesisIISecurityException, RemoteException, NotificationBrokerCreationFailedFaultType {
		
		EnhancedNotificationBrokerFactoryPortType factoryPort = ClientUtils.createProxy(
				EnhancedNotificationBrokerFactoryPortType.class, factoryEPR);
		BrokerWithForwardingPortCreateRequestType request = new BrokerWithForwardingPortCreateRequestType();
		request.setNotificationForwardingPort(forwardingPort);
		request.setNotificationBrokerLifetime(LIFETIME_OF_BROKER);
		EndpointReferenceType brokerEndpoint =  factoryPort.createNotificationBrokerWithForwardingPort(request);
		EnhancedNotificationBrokerPortType brokerPortType = 
			ClientUtils.createProxy(EnhancedNotificationBrokerPortType.class, brokerEndpoint);
		return brokerPortType;
	}
	
	private static String getNotificationBrokerFacotoryUrl(EndpointReferenceType epr, String containerId) {
		
		String factoryUrl = null;
		
		if (containerIdToBrokerFactoryMapping.containsKey(containerId)) {
			factoryUrl = containerIdToBrokerFactoryMapping.get(containerId);
		} else {
			try {
				GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, epr);
				GetResourcePropertyResponse resp = 
					common.getResourceProperty(GenesisIIConstants.NOTIFICATION_BROKER_FACTORY_ADDRESS);
				if (resp != null && resp.get_any() != null) {
					MessageElement element = resp.get_any()[0];
					factoryUrl = element.getValue();
					factoryUrls.add(factoryUrl);
					containerIdToBrokerFactoryMapping.put(containerId, factoryUrl);
				}
			} catch (Exception ex) {
				_logger.info("failed to retrieve notification-broker-factory address", ex);
			}
		}
		return factoryUrl;
	}
}
