package edu.virginia.vcgr.genii.container.common;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Duration;
import org.apache.axis.types.Token;
import org.apache.axis.types.UnsignedLong;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rl_2.DestroyResponse;
import org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType;
import org.oasis_open.docs.wsrf.rl_2.SetTerminationTime;
import org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse;
import org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType;
import org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.notification.InvalidTopicException;
import edu.virginia.vcgr.genii.client.notification.UnknownTopicException;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.x509.CertCreationSpec;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.GeniiSubscriptionPortType;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.SubscribeResponse;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.common.rattrs.AttributeNotSettableFaultType;
import edu.virginia.vcgr.genii.common.rattrs.AttributeUnknownFaultType;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesDocumentResponse;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;
import edu.virginia.vcgr.genii.common.rattrs.IncorrectAttributeCardinalityFaultType;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributes;
import edu.virginia.vcgr.genii.common.rattrs.SetAttributesResponse;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.IContainerManaged;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.attrs.IAttributeManipulator;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionConstructionParameters;
import edu.virginia.vcgr.genii.container.common.notification.TopicSpace;
import edu.virginia.vcgr.genii.container.configuration.ServiceDescription;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.invoker.DatabaseHandler;
import edu.virginia.vcgr.genii.container.invoker.DebugInvoker;
import edu.virginia.vcgr.genii.container.invoker.GAroundInvoke;
import edu.virginia.vcgr.genii.container.invoker.ScheduledTerminationInvoker;
import edu.virginia.vcgr.genii.container.invoker.WSAddressingHandler;
import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resolver.Resolution;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;

@GAroundInvoke({WSAddressingHandler.class, DatabaseHandler.class, 
	DebugInvoker.class, ScheduledTerminationInvoker.class})
public abstract class GenesisIIBase implements GeniiCommon, IContainerManaged
{
	static private Log _logger = LogFactory.getLog(GenesisIIBase.class);

	static private QName _SERVICES_QNAME = new QName(
			GenesisIIConstants.GENESISII_NS, "services");
	
	static private HashMap<Class<? extends GenesisIIBase>, TopicSpace> _topicSpaces =
		new HashMap<Class<? extends GenesisIIBase>, TopicSpace>();
	
	protected String _serviceName;
	public ArrayList<QName> _implementedPortTypes =
		new ArrayList<QName>();
	private AttributePackage _attributePackage = new AttributePackage();
	
	private Properties _defaultResolverFactoryProperties = null;
	private Class<? extends IResolverFactoryProxy> _defaultResolverFactoryProxyClass = null;
	
	public abstract QName getFinalWSResourceInterface();
	
	protected AttributePackage getAttributePackage()
	{
		return _attributePackage;
	}
	
