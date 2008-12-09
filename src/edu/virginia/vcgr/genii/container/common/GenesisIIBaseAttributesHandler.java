package edu.virginia.vcgr.genii.container.common;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIACLManager;
import edu.virginia.vcgr.genii.client.notification.InvalidTopicException;
import edu.virginia.vcgr.genii.client.ogsa.OGSAQNameList;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlAcl;
import edu.virginia.vcgr.genii.client.utils.units.Duration;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.attrs.IAttributeManipulator;
import edu.virginia.vcgr.genii.container.common.notification.Topic;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.q2.QueueSecurity;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.security.authz.providers.*;

public class GenesisIIBaseAttributesHandler 
	extends AbstractAttributeHandler
{
	private GenesisIIBase _baseService;
	
	public GenesisIIBaseAttributesHandler(GenesisIIBase baseService,
		AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
		
		_baseService = baseService;
	}
	
	public Collection<MessageElement> getRegisteredTopics() 
		throws InvalidTopicException
	{
		Collection<Topic> topics = _baseService.getTopicSpace().getRegisteredTopics();
		ArrayList<MessageElement> document = new ArrayList<MessageElement>(
			topics.size());
		
		for (Topic topic : topics)
		{
			document.add(new MessageElement(
				GenesisIIConstants.REGISTERED_TOPICS_ATTR_QNAME,
				topic.getTopicName()));
		}
		
		return document;
	}
	
	public Collection<MessageElement> getMatchingParameters()
		throws ResourceException, ResourceUnknownFaultType
	{
		Collection<MessageElement> ret = new ArrayList<MessageElement>();
		IResource resource = ResourceManager.getCurrentResource().dereference();
		Collection<MatchingParameter> matchingParams = 
			resource.getMatchingParameters();
		for (MatchingParameter param : matchingParams)
		{
			MessageElement me = new MessageElement(
				GenesisIIBaseRP.MATCHING_PARAMTER_ATTR_QNAME,
				param);
			ret.add(me);
		}
		
		return ret;
	}
	
	public MessageElement getImplementedPortTypes() throws SOAPException
	{
		synchronized(_baseService._implementedPortTypes)
		{
			Collection<QName> names = new ArrayList<QName>(
				_baseService._implementedPortTypes.size());
			for (PortType pt : _baseService._implementedPortTypes)
				names.add(pt.getQName());
			
			OGSAQNameList list = new OGSAQNameList(names);
			return list.toMessageElement(
				OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME);
		}
	}
	
	public MessageElement getFinalResourceInterface() throws SOAPException
	{
		return new MessageElement(OGSAWSRFBPConstants.WS_FINAL_RESOURCE_INTERFACE_ATTR_QNAME,
			_baseService.getFinalWSResourceInterface().getQName());
	}
	
	public MessageElement getScheduledTerminationTimeAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		Calendar termTime = GenesisIIBase.getScheduledTerminationTime();
		
		if (termTime != null)
			return new MessageElement(
				OGSAWSRFBPConstants.TERMINATION_TIME_ATTR_QNAME,
				termTime);
		else
			return new MessageElement(
				OGSAWSRFBPConstants.TERMINATION_TIME_ATTR_QNAME);
	}
	
	public void setScheduledTerminationTimeAttr(MessageElement newTermTime)
		throws ResourceException, ResourceUnknownFaultType
	{			
		if (newTermTime == null)
			GenesisIIBase.setScheduledTerminationTime(null);
		
		try
		{
			GenesisIIBase.setScheduledTerminationTime(
				(Calendar)(newTermTime.getObjectValue(
					Calendar.class)));
		}
		catch (Exception e)
		{
			throw new ResourceException(e.getMessage(), e);
		}
	}
	
	public MessageElement getResourceEndpoint()
		throws ResourceUnknownFaultType, ResourceException
	{
		EndpointReferenceType myEPR = (EndpointReferenceType)WorkingContext.getCurrentWorkingContext(
			).getProperty(WorkingContext.EPR_PROPERTY_NAME);
		ResourceKey rKey = ResourceManager.getCurrentResource();
		PortType []implementedPortTypes = new PortType[_baseService._implementedPortTypes.size()];
		_baseService._implementedPortTypes.toArray(implementedPortTypes);
		EndpointReferenceType epr = ResourceManager.createEPR(
			rKey, myEPR.getAddress().get_value().toString(), implementedPortTypes);
		
		return new MessageElement(
			OGSAWSRFBPConstants.RESOURCE_ENDPOINT_REFERENCE_ATTR_QNAME, epr);
	}
	
	public MessageElement getCacheCoherenceWindow()
		throws RemoteException
	{
		Duration gDur = _baseService.getCacheCoherenceWindow();
		if (gDur != null)
		{
			return new MessageElement(
				GenesisIIConstants.CACHE_COHERENCE_WINDOW_ATTR_QNAME,
				gDur.toApacheDuration());
		} else
			return null;
	}
	
	public void setCacheCoherenceWindow(MessageElement mel)
		throws RemoteException
	{
		Duration gDur = null;
		if (mel != null)
		{
			try
			{
				org.apache.axis.types.Duration aDur =
					(org.apache.axis.types.Duration)mel.getObjectValue(
						org.apache.axis.types.Duration.class);
				gDur = Duration.fromApacheDuration(aDur);
			}
			catch (Exception e)
			{
				throw new ResourceException(
					"Unable to set cache coherence window.", e);
			}
		}
		
		_baseService.setCacheCoherenceWindow(gDur);
	}
	
	public MessageElement getPermissionsString() 
		throws ResourceUnknownFaultType, ResourceException, AuthZSecurityException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		IAuthZProvider authZHandler = AuthZProviders.getProvider(
				resource.getParentResourceKey().getServiceName());
		AuthZConfig config = null;
		if (authZHandler != null)
			config = authZHandler.getAuthZConfig(resource);
		GamlAcl acl = GamlAcl.decodeAcl(config);
		Permissions perms = GenesisIIACLManager.getPermissions(acl, 
			QueueSecurity.getCallerIdentities());
		return new MessageElement(
			GenesisIIBaseRP.PERMISSIONS_STRING_QNAME,
			perms.toString());
	}
	
	public MessageElement getAuthZConfig()
			throws ResourceUnknownFaultType, ResourceException, AuthZSecurityException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		IAuthZProvider authZHandler = AuthZProviders.getProvider(
				resource.getParentResourceKey().getServiceName());
		AuthZConfig config = null;
		if (authZHandler != null) {
			config = authZHandler.getAuthZConfig(resource);
		}
		
		return new MessageElement(
			AuthZConfig.getTypeDesc().getXmlType(), 
			config);
	}
	
	public void setAuthZConfig(MessageElement mel)
			throws ResourceException, ResourceUnknownFaultType, AuthZSecurityException {
	
		IResource resource = ResourceManager.getCurrentResource().dereference();
	
		// get the authZ configuration
		if (!mel.getQName().equals(AuthZConfig.getTypeDesc().getXmlType())) {
			throw new AuthZSecurityException("Invalid AuthZ config");
		}
		AuthZConfig config = null;
		try {
			config = (AuthZConfig) 
				mel.getObjectValue(AuthZConfig.class);
		} catch (Exception e) { 
			throw new AuthZSecurityException("Invalid AuthZ config.", e);
		}
		if (config == null) {
			throw new AuthZSecurityException("Invalid AuthZ config");
		}
		
		// get the authZ handler 			
		IAuthZProvider authZHandler = AuthZProviders.getProvider(
				resource.getParentResourceKey().getServiceName());
		if (authZHandler == null) {
			throw new ResourceException("Resource does not have an AuthZ module");
		}
		
		// config the authZ handler
		authZHandler.setAuthZConfig(config, resource);
	}

	public MessageElement getCurrentTimeAttr()
	{
		return new MessageElement(
			OGSAWSRFBPConstants.CURRENT_TIME_ATTR_QNAME,
			new Date());
	}
	
	public MessageElement getResourcePropertyNames()
		throws SOAPException
	{
		Collection<QName> propertyNames = new ArrayList<QName>();
		for (IAttributeManipulator manipulator : 
			_baseService.getAttributePackage().getManipulators())
		{
			propertyNames.add(manipulator.getAttributeQName());
		}
		
		OGSAQNameList list = new OGSAQNameList(propertyNames);
		return list.toMessageElement(
			OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME);
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(
			GenesisIIBaseRP.MATCHING_PARAMTER_ATTR_QNAME,
			"getMatchingParameters");
		
		addHandler(
			OGSAWSRFBPConstants.CURRENT_TIME_ATTR_QNAME,
			"getCurrentTimeAttr");
	
		addHandler(
			OGSAWSRFBPConstants.RESOURCE_ENDPOINT_REFERENCE_ATTR_QNAME,
			"getResourceEndpoint");	
		
		addHandler(
			OGSAWSRFBPConstants.TERMINATION_TIME_ATTR_QNAME,
			"getScheduledTerminationTimeAttr",
			"setScheduledTerminationTimeAttr");
				
		addHandler(
			OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME,
			"getImplementedPortTypes");
		
		addHandler(
			OGSAWSRFBPConstants.WS_FINAL_RESOURCE_INTERFACE_ATTR_QNAME,
			"getFinalResourceInterface");
		
		addHandler(
			GenesisIIConstants.REGISTERED_TOPICS_ATTR_QNAME,
			"getRegisteredTopics");
		
		addHandler(
			GenesisIIConstants.AUTHZ_CONFIG_ATTR_QNAME,
			"getAuthZConfig",
			"setAuthZConfig");
		
		addHandler(
			GenesisIIBaseRP.PERMISSIONS_STRING_QNAME,
			"getPermissionsString");
		
		addHandler(
			OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME,
			"getResourcePropertyNames");
		
		addHandler(
			GenesisIIConstants.CACHE_COHERENCE_WINDOW_ATTR_QNAME,
			"getCacheCoherenceWindow", "setCacheCoherenceWindow");
	}
}