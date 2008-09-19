package edu.virginia.vcgr.genii.container.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedInt;
import org.apache.axis.types.UnsignedLong;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.morgan.util.GUID;
import org.morgan.util.configuration.XMLConfiguration;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rl_2.DestroyResponse;
import org.oasis_open.docs.wsrf.rl_2.ResourceNotDestroyedFaultType;
import org.oasis_open.docs.wsrf.rl_2.SetTerminationTime;
import org.oasis_open.docs.wsrf.rl_2.SetTerminationTimeResponse;
import org.oasis_open.docs.wsrf.rl_2.TerminationTimeChangeRejectedFaultType;
import org.oasis_open.docs.wsrf.rl_2.UnableToSetTerminationTimeFaultType;
import org.oasis_open.docs.wsrf.rp_2.DeleteResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesRequestFailedFaultType;
import org.oasis_open.docs.wsrf.rp_2.DeleteResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.DeleteType;
import org.oasis_open.docs.wsrf.rp_2.GetMultipleResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocument;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyDocumentResponse;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;
import org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesRequestFailedFaultType;
import org.oasis_open.docs.wsrf.rp_2.InsertResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.InsertType;
import org.oasis_open.docs.wsrf.rp_2.InvalidModificationFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidQueryExpressionFaultType;
import org.oasis_open.docs.wsrf.rp_2.InvalidResourcePropertyQNameFaultType;
import org.oasis_open.docs.wsrf.rp_2.QueryEvaluationErrorFaultType;
import org.oasis_open.docs.wsrf.rp_2.QueryExpressionType;
import org.oasis_open.docs.wsrf.rp_2.QueryResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.QueryResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.SetResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.SetResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.SetResourcePropertyRequestFailedFaultType;
import org.oasis_open.docs.wsrf.rp_2.UnableToModifyResourcePropertyFaultType;
import org.oasis_open.docs.wsrf.rp_2.UnknownQueryExpressionDialectFaultType;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesRequestFailedFaultType;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourcePropertiesResponse;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

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
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.x509.CertCreationSpec;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType;
import edu.virginia.vcgr.genii.common.notification.GeniiSubscriptionPortType;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.SubscribeResponse;
import edu.virginia.vcgr.genii.common.notification.UserDataType;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.IContainerManaged;
import edu.virginia.vcgr.genii.container.alarms.AlarmIdentifier;
import edu.virginia.vcgr.genii.container.alarms.AlarmManager;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.attrs.IAttributeManipulator;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionConstructionParameters;
import edu.virginia.vcgr.genii.container.common.notification.TopicSpace;
import edu.virginia.vcgr.genii.container.configuration.ServiceDescription;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.invoker.BaseFaultFixer;
import edu.virginia.vcgr.genii.container.invoker.DatabaseHandler;
import edu.virginia.vcgr.genii.container.invoker.DebugInvoker;
import edu.virginia.vcgr.genii.container.invoker.GAroundInvoke;
import edu.virginia.vcgr.genii.container.invoker.ScheduledTerminationInvoker;
import edu.virginia.vcgr.genii.container.invoker.WSAddressingHandler;
import edu.virginia.vcgr.genii.container.iterator.IteratorResource;
import edu.virginia.vcgr.genii.container.iterator.IteratorServiceImpl;
import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resolver.Resolution;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceFactory;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.utils.creation.CreationProperties;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;
import edu.virginia.vcgr.genii.iterator.IteratorMemberType;
import edu.virginia.vcgr.genii.iterator.IteratorPortType;

@GAroundInvoke({BaseFaultFixer.class, WSAddressingHandler.class, DatabaseHandler.class, 
	DebugInvoker.class, ScheduledTerminationInvoker.class})
public abstract class GenesisIIBase implements GeniiCommon, IContainerManaged
{
	static private Log _logger = LogFactory.getLog(GenesisIIBase.class);

	static private QName _SERVICES_QNAME = new QName(
			GenesisIIConstants.GENESISII_NS, "services");
	
	static private HashMap<Class<? extends GenesisIIBase>, TopicSpace> _topicSpaces =
		new HashMap<Class<? extends GenesisIIBase>, TopicSpace>();
	