	protected void addImplementedPortType(QName portType)
	{
		synchronized(_implementedPortTypes)
		{
			_implementedPortTypes.add(portType);
		}
	}
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		new GenesisIIBaseAttributesHandler(this, getAttributePackage());
	}
	
	protected GenesisIIBase(String serviceName)
		throws RemoteException
	{
		_serviceName = serviceName;
		
		addImplementedPortType(WellKnownPortTypes.GENII_RESOURCE_ATTRS_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_RESOURCE_FACTORY_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_PRODUCER_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.VCGR_COMMON_PORT_TYPE);
		addImplementedPortType(WSRFConstants.WSRF_RLW_IMMEDIATE_TERMINATE_PORT_QNAME);
		addImplementedPortType(WSRFConstants.WSRF_RLW_SCHEDULED_TERMINATE_PORT_QNAME);
		addImplementedPortType(WSRFConstants.WSRF_RPW_GET_RP_PORT_QNAME);
		addImplementedPortType(WSRFConstants.WSRF_RPW_GET_MULTIPLE_RP_PORT_QNAME);
		
		try
		{
			setAttributeHandlers();
		}
		catch (NoSuchMethodException nsme)
		{
			_logger.error("Couldn't set attribute handlers.", nsme);
			throw new RemoteException(nsme.getLocalizedMessage(), nsme);
		}
	}
	
	protected ResourceKey createResource(HashMap<QName, Object> creationParameters)
		throws ResourceException, BaseFaultType
	{
		if (!creationParameters.containsKey(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM))
			creationParameters.put(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM,
					WSName.generateNewEPI());
		
		try
		{
			CertCreationSpec spec = getChildCertSpec();
			if (spec != null) {
				creationParameters.put(
					IResource.CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM,
					spec);
			}
			
			// set the identity of the service into the creation params
			X509Certificate[] serviceChain = (X509Certificate[]) 
				ResourceManager.getCurrentResource().dereference().
				getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			creationParameters.put(IResource.SERVICE_CERTIFICATE_CHAIN_CONSTRUCTION_PARAM,
					serviceChain);
			
			// set the default authz for the resource 
			XMLConfiguration conf = 
				ConfigurationManager.getCurrentConfiguration().getContainerConfiguration();
			Properties resourceIdSecProps = (Properties) conf.retrieveSection(
					GenesisIIConstants.AUTHZ_PROPERTIES_SECTION_NAME);
			creationParameters.put(IResource.AUTHZ_ENABLED_CONSTRUCTION_PARAM,
					Boolean.valueOf(resourceIdSecProps.getProperty(GenesisIIConstants.AUTHZ_ENABLED_CONFIG_PROPERTY)));
		}
		catch (ConfigurationException ce)
		{
			throw new ResourceException(ce.getLocalizedMessage(), ce);
		}
		
		return ResourceManager.createNewResource(_serviceName, creationParameters);
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> constructionParameters) 
		throws ResourceException, BaseFaultType, RemoteException
	{
		IResource resource = rKey.dereference();
		
		Long timeToLive = (Long)constructionParameters.get(
			ClientConstructionParameters.TIME_TO_LIVE_PROPERTY_ELEMENT);
		if (timeToLive != null)
		{
			Calendar termTime = Calendar.getInstance();
			termTime.setTime(
				new Date(new Date().getTime() + timeToLive.longValue()));
			resource.setProperty(
					IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME, termTime);
		}
	}
	
	static protected Calendar getScheduledTerminationTime()
		throws ResourceException, ResourceUnknownFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		return (Calendar)rKey.dereference().getProperty(
			IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME);
	}
	
	static protected void setScheduledTerminationTime(Calendar termTime)
		throws ResourceUnknownFaultType, ResourceException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		rKey.dereference().setProperty(
			IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME, termTime);
		
