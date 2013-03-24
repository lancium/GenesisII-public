package edu.virginia.vcgr.genii.container.common;

import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIACLManager;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.axis.AxisAcl;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributePreFetcher;
import edu.virginia.vcgr.genii.container.notification.EnhancedNotificationBrokerFactoryServiceImpl;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;
import edu.virginia.vcgr.genii.security.acl.Acl;

public class DefaultGenesisIIAttributesPreFetcher<Type extends IResource> extends AbstractAttributePreFetcher
{
	static private Log _logger = LogFactory.getLog(AbstractAttributePreFetcher.class);

	private Type _resource;

	public DefaultGenesisIIAttributesPreFetcher(Type resource)
	{
		_resource = resource;
	}

	@SuppressWarnings("unchecked")
	public DefaultGenesisIIAttributesPreFetcher(EndpointReferenceType target) throws ResourceException,
		ResourceUnknownFaultType
	{
		this((Type) ResourceManager.getTargetResource(target).dereference());
	}

	protected Type getResource()
	{
		return _resource;
	}

	protected Permissions getPermissions() throws Throwable
	{
		IResource resource = getResource();

		IAuthZProvider authZHandler = AuthZProviders.getProvider(resource.getParentResourceKey().getServiceName());
		AuthZConfig config = null;
		if (authZHandler != null)
			config = authZHandler.getAuthZConfig(resource);
		Acl acl = AxisAcl.decodeAcl(config);
		return GenesisIIACLManager.getPermissions(acl, QueueSecurity.getCallerIdentities(false));
	}

	protected AuthZConfig getAuthZConfig() throws Throwable
	{
		IResource resource = getResource();
		IAuthZProvider authZHandler = AuthZProviders.getProvider(resource.getParentResourceKey().getServiceName());
		AuthZConfig config = null;
		if (authZHandler != null)
			config = authZHandler.getAuthZConfig(resource);
		return config;
	}

	protected void fillInAttributes(Collection<MessageElement> attributes)
	{
		try {
			Permissions permissions = getPermissions();
			if (permissions != null) {
				attributes.add(new MessageElement(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME, permissions.toString()));
			}

			String brokerFactoryUrl = Container.getServiceURL(EnhancedNotificationBrokerFactoryServiceImpl.SERVICE_URL);
			MessageElement notificationBrokerFactoryElement = new MessageElement(
				GenesisIIConstants.NOTIFICATION_BROKER_FACTORY_ADDRESS, brokerFactoryUrl);
			attributes.add(notificationBrokerFactoryElement);
		} catch (Throwable cause) {
			_logger.warn("Unable to fill in permissions attribute.", cause);
		}
	}
}