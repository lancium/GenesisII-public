package edu.virginia.vcgr.genii.container.sync;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceLock;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.GamlAclChangeContents;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.security.RWXCategory;

/**
 * This class can handle AclChange messages for all resource types.
 * If the current working context contains credentials with write access to the current resource,
 * then update the resource's ACLs as specified in the ChangeContents message.
 */
public class GamlAclChangeNotificationHandler
	extends AbstractNotificationHandler<GamlAclChangeContents>
{
	static private Log _logger = LogFactory.getLog(GamlAclChangeNotificationHandler.class);
	
	public GamlAclChangeNotificationHandler()
	{
		super(GamlAclChangeContents.class);
	}

	public String handleNotification(TopicPath topicPath,
			EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference,
			GamlAclChangeContents contents) throws Exception
	{
		VersionVector remoteVector = contents.versionVector();
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = rKey.dereference();
		ResourceLock resourceLock = rKey.getResourceLock();
		String serviceName = rKey.getServiceName();
		if (!ServerWSDoAllReceiver.checkAccess(resource, RWXCategory.WRITE))
		{
			_logger.debug("GamlAclChangeNotificationHandler: permission denied");
			return NotificationConstants.FAIL;
		}
		IAuthZProvider authZHandler = AuthZProviders.getProvider(serviceName);
		try
		{
			resourceLock.lock();
			VersionVector localVector = (VersionVector) resource.getProperty(
					SyncProperty.VERSION_VECTOR_PROP_NAME);
			MessageFlags flags = VersionedResourceUtils.validateNotification(
					resource, localVector, remoteVector);
			if (flags.status != null)
				return flags.status;
			authZHandler.receiveAuthZConfig(contents, resource);
			VersionedResourceUtils.updateVersionVector(resource, localVector, remoteVector);
			// replay = flags.replay;
		}
		finally
		{
			resourceLock.unlock();
		}
		return NotificationConstants.OK;
	}
}