	static private EndpointReferenceType _iteratorServiceEPR = null;
	
	protected String _serviceName;
	public ArrayList<PortType> _implementedPortTypes =
		new ArrayList<PortType>();
	private AttributePackage _attributePackage = new AttributePackage();
	
	private Properties _defaultResolverFactoryProperties = null;
	private Class<? extends IResolverFactoryProxy> _defaultResolverFactoryProxyClass = null;
	
	public abstract PortType getFinalWSResourceInterface();
	
	protected AttributePackage getAttributePackage()
	{
		return _attributePackage;
	}
	
	protected void addImplementedPortType(PortType portType)
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
		addImplementedPortType(WSRFConstants.WSRF_RLW_IMMEDIATE_TERMINATE_PORT);
		addImplementedPortType(WSRFConstants.WSRF_RLW_SCHEDULED_TERMINATE_PORT);
		addImplementedPortType(WSRFConstants.WSRF_RPW_GET_RP_PORT);
		addImplementedPortType(WSRFConstants.WSRF_RPW_GET_MULTIPLE_RP_PORT);
		
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
		
		return ResourceManager.createNewResource(_serviceName, creationParameters);	
	}
	
		protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> constructionParameters, 
		Collection<MessageElement> resolverCreationParameters) 
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
		
		Duration gDur = getInitialCacheCoherenceWindow();
		if (gDur != null)
			resource.setProperty(
				IResource.CACHE_COHERENCE_WINDOW_PROPERTY, gDur);
	}
	
	static protected Calendar getScheduledTerminationTime()
		throws ResourceException, ResourceUnknownFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		return (Calendar)rKey.dereference().getProperty(
			IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME);
	}
	
	final protected void terminationAlarm(
		AlarmIdentifier alarmID, Object userData)
		throws ResourceNotDestroyedFaultType, RemoteException
	{
		_logger.info(
			"Resource is self-terminating due to scheduled termination time.");
		destroy(null);
	}
	
	static protected void setScheduledTerminationTime(Calendar termTime)
		throws ResourceUnknownFaultType, ResourceException
	{
		AlarmIdentifier alarmID;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = rKey.dereference();
		
		resource.setProperty(
			IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME, termTime);
		
		alarmID = (AlarmIdentifier)resource.getProperty(IResource.TERM_TIME_ALARM);
		if (alarmID != null)
			alarmID.cancel();
		
		if (termTime != null)
			alarmID = AlarmManager.getManager().addAlarm(
				termTime.getTime(), 15 * 1000L, null, null, "terminationAlarm", null);
		
		resource.commit();
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
		topicSpace.registerTopic(WellknownTopics.RANDOM_BYTEIO_OP);
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
		} else if (name.equals(CreationProperties.CREATION_PROPERTIES_QNAME))
		{
			return CreationProperties.translate(property);
		} else
			return property;
	}
	
	static private Method findAlarmMethod(
		Class<?> cl, String methodName, Object userData)
	{
		if (cl == null)
			return null;
		
		if (userData != null)
		{
			try
			{
				return cl.getDeclaredMethod(methodName, AlarmIdentifier.class,
					userData.getClass());
			}
			catch (NoSuchMethodException nsme)
			{
				return findAlarmMethod(cl.getSuperclass(), methodName, userData);
			}
		} else
		{
			try
			{
				return cl.getDeclaredMethod(methodName,
					AlarmIdentifier.class, Object.class);
			}
			catch (NoSuchMethodException nsme)
			{
				try
				{
					return cl.getDeclaredMethod(methodName, 
						AlarmIdentifier.class);
				}
				catch (NoSuchMethodException nsme2)
				{
					return findAlarmMethod(cl.getSuperclass(),
						methodName, userData);
				}
			}
		}
	}
	
	final public void callAlarmMethod(AlarmIdentifier alarm, 
		String methodName, Object userData) throws ResourceUnknownFaultType
	{
		Method m = findAlarmMethod(getClass(), methodName, userData);
		if (m == null)
		{
			_logger.warn("Attempt to call alarm method \"" + 
				methodName + "\" which couldn't be found.");
		} else
		{
			try
			{
				if (m.getParameterTypes().length == 1)
					m.invoke(this, alarm);
				else
					m.invoke(this, alarm, userData);
			}
			catch (InvocationTargetException ite)
			{
				Throwable cause = ite.getCause();
				if (cause instanceof ResourceUnknownFaultType)
					throw (ResourceUnknownFaultType)cause;
				
				_logger.warn("Calling alarm method \"" + methodName +
					"\" resulted in exception.", ite.getCause());
			}
			catch (Throwable cause)
			{
				_logger.warn("Couldn't call alarm method \"" + methodName +
					"\".", cause);
			}
		}
	}
	
	public PortType[] getImplementedPortTypes(ResourceKey rKey) 
		throws ResourceException, ResourceUnknownFaultType
	{
		PortType[]ret;
		
		synchronized(_implementedPortTypes)
		{
			ret = new PortType[_implementedPortTypes.size()];
			_implementedPortTypes.toArray(ret);
		}
		
		return ret;
	}
	
	@RWXMapping(RWXCategory.READ)
	public final String ping(String request) {
		return request;
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
			{
				_logger.error("The resource property \"" + name + "\" is unknown.");
				throw FaultManipulator.fillInFault(
					new InvalidResourcePropertyQNameFaultType(
						null, null, null, null, new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription("The resource property " +
								name + " is unknown.") },
						null));
			}
				
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
	
	protected void preDestroy() throws RemoteException, ResourceException
	{
	}
	
	/**
	 * Quick test for overriding classes to implement should they desire
	 * to disable resource creation on this endpoint
	 * @return true if vcgrCreate is applicable, false otherwise.
	 */
	protected boolean allowVcgrCreate() throws ResourceException, ResourceUnknownFaultType {
		return true;
	}
	
	public EndpointReferenceType CreateEPR(MessageElement []creationParameters, 
			String targetServiceURL )
	throws RemoteException, ResourceCreationFaultType, BaseFaultType
	{
		HashMap<QName, Object> constructionParameters 
		= new HashMap<QName, Object>();


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
				targetServiceURL, getImplementedPortTypes(rKey));
		
		if (!(this instanceof GeniiNoOutCalls)){
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
		
		}	
		Collection<MessageElement> resolverCreationParams = new Vector<MessageElement>();
		
		// allow subclasses to do creation work
		postCreate(rKey, epr, constructionParameters, resolverCreationParams);
		return epr;
	}

	
	
	@RWXMapping(RWXCategory.EXECUTE)
	public final VcgrCreateResponse vcgrCreate(VcgrCreate createRequest)
		throws RemoteException, ResourceCreationFaultType
	{
		if (!allowVcgrCreate()) {
			throw new RemoteException("\"vcgrCreate\" not applicable.");
		}
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
			myEPR.getAddress().get_value().toString(), getImplementedPortTypes(rKey));
		if (!(this instanceof GeniiNoOutCalls)){
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
		}
		//collection to hold creation parameters for default resolvers
		Collection<MessageElement> resolverCreationParams = new Vector<MessageElement>();
		
		// allow subclasses to do creation work
		postCreate(rKey, epr, constructionParameters, resolverCreationParams);
	
		rKey.dereference().commit();
		_logger.debug("Created resource \"" + rKey.getKey() + "\" for service \"" +
			rKey.getServiceName() + "\".");
		EndpointReferenceType resolveEPR = addResolvers(rKey, epr, resolverCreationParams);
		return new VcgrCreateResponse(resolveEPR);
	}
	
	protected EndpointReferenceType addResolvers(ResourceKey rKey, 
			EndpointReferenceType newEPR,
			Collection<MessageElement> resolverCreationParams) 
			throws ResourceException, BaseFaultType
	{
		EndpointReferenceType resolveEPR = newEPR;
		
		//convert collection to array of message elements
		MessageElement[] resolverCreationParamsArray = null;
		if (!resolverCreationParams.isEmpty()){
			int paramCnt = resolverCreationParams.size();
			int paramIter = 0;
			resolverCreationParamsArray = new MessageElement[paramCnt];
			Iterator<MessageElement>collectionIter = resolverCreationParams.iterator();
			for(;paramIter < paramCnt; paramIter++){
				resolverCreationParamsArray[paramIter]=
					collectionIter.next();
			}
		}		
		
		/* parse config info for service to see if there is a default resolver service */
		try
		{
			Class<? extends IResolverFactoryProxy> resolverFactoryProxyClass = getDefaultResolverFactoryProxyClass();
		
			if (resolverFactoryProxyClass != null)
			{
				Properties resolverFactoryProps = getDefaultResolverFactoryProperties();
				
				IResolverFactoryProxy resolverFactoryProxy = 
					(IResolverFactoryProxy) resolverFactoryProxyClass.newInstance();
				if (resolverFactoryProxy != null)
				{
					Resolution newResolution = resolverFactoryProxy.createResolver(
							newEPR,
							resolverFactoryProps,
							resolverCreationParamsArray);
					if (newResolution != null){
						_logger.debug("Setup new resolver for instance of service \"" + _serviceName);
						resolveEPR = newResolution.getResolvedTargetEPR();
					}
					else{
						_logger.debug("No resolver setup for instance of service \"" + _serviceName);
						resolveEPR = newEPR;
					}
				}
			}
		}
		catch(Throwable t)
		{
			_logger.error("Could not create resolver for new instance of service " + _serviceName, t);
//			throw new ResourceException("Could not create resolver for new instance of service " + _serviceName, t);
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
				
				rKey = ResourceManager.createServiceResource(_serviceName, 
					constructionParameters);
				rKey.dereference().commit();
			}
		}
		catch (ResourceException re)
		{
			_logger.error(re);
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}

		return serviceCreated;
	}
	
	static private Long _serviceCertificateLifetime = null;
	static private Long _resourceCertificateLifetime = null;
	
	@SuppressWarnings("unchecked")
	private void setLifetimes()
	{
		if (_serviceCertificateLifetime == null)		
			synchronized(GenesisIIBase.class)
			/* ASG, August 10, 2008. The current code may over synchronize, 
			 * and depending on the cost of synchronization, may take 
			 * too much time. A (most times) lock free way to check is
			 * if (_serviceCertificateLifetime == null)
			 * 	{
			 * 		synchronized(GenesisIIBase.class)
			 * 	Rest of code as before
			 *  }
			 *  Basically, if is not null (the vast majority of times) no synch is performed.
			 */
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
							_resourceCertificateLifetime =
								new Long(desc.getResourceCertificateLifetime());
							_serviceCertificateLifetime = 
								new Long(desc.getServiceCertificateLifetime());
						}
					}
				}
			}
	}
	
	protected long getServiceCertificateLifetime()
	{
		setLifetimes();
		
		return _serviceCertificateLifetime.longValue();
	}
	
	protected long getResourceCertificateLifetime()
	{
		setLifetimes();
		
		return _resourceCertificateLifetime.longValue();
	}
	
	// Returns null if security is turned off.
	// If we decided to make the resource certificates children of the service certificate
	// we would revisit this spot.
	protected CertCreationSpec getChildCertSpec() 
		throws ResourceException, ResourceUnknownFaultType
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
	
	protected IteratorInitializationType createWSIterator(
		Iterator<MessageElement> contents, int defaultBatchSize) 
			throws ResourceException, ResourceUnknownFaultType,
				SQLException, GenesisIISecurityException, RemoteException
	{
		Collection<IteratorMemberType> initMembers = 
			new LinkedList<IteratorMemberType>();
		int lcv;
		for ( lcv = 0; lcv < defaultBatchSize; lcv++)
		{
			if (!contents.hasNext())
				break;
			initMembers.add(new IteratorMemberType(
				new MessageElement[] { contents.next() }, 
				new UnsignedInt((long)lcv)));
		}

		if (!contents.hasNext())
			return new IteratorInitializationType(
				null, initMembers.toArray(new IteratorMemberType[0]));
		
		String id = (new GUID()).toString();
		DatabaseConnectionPool pool =
			((BasicDBResourceFactory)ResourceManager.getCurrentResource(
				).getProvider().getFactory()).getConnectionPool();
		Connection connection = null;
		PreparedStatement stmt = null;
		long count = defaultBatchSize;
		boolean needsExecute = false;
		
		try
		{
			connection = pool.acquire();
			stmt = connection.prepareStatement(
				"INSERT INTO iterators (iteratorid, elementindex, contents) " +
				"VALUES(?, ?, ?)");
			while (contents.hasNext())
			{
				MessageElement any = contents.next();
				IteratorMemberType member = new IteratorMemberType(
					new MessageElement[] { any }, new UnsignedInt(count));
				stmt.setString(1, id);
				stmt.setLong(2, count);
				stmt.setBlob(3, DBSerializer.xmlToBlob(member));
				
				stmt.addBatch();
				needsExecute = true;
				count++;
				
				if (count % 16 == 0)
				{
					stmt.executeBatch();
					needsExecute = false;
				}
			}
			
			if (needsExecute)
				stmt.executeBatch();
			
			EndpointReferenceType epr;

			
			MessageElement []createRequest =
				new MessageElement[2];
			createRequest[0] = new MessageElement(
				IteratorResource.ITERATOR_CONSTRUCTION_PARAM_ID, id);
			createRequest[1] = 
				ClientConstructionParameters.createTimeToLiveProperty(
					1000L * 60 * 60);

			if (_iteratorServiceEPR == null)
				_iteratorServiceEPR = EPRUtils.makeEPR(
					Container.getServiceURL("IteratorPortType"));
/*			
			IteratorPortType iter = ClientUtils.createProxy(
				IteratorPortType.class, _iteratorServiceEPR);
			epr = iter.vcgrCreate(
					new VcgrCreate(createRequest)).getEndpoint();
*/							
			//long start = System.currentTimeMillis();
			// ASG Here is where we need to directly create in the current container */
			epr = new IteratorServiceImpl().CreateEPR(createRequest, Container.getServiceURL("IteratorPortType"));
			//System.err.println("createWSIterator: time to createEPR " + (System.currentTimeMillis()-start));

			connection.commit();
			return new IteratorInitializationType(epr, 
				initMembers.toArray(new IteratorMemberType[0]));
		}
		finally
		{
			StreamUtils.close(stmt);
			pool.release(connection);
		}
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public SubscribeResponse subscribe(Subscribe subscribeRequest) 
		throws RemoteException, ResourceUnknownFaultType
	{
		EndpointReferenceType target = subscribeRequest.getTarget();
		UnsignedLong ttl = subscribeRequest.getTimeToLive();
		Token topic = subscribeRequest.getTopic();
		UserDataType userData = subscribeRequest.getUserData();
		
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
	
	@SuppressWarnings("unchecked")
	private void setDefaultResolverFactoryDescription()
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

	protected Properties getDefaultResolverFactoryProperties()
	{
		setDefaultResolverFactoryDescription();
		return _defaultResolverFactoryProperties;
	}

	
	protected Class<? extends IResolverFactoryProxy> getDefaultResolverFactoryProxyClass()
	{
		setDefaultResolverFactoryDescription();
		return _defaultResolverFactoryProxyClass;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
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
	    			getImplementedPortTypes(null)));
	    	
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
	@RWXMapping(RWXCategory.WRITE)
	public SetTerminationTimeResponse setTerminationTime(
			SetTerminationTime setTerminationTimeRequest)
			throws RemoteException, ResourceUnknownFaultType,
			UnableToSetTerminationTimeFaultType, ResourceUnavailableFaultType,
			TerminationTimeChangeRejectedFaultType
	{
		org.apache.axis.types.Duration duration = 
			setTerminationTimeRequest.getRequestedLifetimeDuration();
		Calendar c = setTerminationTimeRequest.getRequestedTerminationTime();
		
		if (c == null)
			c = duration.getAsCalendar();
		
		setScheduledTerminationTime(c);
		return new SetTerminationTimeResponse(c, Calendar.getInstance());
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public DeleteResourcePropertiesResponse deleteResourceProperties(
			DeleteResourceProperties deleteResourcePropertiesRequest)
			throws RemoteException, InvalidResourcePropertyQNameFaultType,
			InvalidModificationFaultType,
			DeleteResourcePropertiesRequestFailedFaultType,
			ResourceUnknownFaultType, UnableToModifyResourcePropertyFaultType,
			ResourceUnavailableFaultType
	{
		setResourceProperties(deleteResourcePropertiesRequest.getDelete());
		return new DeleteResourcePropertiesResponse();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetResourcePropertyDocumentResponse getResourcePropertyDocument(
			GetResourcePropertyDocument getResourcePropertyDocumentRequest)
			throws RemoteException, ResourceUnknownFaultType,
			ResourceUnavailableFaultType
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
		
		return new GetResourcePropertyDocumentResponse(elementsArray);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public InsertResourcePropertiesResponse insertResourceProperties(
			InsertResourceProperties insertResourcePropertiesRequest)
			throws RemoteException,
			InsertResourcePropertiesRequestFailedFaultType,
			InvalidResourcePropertyQNameFaultType,
			InvalidModificationFaultType, ResourceUnknownFaultType,
			UnableToModifyResourcePropertyFaultType,
			ResourceUnavailableFaultType
	{
		setResourceProperties(insertResourcePropertiesRequest.getInsert());
		return new InsertResourcePropertiesResponse();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public QueryResourcePropertiesResponse queryResourceProperties(
			QueryResourceProperties queryResourcePropertiesRequest)
			throws RemoteException, UnknownQueryExpressionDialectFaultType,
			QueryEvaluationErrorFaultType, InvalidQueryExpressionFaultType,
			InvalidResourcePropertyQNameFaultType, ResourceUnknownFaultType,
			ResourceUnavailableFaultType
	{
		QueryExpressionType qet = queryResourcePropertiesRequest.getQueryExpression();
		
		// First, make sure we understand the query expresion dialect
		URI dialect = qet.getDialect();
		if (!dialect.toString().equals(WSRFConstants.XPATH_QUERY_EXPRESSION_DIALECT_STRING))
			throw FaultManipulator.fillInFault(new UnknownQueryExpressionDialectFaultType(
				null, null, null, null, null, null));
		
		// Form document
		try
		{
			MessageElement doc = new MessageElement(
				new QName("http://tempuri.org", "ResourcePropertiesDocument"));
			for (MessageElement elem : getResourcePropertyDocument(new GetResourcePropertyDocument()).get_any())
			{
				doc.addChild(elem);
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(baos);
			ObjectSerializer.serialize(writer, doc, doc.getQName());
			writer.flush();
			InputSource inputSource = new InputSource(new ByteArrayInputStream(baos.toByteArray()));
			
			String str = (String)XPathFactory.newInstance().newXPath().evaluate(
				"/*/Ticker", inputSource, XPathConstants.STRING);
			System.err.println("The String is \"" + str + "\".");
			
			return null;
		}
		catch (SOAPException se)
		{
			_logger.error("Unable to query resource properties.", se);
			throw FaultManipulator.fillInFault(
				new QueryEvaluationErrorFaultType(null, null, null, null, null, null));
		}
		catch (IOException ioe)
		{
			_logger.error("Unable to query resource properties.", ioe);
			throw FaultManipulator.fillInFault(
				new QueryEvaluationErrorFaultType(null, null, null, null, null, null));
		}
		catch (XPathExpressionException xpee)
		{
			_logger.error("Unable to query resource properties.", xpee);
			throw FaultManipulator.fillInFault(
				new InvalidQueryExpressionFaultType(null, null, null, null, null, null));
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public SetResourcePropertiesResponse setResourceProperties(
			SetResourceProperties setResourcePropertiesRequest)
			throws RemoteException, InvalidResourcePropertyQNameFaultType,
			InvalidModificationFaultType,
			SetResourcePropertyRequestFailedFaultType,
			ResourceUnknownFaultType, UnableToModifyResourcePropertyFaultType,
			ResourceUnavailableFaultType
	{
		setResourceProperties(setResourcePropertiesRequest.getInsert());
		setResourceProperties(setResourcePropertiesRequest.getUpdate());
		setResourceProperties(setResourcePropertiesRequest.getDelete());
		
		return new SetResourcePropertiesResponse();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public UpdateResourcePropertiesResponse updateResourceProperties(
			UpdateResourceProperties updateResourcePropertiesRequest)
			throws RemoteException, InvalidResourcePropertyQNameFaultType,
			InvalidModificationFaultType, ResourceUnknownFaultType,
			UnableToModifyResourcePropertyFaultType,
			UpdateResourcePropertiesRequestFailedFaultType,
			ResourceUnavailableFaultType
	{
		setResourceProperties(updateResourcePropertiesRequest.getUpdate());
		return new UpdateResourcePropertiesResponse();
	}
	
	private void setResourceProperties(DeleteType del)
		throws RemoteException
	{
		QName rp = del.getResourceProperty();
		
		IAttributeManipulator manipulator =
			_attributePackage.getManipulator(rp);
			
		if (manipulator == null)
			FaultManipulator.fillInFault(
				new InvalidResourcePropertyQNameFaultType(
					null, null, null, null, new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription("The resource property " + rp + " is unknown.")
					}, null));
			
		manipulator.setAttributeValues(new ArrayList<MessageElement>());
	}
	
	private void setResourceProperties(InsertType ins) throws RemoteException
	{
		HashMap<QName, Collection<MessageElement>> map =
			new HashMap<QName, Collection<MessageElement>>();
		
		MessageElement []any = ins.get_any();
		for (MessageElement elem : any)
		{
			QName name = elem.getQName();
			Collection<MessageElement> list = map.get(name);
			if (list == null)
				map.put(name, list = new ArrayList<MessageElement>());
			list.add(elem);
		}
		
		for (QName attrName : map.keySet())
		{
			Collection<MessageElement> newAttrs = map.get(attrName);
			
			IAttributeManipulator manip = getAttributePackage().getManipulator(attrName);
			if (manip == null)
			{
				FaultManipulator.fillInFault(
					new InvalidResourcePropertyQNameFaultType(
						null, null, null, null, new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription("The resource property " + attrName + " is unknown.")
						}, null));
			}
			
			Collection<MessageElement> oldValues = new ArrayList<MessageElement>(
				manip.getAttributeValues());
			oldValues.addAll(newAttrs);
			manip.setAttributeValues(oldValues);
		}
	}
	
	private void setResourceProperties(UpdateType update)
		throws RemoteException
	{
		HashMap<QName, Collection<MessageElement>> map =
			new HashMap<QName, Collection<MessageElement>>();
		
		MessageElement []any = update.get_any();
		for (MessageElement elem : any)
		{
			QName name = elem.getQName();
			Collection<MessageElement> list = map.get(name);
			if (list == null)
				map.put(name, list = new ArrayList<MessageElement>());
			list.add(elem);
		}
		
		for (QName attrName : map.keySet())
		{
			Collection<MessageElement> newAttrs = map.get(attrName);
			
			IAttributeManipulator manip = getAttributePackage().getManipulator(attrName);
			if (manip == null)
			{
				throw FaultManipulator.fillInFault(
					new InvalidResourcePropertyQNameFaultType(
						null, null, null, null, new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription("The resource property " + attrName + " is unknown.")
						}, null));
			}
			
			manip.setAttributeValues(newAttrs);
		}
	}
	
	protected Duration getInitialCacheCoherenceWindow()
	{
		return new Duration(0L);
	}
	
	public Duration getCacheCoherenceWindow()
		throws ResourceException, RemoteException
	{
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		Duration gDur = (Duration)resource.getProperty(
			IResource.CACHE_COHERENCE_WINDOW_PROPERTY);
		if (gDur == null)
			gDur = getInitialCacheCoherenceWindow();
		return gDur;
	}
	
	public void setCacheCoherenceWindow(Duration duration)
		throws ResourceException, RemoteException
	{
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.setProperty(
			IResource.CACHE_COHERENCE_WINDOW_PROPERTY, duration);
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public AddMatchingParameterResponseType addMatchingParameter(
			MatchingParameter[] addMatchingParameterRequest)
			throws RemoteException
	{
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.addMatchingParameter(addMatchingParameterRequest);
		return new AddMatchingParameterResponseType();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RemoveMatchingParameterResponseType removeMatchingParameter(
			MatchingParameter[] removeMatchingParameterRequest)
			throws RemoteException
	{
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.removeMatchingParameter(removeMatchingParameterRequest);
		return new RemoveMatchingParameterResponseType();
	}
}