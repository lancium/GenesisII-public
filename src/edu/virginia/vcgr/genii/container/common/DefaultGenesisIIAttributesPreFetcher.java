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
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributePreFetcher;
import edu.virginia.vcgr.genii.container.notification.EnhancedNotificationBrokerFactoryServiceImpl;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.security.authz.providers.AuthZProviders;
import edu.virginia.vcgr.genii.container.security.authz.providers.IAuthZProvider;

public class DefaultGenesisIIAttributesPreFetcher<Type extends IResource> extends AbstractAttributePreFetcher
{
	static private Log _logger = LogFactory.getLog(AbstractAttributePreFetcher.class);

	private Type _resource;

	public DefaultGenesisIIAttributesPreFetcher(Type resource)
	{
		_resource = resource;
	}

	@SuppressWarnings("unchecked")
	public DefaultGenesisIIAttributesPreFetcher(EndpointReferenceType target) throws ResourceException, ResourceUnknownFaultType
	{
		this((Type) ResourceManager.getTargetResource(target).dereference());
	}

	protected Type getResource()
	{
		return _resource;
	}

//	old version: protected Permissions getPermissions() throws Throwable
//	{
//		long start = System.currentTimeMillis();
//		IResource resource = getResource();
//		System.out.println("Time to get resource = " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		IAuthZProvider authZHandler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());
//		System.out.println("Time to get getprovider = " + (System.currentTimeMillis() - start));
//		AuthZConfig config = null;
//		start = System.currentTimeMillis();
//		if (authZHandler != null)
//			config = authZHandler.getAuthZConfig(resource);
//		System.out.println("Time to get getauthzconfig = " + (System.currentTimeMillis() - start));
//		start = System.currentTimeMillis();
//		Acl acl = AxisAcl.decodeAcl(config);
//		System.out.println("Time to decode = " + (System.currentTimeMillis() - start));
//		return GenesisIIACLManager.getPermissions(acl, QueueSecurity.getCallerIdentities(false));
//	}

	protected Permissions getPermissions() throws Throwable
	{
		// long start = System.currentTimeMillis();
		IResource resource = getResource();
		// System.out.println("Time to get resource = " + (System.currentTimeMillis()-start));
		// start = System.currentTimeMillis();
		String ACLString = resource.getACLString(true);
		// System.out.println("\tTime to getACLSTring = " + (System.currentTimeMillis()-start));
		if (ACLString == null) {
			// start = System.currentTimeMillis();
			BasicDBResource DBresource = (BasicDBResource) resource;
			DBresource.translateOldAcl();
			ACLString = resource.getACLString(true);
			// System.out.println("\t\tTime to translate ACL from old to new = " + (System.currentTimeMillis()-start));
		}
		// start = System.currentTimeMillis();
		Permissions p = GenesisIIACLManager.getPermissions(ACLString, QueueSecurity.getCallerIdentities(false));
		// System.out.println("\tTime to getPermissions = " + (System.currentTimeMillis()-start) + "|| " + p.toString());
		return p;

		/*
		 * AuthZConfig config = null; start = System.currentTimeMillis(); IAuthZProvider authZHandler =
		 * AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName()); System.out.println(
		 * "Time to get getprovider = " + (System.currentTimeMillis()-start)); start = System.currentTimeMillis(); if (authZHandler != null)
		 * config = authZHandler.getAuthZConfig(resource); System.out.println("Time to get getauthzconfig = " +
		 * (System.currentTimeMillis()-start)); start = System.currentTimeMillis(); Acl acl = AxisAcl.decodeAcl(config); System.out.println(
		 * "Time to decode = " + (System.currentTimeMillis()-start)); return GenesisIIACLManager.getPermissions(acl,
		 * QueueSecurity.getCallerIdentities(false));
		 */
	}

	protected AuthZConfig getAuthZConfig() throws Throwable
	{
		IResource resource = getResource();
		IAuthZProvider authZHandler = AuthZProviders.getProvider(((ResourceKey) resource.getParentResourceKey()).getServiceName());
		AuthZConfig config = null;
		if (authZHandler != null)
			config = authZHandler.getAuthZConfig(resource);
		return config;
	}

	protected void fillInAttributes(Collection<MessageElement> attributes)
	{
		try {
			// long start = System.currentTimeMillis();
			Permissions permissions = getPermissions();

			if (permissions != null) {
				attributes.add(new MessageElement(GenesisIIBaseRP.PERMISSIONS_STRING_QNAME, permissions.toString()));
			}
			String brokerFactoryUrl = Container.getServiceURL(EnhancedNotificationBrokerFactoryServiceImpl.SERVICE_URL);
			MessageElement notificationBrokerFactoryElement =
				new MessageElement(GenesisIIConstants.NOTIFICATION_BROKER_FACTORY_ADDRESS, brokerFactoryUrl);
			attributes.add(notificationBrokerFactoryElement);
			// System.out.println("Time to fill in attributes of default genesis = " + (System.currentTimeMillis()-start));

		} catch (Throwable cause) {
			_logger.warn("Unable to fill in permissions attribute.", cause);
		}
	}
}