/* MOOCH
		Container.getLifetimeVulture().setLifetimeWatch(
			(URI)(rKey.dereference().getProperty(
				IResource.ENDPOINT_IDENTIFIER_PROPERTY_NAME)),
			(EndpointReferenceType)(WorkingContext.getCurrentWorkingContext().getProperty(
				WorkingContext.EPR_PROPERTY_NAME)),
			termTime.getTime());
*/
		
		rKey.dereference().commit();
	}
	
	protected TopicSpace getTopicSpace() throws InvalidTopicException
	{
		TopicSpace ret;
		Class<? extends GenesisIIBase> myClass = getClass();
		
		synchronized(_topicSpaces)
		{
			ret = _topicSpaces.get(myClass);
			if (ret == null)
			{
				ret = new TopicSpace();
				registerTopics(ret);
				_topicSpaces.put(myClass, ret);
			}
		}
		
		return ret;
	}
	
	protected void registerTopics(TopicSpace topicSpace) throws InvalidTopicException
	{
		topicSpace.registerTopic(WellknownTopics.TERMINATED);
	}
	
	protected Object translateConstructionParameter(MessageElement property)
		throws Exception
	{
		QName name = property.getQName();
		
		if (name.equals(ClientConstructionParameters.TIME_TO_LIVE_PROPERTY_ELEMENT))
		{
			return new Long(ClientConstructionParameters.getTimeToLiveProperty(
				property));
		} else if (name.equals(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM))
		{
			return ClientConstructionParameters.getEndpointIdentifierProperty(property);
		} else
			return property;
	}
	
	public final QName[] getImplementedPortTypes()
	{
		QName []ret;
		
		synchronized(_implementedPortTypes)
		{
			ret = new QName[_implementedPortTypes.size()];
			_implementedPortTypes.toArray(ret);
		}
		
		return ret;
	}
	
	@RWXMapping(RWXCategory.READ)
	public final String ping(String request) {
		return request;
	}

	@RWXMapping(RWXCategory.READ)
	public final GetAttributesResponse getAttributes(QName[] attributeQname)
			throws RemoteException, ResourceUnknownFaultType,
			AttributeUnknownFaultType
	{
		ArrayList<MessageElement> document = new ArrayList<MessageElement>();
		for (QName name : attributeQname)
		{
			IAttributeManipulator manipulator =
				_attributePackage.getManipulator(name);
			
			if (manipulator == null)
				throw FaultManipulator.fillInFault(
					new AttributeUnknownFaultType());
			
			document.addAll(manipulator.getAttributeValues());
		}
		
		MessageElement []ret = new MessageElement[document.size()];
		document.toArray(ret);
		return new GetAttributesResponse(ret);
	}
	
	@RWXMapping(RWXCategory.READ)
	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(
			QName[] getMultipleResourcePropertiesRequest)
			throws RemoteException, InvalidResourcePropertyQNameFaultType,
			ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> document = new ArrayList<MessageElement>();
		
		for (QName name : getMultipleResourcePropertiesRequest)
		{
			IAttributeManipulator manipulator =
				_attributePackage.getManipulator(name);
				
			if (manipulator == null)
				throw FaultManipulator.fillInFault(
					new InvalidResourcePropertyQNameFaultType(
						null, null, null, null, new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription("The resource property " +
								name + " is unknown.") },
						null));
				
			document.addAll(manipulator.getAttributeValues());
		}
		
		MessageElement []ret = new MessageElement[document.size()];
		document.toArray(ret);
		return new GetMultipleResourcePropertiesResponse(ret);
	}

	@RWXMapping(RWXCategory.READ)
	public GetResourcePropertyResponse getResourceProperty(
			QName getResourcePropertyRequest) throws RemoteException,
			InvalidResourcePropertyQNameFaultType,
			ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> document = new ArrayList<MessageElement>();
		
		IAttributeManipulator manipulator =
			_attributePackage.getManipulator(getResourcePropertyRequest);
			
		if (manipulator == null)
			throw FaultManipulator.fillInFault(
				new InvalidResourcePropertyQNameFaultType(
					null, null, null, null, new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription("The resource property " +
							getResourcePropertyRequest + " is unknown.") },
					null));
			
		document.addAll(manipulator.getAttributeValues());
		
		MessageElement []ret = new MessageElement[document.size()];
		document.toArray(ret);
		return new GetResourcePropertyResponse(ret);	
	}
	
	@RWXMapping(RWXCategory.READ)
	public final GetAttributesDocumentResponse getAttributesDocument(
			Object getAttributesDocumentRequest) throws RemoteException,
			ResourceUnknownFaultType
	{
		ArrayList<IAttributeManipulator> manipulators =
			_attributePackage.getManipulators();
		ArrayList<MessageElement> elements = new ArrayList<MessageElement>();
		for (IAttributeManipulator manipulator : manipulators)
		{
			elements.addAll(manipulator.getAttributeValues());
		}
		
		MessageElement []elementsArray = new MessageElement[elements.size()];
		elements.toArray(elementsArray);
		return new GetAttributesDocumentResponse(elementsArray);
	}

	protected void preDestroy() throws RemoteException, ResourceException
	{
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public final SetAttributesResponse setAttributes(
			SetAttributes setAttributesRequest) throws RemoteException,
			AttributeNotSettableFaultType,
			IncorrectAttributeCardinalityFaultType, ResourceUnknownFaultType,
			AttributeUnknownFaultType
	{
		HashMap<QName, ArrayList<MessageElement> > map =
			new HashMap<QName, ArrayList<MessageElement> >();
		
		MessageElement []elements = setAttributesRequest.get_any();
		for (MessageElement element : elements)
		{
			QName name = element.getQName();
			ArrayList<MessageElement> list = map.get(name);
			if (list == null)
				map.put(name, (list = new ArrayList<MessageElement>()));
			list.add(element);
		}
		
		for (QName name : map.keySet())
		{
			IAttributeManipulator manipulator =
				_attributePackage.getManipulator(name);
			
			if (manipulator == null)
				FaultManipulator.fillInFault(
					new AttributeUnknownFaultType());
			
			manipulator.setAttributeValues(map.get(name));
		}
		
		return new SetAttributesResponse(true);
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public final VcgrCreateResponse vcgrCreate(VcgrCreate createRequest)
		throws RemoteException, ResourceCreationFaultType
	{
		EndpointReferenceType myEPR = 
			(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
					WorkingContext.EPR_PROPERTY_NAME);
		
		if (myEPR == null)
		{
			myEPR = new EndpointReferenceType(
				new AttributedURITypeSmart(Container.getServiceURL(_serviceName)),
				null, null, null);
			WorkingContext.getCurrentWorkingContext().setProperty(
				WorkingContext.EPR_PROPERTY_NAME, myEPR);
		}
		
		HashMap<QName, Object> constructionParameters 
			= new HashMap<QName, Object>();
		
		MessageElement []creationParameters = createRequest.get_any();
		if (creationParameters != null)
		{
			for (MessageElement property : creationParameters)
			{
				try
				{
					constructionParameters.put(property.getQName(),
						translateConstructionParameter(property));
				}
				catch (Exception e)
				{
					if (e instanceof RemoteException)
						throw (RemoteException)e;
					
					throw FaultManipulator.fillInFault(
						new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(e.getLocalizedMessage()) },
						null));
				}
			}
		}
		
		ResourceKey rKey = createResource(constructionParameters);
		EndpointReferenceType epr = ResourceManager.createEPR(rKey, 
			myEPR.getAddress().get_value().toString(), getImplementedPortTypes());

		try
		{
			CallingContextImpl context = new CallingContextImpl((CallingContextImpl)null);
			context.setActiveKeyAndCertMaterial(new KeyAndCertMaterial(
				(X509Certificate[])constructionParameters.get(IResource.CERTIFICATE_CHAIN_CONSTRUCTION_PARAM),
				Container.getContainerPrivateKey()));
			rKey.dereference().setProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME, context);
		}
		catch (GeneralSecurityException gse)
		{
			throw FaultManipulator.fillInFault(
				new ResourceCreationFaultType(null, null, null, null, new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription("Security error while initializing new resource's calling context."),
					new BaseFaultTypeDescription(gse.getLocalizedMessage()) }, null));
		}
		
		// allow subclasses to do creation work
		postCreate(rKey, epr, constructionParameters);
		
		rKey.dereference().commit();
		_logger.debug("Created resource \"" + rKey.getKey() + "\" for service \"" +
			rKey.getServiceName() + "\".");
		EndpointReferenceType resolveEPR = addResolvers(rKey, epr, constructionParameters);
		
		return new VcgrCreateResponse(resolveEPR);
	}
	
	protected EndpointReferenceType addResolvers(ResourceKey rKey, EndpointReferenceType newEPR,
			HashMap<QName, Object> constructionParameters) 
			throws ResourceException, BaseFaultType
	{
		EndpointReferenceType resolveEPR = newEPR;
		
		/* parse config info for service to see if there is a default resolver service */
		try
		{
			Class<? extends IResolverFactoryProxy> resolverFactoryProxyClass = getDefaultResolverFactoryProxyClass();
		
			if (resolverFactoryProxyClass != null)
			{
				Properties resolverFactoryProps = getDefaultResolverFactoryProperties();
				IResolverFactoryProxy resolverFactoryProxy = (IResolverFactoryProxy) resolverFactoryProxyClass.newInstance();
				if (resolverFactoryProxy != null)
				{
					Resolution newResolution = resolverFactoryProxy.createResolver(newEPR, resolverFactoryProps);
					if (newResolution != null)
					{
						_logger.debug("Setup new resolver for instance of service \"" + _serviceName);
						resolveEPR = newResolution.getResolvedTargetEPR();
					}
				}
			}
		}
		catch(Throwable t)
		{
			_logger.warn("Could not create resolver for new instance of service \"" + _serviceName, t);
//			throw new ResourceException("Could not create resolver for new instance of service \"" + _serviceName, t);
		}
		
		return resolveEPR;
	}
	
	/**
	 * Called by the container to initialize the service.  Returns true if the 
	 * service is being *created* for the first time.
	 */
	public boolean startup()
	{
		boolean serviceCreated = false;
		
		_logger.info("Initializing \"" + getClass().getName() 
			+ "\" service implementation.");
		
		ResourceKey rKey = null;
		
		try
		{
			WorkingContext.setCurrentWorkingContext(new WorkingContext());
			
			try
			{
				rKey = ResourceManager.getServiceResource(_serviceName);
			}
			catch (ResourceUnknownFaultType ruft)
			{
				// need to create the service resource.
				serviceCreated = true;
				
				HashMap<QName, Object> constructionParameters = 
					new HashMap<QName, Object>();
				
				constructionParameters.put(
					IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM,
					WSName.generateNewEPI());
				
				// need to create the service resource
				CertCreationSpec serviceCertSpec = null;
				X509Certificate[] containerChain = Container.getContainerCertChain();
					// If this is null, then security isn't turned on.
				if (containerChain != null)
				{
					serviceCertSpec = new CertCreationSpec(
						containerChain[0].getPublicKey(),
						containerChain,
						Container.getContainerPrivateKey(),
						getServiceCertificateLifetime());
					
					constructionParameters.put(
						IResource.CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM,
						serviceCertSpec);
				}
				
				// set the default authz classname the resource 
				XMLConfiguration conf = 
					ConfigurationManager.getCurrentConfiguration().getContainerConfiguration();
				Properties resourceIdSecProps = (Properties) conf.retrieveSection(
						GenesisIIConstants.AUTHZ_PROPERTIES_SECTION_NAME);
				constructionParameters.put(IResource.AUTHZ_ENABLED_CONSTRUCTION_PARAM,
						Boolean.valueOf(resourceIdSecProps.getProperty(GenesisIIConstants.AUTHZ_ENABLED_CONFIG_PROPERTY)));
				
				rKey = ResourceManager.createServiceResource(_serviceName, 
					constructionParameters);
				rKey.dereference().commit();
			}
		}
		catch (ConfigurationException ce)
		{
			_logger.error(ce);
		}
		catch (ResourceException re)
		{
			_logger.error(re);
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}

		// TODO have to register the scheduled terminations with the vulture.

		return serviceCreated;
	}
	
	static private Long _serviceCertificateLifetime = null;
	static private Long _resourceCertificateLifetime = null;
	
	@SuppressWarnings("unchecked")
	private void setLifetimes() throws ConfigurationException
	{
		synchronized(GenesisIIBase.class)
		{
			if (_serviceCertificateLifetime == null)
			{
				XMLConfiguration conf = 
					ConfigurationManager.getCurrentConfiguration().getContainerConfiguration();
				ArrayList<Object> sections;
				sections = conf.retrieveSections(_SERVICES_QNAME);
				for (Object obj : sections)
				{
					HashMap<String, ServiceDescription> services =
						(HashMap<String, ServiceDescription>)obj;
					ServiceDescription desc = services.get(_serviceName);
					if (desc != null)
					{
						_serviceCertificateLifetime = 
							new Long(desc.getServiceCertificateLifetime());
						_resourceCertificateLifetime =
							new Long(desc.getResourceCertificateLifetime());
					}
				}
			}
		}
	}
	
	protected long getServiceCertificateLifetime()
		throws ConfigurationException
	{
		setLifetimes();
		
		return _serviceCertificateLifetime.longValue();
	}
	
	protected long getResourceCertificateLifetime()
		throws ConfigurationException
	{
		setLifetimes();
		
		return _resourceCertificateLifetime.longValue();
	}
	
	// Returns null if security is turned off.
	// If we decided to make the resource certificates children of the service certificate
	// we would revisit this spot.
	protected CertCreationSpec getChildCertSpec() 
		throws ResourceException, ConfigurationException
	{
		X509Certificate[] containerChain = Container.getContainerCertChain();
			// If this is null, then security isn't turned on.
		if (containerChain != null)
		{
			return new CertCreationSpec(
				containerChain[0].getPublicKey(),
				containerChain,
				Container.getContainerPrivateKey(),
				getServiceCertificateLifetime());
		}
		
		return null;
	}
	
	protected SByteIOFactory createStreamableByteIOResource()
		throws IOException
	{
		return new SByteIOFactory(Container.getServiceURL("StreamableByteIOPortType"));
	}	
	
	@RWXMapping(RWXCategory.WRITE)
	public SubscribeResponse subscribe(Subscribe subscribeRequest) 
		throws RemoteException, ResourceUnknownFaultType
	{
		EndpointReferenceType target = subscribeRequest.getTarget();
		UnsignedLong ttl = subscribeRequest.getTimeToLive();
		Token topic = subscribeRequest.getTopic();
		UserDataType userData = subscribeRequest.getUserData();
		
		try
		{
			GeniiSubscriptionPortType subscription =
				ClientUtils.createProxy(GeniiSubscriptionPortType.class,
				EPRUtils.makeEPR(Container.getServiceURL("GeniiSubscriptionPortType")));

			HashMap<QName, MessageElement> constructionParameters =
				new HashMap<QName, MessageElement>();
			SubscriptionConstructionParameters.insertSubscriptionParameters(
				constructionParameters, 
				(String)ResourceManager.getCurrentResource().dereference().getKey(),
				target, topic.toString(), 
				(ttl == null) ? null : new Long(ttl.longValue()),
				userData);
			MessageElement []params = new MessageElement[constructionParameters.size()];
			constructionParameters.values().toArray(params);
			return new SubscribeResponse(
				subscription.vcgrCreate(
					new VcgrCreate(params)).getEndpoint());
		}
		catch (ConfigurationException ce)
		{
			throw new RemoteException(ce.getLocalizedMessage(), ce);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setDefaultResolverFactoryDescription() throws ConfigurationException
	{
		synchronized(GenesisIIBase.class)
		{
			if (_defaultResolverFactoryProperties == null || _defaultResolverFactoryProxyClass == null)
			{
				XMLConfiguration conf = 
					ConfigurationManager.getCurrentConfiguration().getContainerConfiguration();
				ArrayList<Object> sections;
				sections = conf.retrieveSections(_SERVICES_QNAME);
				for (Object obj : sections)
				{
					HashMap<String, ServiceDescription> services =
						(HashMap<String, ServiceDescription>)obj;
					ServiceDescription desc = services.get(_serviceName);
					if (desc != null)
					{
						_defaultResolverFactoryProperties = 
							new Properties(desc.getDefaultResolverFactoryProperties());
						_defaultResolverFactoryProxyClass =
							desc.getDefaultResolverFactoryProxyClass();
					}
				}
			}
		}
	}

	protected Properties getDefaultResolverFactoryProperties() throws ConfigurationException
	{
		setDefaultResolverFactoryDescription();
		return _defaultResolverFactoryProperties;
	}

	
	protected Class<? extends IResolverFactoryProxy> getDefaultResolverFactoryProxyClass()
		throws ConfigurationException
	{
		setDefaultResolverFactoryDescription();
		return _defaultResolverFactoryProxyClass;
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public DestroyResponse destroy(Destroy destroyRequest)
			throws RemoteException, ResourceUnknownFaultType,
			ResourceNotDestroyedFaultType, ResourceUnavailableFaultType
	{
		preDestroy();
		
		try
    	{
	    	MessageElement []payload = new MessageElement[1];
	    	
	    	payload[0] = new MessageElement(
	    		new QName(GenesisIIConstants.GENESISII_NS, "entry-reference"),
	    		ResourceManager.createEPR(
	    			ResourceManager.getCurrentResource(), 
	    			Container.getServiceURL(_serviceName),
	    			getImplementedPortTypes()));
	    	
	    	getTopicSpace().getTopic(WellknownTopics.TERMINATED).notifyAll(
	    		payload);
    	}
    	catch (InvalidTopicException ite)
    	{
    		_logger.warn(ite.getLocalizedMessage(), ite);
    	}
    	catch (UnknownTopicException ute)
    	{
    		_logger.warn(ute.getLocalizedMessage(), ute);
    	}
		
    	ResourceKey resource = ResourceManager.getCurrentResource();
    	try
    	{
    		resource.destroy();
        	resource.dereference().commit();
    	}
    	catch(ResourceException re)
    	{
    		_logger.error(re);
    		throw re;
    	}
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public SetTerminationTimeResponse setTerminationTime(
			SetTerminationTime setTerminationTimeRequest)
			throws RemoteException, ResourceUnknownFaultType,
			UnableToSetTerminationTimeFaultType, ResourceUnavailableFaultType,
			TerminationTimeChangeRejectedFaultType
	{
		Duration duration = setTerminationTimeRequest.getRequestedLifetimeDuration();
		Calendar c = setTerminationTimeRequest.getRequestedTerminationTime();
		
		if (c == null)
			c = duration.getAsCalendar();
		
		setScheduledTerminationTime(c);
		return new SetTerminationTimeResponse(c, Calendar.getInstance());
	}
}