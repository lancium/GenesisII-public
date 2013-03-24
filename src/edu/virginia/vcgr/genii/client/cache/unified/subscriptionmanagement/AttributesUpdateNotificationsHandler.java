package edu.virginia.vcgr.genii.client.cache.unified.subscriptionmanagement;

import java.util.Calendar;
import java.util.Date;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cache.unified.CacheUtils;
import edu.virginia.vcgr.genii.client.cache.unified.WSResourceConfig;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.AuthZConfigUpdateNotification;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOAttributesUpdateNotification;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;

/*
 * This is the handler class for processing any notification received for byteIO attributes update and access right 
 * change on any kind of resource.
 * */
public class AttributesUpdateNotificationsHandler
{

	private static Log _logger = LogFactory.getLog(AttributesUpdateNotificationsHandler.class);

	public static void handleByteIOAttributesUpdate(ByteIOAttributesUpdateNotification notification,
		EndpointReferenceType byteIOEndpoint)
	{

		String nameSpaceForAttributes = CacheUtils.getNamespaceForByteIOAttributes(byteIOEndpoint);
		if (nameSpaceForAttributes == null)
			return;

		if (notification.isPublisherBlockedFromFurtherNotifications()) {
			handleNotificationBlockage(notification, byteIOEndpoint, nameSpaceForAttributes);
		} else {
			// When notification is blocked, only the size and modification time attributes get
			// invalidated. So
			// we are updating these two attributes when notification is not blocked and the rest of
			// the attributes
			// all the time.
			QName sizeAttributeQName = new QName(nameSpaceForAttributes, ByteIOConstants.SIZE_ATTR_NAME);
			long size = notification.getSize();
			MessageElement sizeElement = new MessageElement(sizeAttributeQName, size);
			CacheManager.putItemInCache(byteIOEndpoint, sizeAttributeQName, sizeElement);

			QName modTimeAttributeQName = new QName(nameSpaceForAttributes, ByteIOConstants.MODTIME_ATTR_NAME);
			Calendar modificationTime = notification.getModificationTime();
			MessageElement modTimeElement = new MessageElement(modTimeAttributeQName, modificationTime);
			CacheManager.putItemInCache(byteIOEndpoint, modTimeAttributeQName, modTimeElement);
		}

		QName accessTimeAttributeQName = new QName(nameSpaceForAttributes, ByteIOConstants.ACCESSTIME_ATTR_NAME);
		Calendar accessTime = notification.getAccessTime();
		MessageElement accessTimeElement = new MessageElement(accessTimeAttributeQName, accessTime);
		CacheManager.putItemInCache(byteIOEndpoint, accessTimeAttributeQName, accessTimeElement);

		QName createTimeAttributeQName = new QName(nameSpaceForAttributes, ByteIOConstants.CREATTIME_ATTR_NAME);
		Calendar createTime = notification.getCreateTime();
		MessageElement createTimeElement = new MessageElement(createTimeAttributeQName, createTime);
		CacheManager.putItemInCache(byteIOEndpoint, createTimeAttributeQName, createTimeElement);
	}

	private static void handleNotificationBlockage(ByteIOAttributesUpdateNotification notification,
		EndpointReferenceType byteIOEndpoint, String nameSpaceForAttributes)
	{

		WSName byteIOName = new WSName(byteIOEndpoint);
		URI wsEndpointIdentifier = byteIOName.getEndpointIdentifier();

		// Remove size and modification time attributes from the cache, because these are the only
		// two attributes that
		// get affected by a blockade.
		CacheManager.removeItemFromCache(wsEndpointIdentifier,
			new QName(nameSpaceForAttributes, ByteIOConstants.SIZE_ATTR_NAME), MessageElement.class);
		CacheManager.removeItemFromCache(wsEndpointIdentifier, new QName(nameSpaceForAttributes,
			ByteIOConstants.MODTIME_ATTR_NAME), MessageElement.class);

		WSResourceConfig resourceConfig = (WSResourceConfig) CacheManager.getItemFromCache(wsEndpointIdentifier,
			WSResourceConfig.class);
		if (resourceConfig == null) {
			if (_logger.isDebugEnabled())
				_logger.debug("A notification blocking request received for a resource that has no cached configuration!");
		}
		// Update resource configuration to avoid re-insert of attributes within the blockade
		// period.
		long blockageTime = notification.getBlockageTime();
		long blockageExpiryTimeInMillis = System.currentTimeMillis() + blockageTime;
		resourceConfig.blockCacheAccess();
		resourceConfig.setCacheBlockageExpiryTime(new Date(blockageExpiryTimeInMillis));
		CacheManager.putItemInCache(wsEndpointIdentifier, resourceConfig);
	}

	public static void handleAuthZConfigUpdate(AuthZConfigUpdateNotification notification, EndpointReferenceType endPoint)
	{
		try {
			AuthZConfig config = notification.getNewConfig();
			if (config == null)
				return;
			CacheManager.putItemInCache(endPoint, GenesisIIBaseRP.AUTHZ_CONFIG_QNAME, new MessageElement(
				GenesisIIBaseRP.AUTHZ_CONFIG_QNAME, config));
		} catch (Exception e) {
			_logger.info("failed to process authzUpdate notification");
		}
	}
}
