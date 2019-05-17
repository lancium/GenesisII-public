package edu.virginia.vcgr.genii.container.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.inject.InjectionException;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnavailableFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
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
import org.oasis_open.wsn.base.GetCurrentMessage;
import org.oasis_open.wsn.base.GetCurrentMessageResponse;
import org.oasis_open.wsn.base.InvalidFilterFaultType;
import org.oasis_open.wsn.base.InvalidMessageContentExpressionFaultType;
import org.oasis_open.wsn.base.InvalidProducerPropertiesExpressionFaultType;
import org.oasis_open.wsn.base.InvalidTopicExpressionFaultType;
import org.oasis_open.wsn.base.MultipleTopicsSpecifiedFaultType;
import org.oasis_open.wsn.base.NoCurrentMessageOnTopicFaultType;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.Notify;
import org.oasis_open.wsn.base.NotifyMessageNotSupportedFaultType;
import org.oasis_open.wsn.base.Subscribe;
import org.oasis_open.wsn.base.SubscribeCreationFailedFaultType;
import org.oasis_open.wsn.base.SubscribeResponse;
import org.oasis_open.wsn.base.TopicExpressionDialectUnknownFaultType;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType;
import org.oasis_open.wsn.base.UnrecognizedPolicyRequestFaultType;
import org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.w3c.dom.Document;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.algorithm.structures.queue.IServiceWithCleanupHook;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.comm.axis.Elementals;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextException;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.resource.AttributedURITypeSmart;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.utils.creation.CreationProperties;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.DefaultNotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.NotificationHelper;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.DefaultSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeRequest;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.TerminationTimeType;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ResourceTerminationContents;
import edu.virginia.vcgr.genii.common.AddMatchingParameterResponseType;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.HistoryEventBundleType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsRequestType;
import edu.virginia.vcgr.genii.common.IterateHistoryEventsResponseType;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.common.RemoveMatchingParameterResponseType;
import edu.virginia.vcgr.genii.common.notification.NotifyResponseType;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.alarms.AlarmIdentifier;
import edu.virginia.vcgr.genii.container.alarms.AlarmManager;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.attrs.IAttributeManipulator;
import edu.virginia.vcgr.genii.container.common.notification.GeniiSubscriptionServiceImpl;
import edu.virginia.vcgr.genii.container.common.notification.SubscriptionConstructionParameters;
import edu.virginia.vcgr.genii.container.configuration.GenesisIIServiceConfiguration;
import edu.virginia.vcgr.genii.container.configuration.GenesisIIServiceConfigurationFactory;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.history.CloseableIterator;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContainerService;
import edu.virginia.vcgr.genii.container.invoker.BaseFaultFixer;
import edu.virginia.vcgr.genii.container.invoker.DatabaseHandler;
import edu.virginia.vcgr.genii.container.invoker.DebugInvoker;
import edu.virginia.vcgr.genii.container.invoker.GAroundInvoke;
import edu.virginia.vcgr.genii.container.invoker.ScheduledTerminationInvoker;
import edu.virginia.vcgr.genii.container.invoker.ServiceInitializationLocker;
import edu.virginia.vcgr.genii.container.invoker.SoapHeaderHandler;
import edu.virginia.vcgr.genii.container.invoker.inject.MInjectionInvoker;
import edu.virginia.vcgr.genii.container.invoker.timing.TimingHandler;
import edu.virginia.vcgr.genii.container.iterator.AbstractIteratorBuilder;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;
import edu.virginia.vcgr.genii.container.iterator.IteratorBuilder;
import edu.virginia.vcgr.genii.container.iterator.WSIteratorConstructionParameters;
import edu.virginia.vcgr.genii.container.iterator.WSIteratorServiceImpl;
import edu.virginia.vcgr.genii.container.resolver.IResolverFactoryProxy;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResource;
import edu.virginia.vcgr.genii.container.resource.db.BasicDBResourceProvider;
import edu.virginia.vcgr.genii.container.resource.db.query.ResourceSummary;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclAuthZProvider;
import edu.virginia.vcgr.genii.container.serializer.MessageElementSerializer;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.iterator.IterableElementType;
import edu.virginia.vcgr.genii.iterator.IteratorInitializationType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;
import edu.virginia.vcgr.genii.security.x509.CertCreationSpec;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.system.classloader.GenesisClassLoader;

@GAroundInvoke({ ServiceInitializationLocker.class, BaseFaultFixer.class, SoapHeaderHandler.class, DatabaseHandler.class, DebugInvoker.class,
	ScheduledTerminationInvoker.class, MInjectionInvoker.class, TimingHandler.class })
@ConstructionParametersType(ConstructionParameters.class)
@GeniiServiceConfiguration(resourceProvider = BasicDBResourceProvider.class, defaultAuthZProvider = AclAuthZProvider.class)
public abstract class GenesisIIBase implements GeniiCommon, IServiceWithCleanupHook, GenesisIIBaseTopics
{
	static private Log _logger = LogFactory.getLog(GenesisIIBase.class);

	static private class ServiceBasedSubscriptionFactory extends DefaultSubscriptionFactory
	{
		private ServiceBasedSubscriptionFactory(EndpointReferenceType consumer)
		{
			super(consumer);
		}
	}

	private NotificationMultiplexer _notificationMultiplexer = null;

	protected String _serviceName;
	public ArrayList<PortType> _implementedPortTypes = new ArrayList<PortType>();
	private AttributePackage _attributePackage = new AttributePackage();

	private IResolverFactoryProxy _defaultResolverFactoryProxy = null;

	public abstract PortType getFinalWSResourceInterface();

	final private NotificationMultiplexer notificationMultiplexer()
	{
		synchronized (this) {
			if (_notificationMultiplexer == null) {
				_notificationMultiplexer = new DefaultNotificationMultiplexer();
				registerNotificationHandlers(_notificationMultiplexer);
			}
		}

		return _notificationMultiplexer;
	}

	protected SubscriptionFactory subscriptionFactory() throws ResourceUnknownFaultType, ResourceException
	{
		return new ServiceBasedSubscriptionFactory(getMyEPR(false));
	}

	protected void registerNotificationHandlers(NotificationMultiplexer multiplexer)
	{
		// We don't implement this by default, but a derived service can.
	}

	protected AttributePackage getAttributePackage()
	{
		return _attributePackage;
	}

	protected void addImplementedPortType(PortType portType)
	{
		synchronized (_implementedPortTypes) {
			_implementedPortTypes.add(portType);
		}
		if (_logger.isTraceEnabled())
			_logger.trace("added port type " + portType + " for this class " + this.toString());
	}

	protected void setAttributeHandlers() throws NoSuchMethodException, ResourceException, ResourceUnknownFaultType
	{
		new GenesisIIBaseAttributesHandler(this, getAttributePackage());
	}

	protected GenesisIIBase(String serviceName) throws RemoteException
	{
		GenesisClassLoader.classLoaderFactory().addLoader(Thread.currentThread().getContextClassLoader());
		// normally one uses: this.getClass().getClassLoader();
		_serviceName = serviceName;

		addImplementedPortType(WellKnownPortTypes.GENII_RESOURCE_ATTRS_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.GENII_RESOURCE_FACTORY_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.VCGR_COMMON_PORT_TYPE());
		addImplementedPortType(WSRFConstants.WSRF_RLW_IMMEDIATE_TERMINATE_PORT());
		addImplementedPortType(WSRFConstants.WSRF_RLW_SCHEDULED_TERMINATE_PORT());
		addImplementedPortType(WSRFConstants.WSRF_RPW_GET_RP_PORT());
		addImplementedPortType(WSRFConstants.WSRF_RPW_GET_MULTIPLE_RP_PORT());
		addImplementedPortType(WSRFConstants.WSN_NOTIFICATION_CONSUMER_PORT());
		addImplementedPortType(WSRFConstants.WSN_NOTIFICATION_PRODUCER_PORT());

		try {
			setAttributeHandlers();
		} catch (NoSuchMethodException nsme) {
			_logger.error("Couldn't set attribute handlers.", nsme);
			throw new RemoteException(nsme.getLocalizedMessage(), nsme);
		}
	}

	protected ResourceKey createResource(GenesisHashMap creationParameters) throws ResourceException, BaseFaultType
	{
		if (!creationParameters.containsKey(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM))
			creationParameters.put(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM, WSName.generateNewEPI());

		if (!(this instanceof GeniiNoOutCalls)) {

			CertCreationSpec spec = getChildCertSpec();
			if (spec != null) {
				creationParameters.put(IResource.CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM, spec);
			}

			// set the identity of the service into the creation params
			X509Certificate[] serviceChain =
				(X509Certificate[]) ResourceManager.getCurrentResource().dereference().getProperty(IResource.CERTIFICATE_CHAIN_PROPERTY_NAME);
			creationParameters.put(IResource.SERVICE_CERTIFICATE_CHAIN_CONSTRUCTION_PARAM, serviceChain);
		}
		return ResourceManager.createNewResource(_serviceName, creationParameters);
	}

	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		GenesisHashMap constructionParameters, Collection<MessageElement> resolverCreationParameters)
		throws ResourceException, BaseFaultType, RemoteException
	{
		if (_logger.isTraceEnabled())
			_logger.debug("postCreate: " + this.getClass().getName() + " -- " + constructionParameters.size() + " construct parms, "
				+ resolverCreationParameters.size() + " resolver creation parms");

		IResource resource = rKey.dereference();

		Long timeToLive = cParams.timeToLive();
		if (timeToLive != null) {
			Calendar termTime = Calendar.getInstance();
			termTime.setTime(new Date(new Date().getTime() + timeToLive.longValue()));
			resource.setProperty(IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME, termTime);
		}

		Duration gDur = getInitialCacheCoherenceWindow();
		if (gDur != null)
			resource.setProperty(IResource.CACHE_COHERENCE_WINDOW_PROPERTY, gDur);

		resource.constructionParameters(cParams);
	}

	static protected Calendar getScheduledTerminationTime() throws ResourceException, ResourceUnknownFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		return (Calendar) rKey.dereference().getProperty(IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME);
	}

	final protected void terminationAlarm(AlarmIdentifier alarmID, Object userData) throws ResourceNotDestroyedFaultType, RemoteException
	{
		_logger.info("Resource is self-terminating due to scheduled termination time.");
		destroy(null);
	}

	static protected void setScheduledTerminationTime(Calendar termTime, ResourceKey rKey) throws ResourceUnknownFaultType, ResourceException
	{
		AlarmIdentifier alarmID;
		IResource resource = rKey.dereference();

		resource.setProperty(IResource.SCHEDULED_TERMINATION_TIME_PROPERTY_NAME, termTime);

		alarmID = (AlarmIdentifier) resource.getProperty(IResource.TERM_TIME_ALARM);
		if (alarmID != null)
			alarmID.cancel();

		if (termTime != null)
			alarmID = AlarmManager.getManager().addAlarm(termTime.getTime(), 15 * 1000L, null, null, "terminationAlarm", null);
		resource.setProperty(IResource.TERM_TIME_ALARM, alarmID);

		resource.commit();
	}

	static protected void setScheduledTerminationTime(Calendar termTime) throws ResourceUnknownFaultType, ResourceException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		setScheduledTerminationTime(termTime, rKey);
	}

	/**
	 * translates the property into an Object we know, if possible. otherwise just returns the axis level MessageElement from the property.
	 */
	protected Object translateConstructionParameter(MessageElement property) throws Exception
	{
		QName name = property.getQName();

		if (name.equals(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM)) {
			return ClientConstructionParameters.getEndpointIdentifierProperty(property);
		} else if (name.equals(CreationProperties.CREATION_PROPERTIES_QNAME)) {
			return CreationProperties.translate(property);
		} else if (name.equals(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME)) {
			return ConstructionParameters.deserializeConstructionParameters(getClass(), property);
		} else if (name.equals(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM)) {
			return property.getObjectValue(EndpointReferenceType.class);
		} else {
			return property;
		}
	}

	static private Method findAlarmMethod(Class<?> cl, String methodName, Object userData)
	{
		if (cl == null)
			return null;

		if (userData != null) {
			try {
				return cl.getDeclaredMethod(methodName, AlarmIdentifier.class, userData.getClass());
			} catch (NoSuchMethodException nsme) {
				return findAlarmMethod(cl.getSuperclass(), methodName, userData);
			}
		} else {
			try {
				return cl.getDeclaredMethod(methodName, AlarmIdentifier.class, Object.class);
			} catch (NoSuchMethodException nsme) {
				try {
					return cl.getDeclaredMethod(methodName, AlarmIdentifier.class);
				} catch (NoSuchMethodException nsme2) {
					return findAlarmMethod(cl.getSuperclass(), methodName, userData);
				}
			}
		}
	}

	final public void callAlarmMethod(AlarmIdentifier alarm, String methodName, Object userData) throws ResourceUnknownFaultType
	{
		Method m = findAlarmMethod(getClass(), methodName, userData);
		if (m == null) {
			_logger.warn("Attempt to call alarm method \"" + methodName + "\" which couldn't be found.");
		} else {
			try {
				if (m.getParameterTypes().length == 1)
					m.invoke(this, alarm);
				else
					m.invoke(this, alarm, userData);
			} catch (InvocationTargetException ite) {
				Throwable cause = ite.getCause();
				if (cause instanceof ResourceUnknownFaultType)
					throw (ResourceUnknownFaultType) cause;

				_logger.warn("Calling alarm method \"" + methodName + "\" resulted in exception.", ite.getCause());
			} catch (Throwable cause) {
				_logger.warn("Couldn't call alarm method \"" + methodName + "\".", cause);
			}
		}
	}

	public PortType[] getImplementedPortTypes(ResourceKey rKey) throws ResourceException, ResourceUnknownFaultType
	{
		PortType[] ret;

		synchronized (_implementedPortTypes) {
			ret = new PortType[_implementedPortTypes.size()];
			_implementedPortTypes.toArray(ret);
		}

		return ret;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage arg0)
		throws RemoteException, TopicNotSupportedFaultType, TopicExpressionDialectUnknownFaultType, MultipleTopicsSpecifiedFaultType,
		InvalidTopicExpressionFaultType, ResourceUnknownFaultType, NoCurrentMessageOnTopicFaultType
	{
		throw FaultManipulator.fillInFault(new NoCurrentMessageOnTopicFaultType());
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify msg) throws RemoteException
	{
		_logger.debug("A notification has arrived");
		NotificationHelper.notify(msg, notificationMultiplexer());
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public IterateHistoryEventsResponseType iterateHistoryEvents(IterateHistoryEventsRequestType arg0) throws RemoteException
	{
		HistoryContainerService service = ContainerServices.findService(HistoryContainerService.class);
		CloseableIterator<HistoryEvent> iter = null;

		try {
			if (arg0 != null && arg0.getResourceHint() != null)
				iter = service.iterateEvents(arg0.getResourceHint());
			else
				iter = service.iterateEvents(ResourceManager.getCurrentResource().getResourceKey());

			IteratorBuilder<HistoryEvent> builder = new GenesisIIBaseAbstractIteratorBuilder<HistoryEvent>()
			{
				@Override
				protected MessageElement serialize(HistoryEvent item) throws IOException
				{
					byte[] data = DBSerializer.serialize(item, -1L);
					return new MessageElement(new QName("http://tempuri.org", "data"), new HistoryEventBundleType(data));
				}
			};
			builder.preferredBatchSize(100);
			builder.addElements(iter);

			return new IterateHistoryEventsResponseType(builder.create());
		} catch (SQLException sqe) {
			throw new RemoteException(String.format("Unable to retrieve history events."), sqe);
		} finally {
			StreamUtils.close(iter);
		}
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public NotifyResponseType notifyWithResponse(Notify msg) throws java.rmi.RemoteException
	{
		// A client may send multiple messages to the regular notify() function,
		// but if the client wants a response, then it must send a single message.
		NotificationMessageHolderType[] msgs = msg.getNotificationMessage();
		String status = null;
		if ((msgs == null) || (msgs.length == 0)) {
			if (_logger.isDebugEnabled())
				_logger.debug("notifyWithResponse: received empty notification");
			status = NotificationConstants.FAIL;
		} else {
			if (msgs.length > 1) {
				if (_logger.isDebugEnabled())
					_logger.debug("notifyWithResponse: client sent " + msgs.length + " messages");
			}
			status = NotificationHelper.notifySingleMessage(msgs[0], notificationMultiplexer());
		}
		return new NotifyResponseType(NotificationConstants.toURI(status));
	}

	public static SubscribeResponse processSubscribeRequest(String resourceKey, SubscribeRequest request)
		throws RemoteException, TopicNotSupportedFaultType, TopicExpressionDialectUnknownFaultType, InvalidFilterFaultType,
		UnacceptableInitialTerminationTimeFaultType, SubscribeCreationFailedFaultType, InvalidMessageContentExpressionFaultType,
		InvalidTopicExpressionFaultType, UnsupportedPolicyRequestFaultType, UnrecognizedPolicyRequestFaultType, ResourceUnknownFaultType,
		NotifyMessageNotSupportedFaultType, InvalidProducerPropertiesExpressionFaultType
	{
		Calendar currentTime = Calendar.getInstance();
		TerminationTimeType ttt = request.terminationTime();
		Calendar terminationTime = (ttt == null) ? null : ttt.terminationTime();

		SubscriptionConstructionParameters cons = new SubscriptionConstructionParameters(resourceKey, request.consumerReference(),
			request.topicFilter(), request.policies(), request.additionalUserData());

		if (terminationTime != null)
			cons.timeToLive(terminationTime.getTimeInMillis() - currentTime.getTimeInMillis());

		EndpointReferenceType subscription = new GeniiSubscriptionServiceImpl()
			.CreateEPR(Elementals.unitaryArray(cons.serializeToMessageElement()), Container.getServiceURL("GeniiSubscriptionPortType"));

		return new SubscribeResponse(subscription, currentTime, terminationTime, null);
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public SubscribeResponse subscribe(Subscribe arg0)
		throws RemoteException, TopicNotSupportedFaultType, TopicExpressionDialectUnknownFaultType, InvalidFilterFaultType,
		UnacceptableInitialTerminationTimeFaultType, SubscribeCreationFailedFaultType, InvalidMessageContentExpressionFaultType,
		InvalidTopicExpressionFaultType, UnsupportedPolicyRequestFaultType, UnrecognizedPolicyRequestFaultType, ResourceUnknownFaultType,
		NotifyMessageNotSupportedFaultType, InvalidProducerPropertiesExpressionFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		return processSubscribeRequest(rKey.getResourceKey(), new SubscribeRequest(arg0));
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public final String ping(String request)
	{
		try {
			EndpointReferenceType epr =
				(EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);
			_logger.info(String.format("Container ID \"%s\".\n", EPRUtils.getGeniiContainerID(epr)));
		} catch (ContextException ce) {
			_logger.warn("Unable to get current EPR.");
		}

		return request;
	}

	/**
	 * helper method for both GetResourceProperty and GetMultipleResourceProperties that finds all the requested items and provides an array
	 * which can be packaged up as a MessageElement list needed for the response.
	 */
	public ArrayList<MessageElement> gatherProperties(QName[] getMultipleResourcePropertiesRequest)
		throws RemoteException, InvalidResourcePropertyQNameFaultType, ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> document = new ArrayList<MessageElement>();
		Map<QName, Collection<MessageElement>> unknowns = null;
		QName old_acl_name = new QName(AclAuthZProvider.GENII_ACL_PROPERTY_NAME);
		boolean sawNullList = false; // records if the array is empty.
		boolean requestedAclInfo = false; // records if they wanted to know the ACL information.
		boolean addedAclExplicitly = false; // true if we added the appropriate form of ACL info already.

		for (QName name : getMultipleResourcePropertiesRequest) {
			if (name == null) {
				sawNullList = true;
				if (_logger.isDebugEnabled())
					_logger.debug("** saw null for qname while iterating array, so array is empty, meaning wildcard request.");
			} else if (name.equals(old_acl_name)) {
				requestedAclInfo = true;
				if (_logger.isDebugEnabled())
					_logger.debug("** saw a specific request for the ACL information, so will add.");
			}

			if (_logger.isDebugEnabled())
				_logger.debug("## get mult resource props requesting name: " + name);

			IAttributeManipulator manipulator = _attributePackage.getManipulator(name);

			if (manipulator == null) {
				if (unknowns == null)
					unknowns = _attributePackage.getUnknownAttributes(ResourceManager.getCurrentResource().dereference());

				Collection<MessageElement> values = null;
				// if (!sawNullList && name.equals(old_acl_name)) {
				// if (_logger.isDebugEnabled())
				// _logger.debug("** adding in ACL info in properties request with null manipulator.");
				// Acl acl = BasicDBResource.rationalizeAcl(ResourceManager.getCurrentResource().dereference());
				// values = new ArrayList<MessageElement>(0);
				// values.add(new MessageElement(old_acl_name, AxisAcl.encodeAcl(acl)));
				// addedAclExplicitly = true;
				// } else {
					values = unknowns.get(name);
					if (values == null && unknowns.containsKey(name)) {
						values = new ArrayList<MessageElement>(0);
					}
				// }

				if (values == null) {
					_logger.error("The resource property \"" + name + "\" is unknown.");
					throw FaultManipulator.fillInFault(new InvalidResourcePropertyQNameFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("The resource property " + name + " is unknown.") },
						null));
				}

				document.addAll(values);
			} else {
				document.addAll(manipulator.getAttributeValues());
			}
		}

		/*
		 * see if the item for ACLs was already present, and whack it if so, since it's the older style answer.
		 */
		// for (int i = 0; i < document.size(); i++) {
		// if (document.get(i).getQName().equals(old_acl_name)) {
		// if (_logger.isDebugEnabled())
		// _logger.debug("** found acl property in property-based answer; cleaning out.");
		// document.remove(i);
		// break;
		// }
		// }

		// make sure we have the right conditions before we add in the new form of ACL.
		// if (!addedAclExplicitly && (sawNullList || requestedAclInfo)) {
		// if (_logger.isDebugEnabled())
		// _logger.debug("** adding in ACL info in properties request.");
		// // new.
		// Acl acl = BasicDBResource.rationalizeAcl(ResourceManager.getCurrentResource().dereference());
		// // old. Acl acl = (Acl) ResourceManager.getCurrentResource().dereference().getProperty(AclAuthZProvider.GENII_ACL_PROPERTY_NAME);
		// if (acl != null) {
		// document.add(new MessageElement(old_acl_name, AxisAcl.encodeAcl(acl)));
		// } else {
		// _logger.debug("** not adding ACL to properties request since it came back as null.");
		// }
		// }

		return document;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(QName[] getMultipleResourcePropertiesRequest)
		throws RemoteException, InvalidResourcePropertyQNameFaultType, ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		ArrayList<MessageElement> document = gatherProperties(getMultipleResourcePropertiesRequest);
		MessageElement[] ret = new MessageElement[document.size()];
		document.toArray(ret);
		return new GetMultipleResourcePropertiesResponse(ret);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetResourcePropertyResponse getResourceProperty(QName getResourcePropertyRequest)
		throws RemoteException, InvalidResourcePropertyQNameFaultType, ResourceUnavailableFaultType, ResourceUnknownFaultType
	{
		/*
		 * ArrayList<MessageElement> document = new ArrayList<MessageElement>();
		 * 
		 * IAttributeManipulator manipulator = _attributePackage.getManipulator(getResourcePropertyRequest);
		 * 
		 * if (manipulator == null) { Map<QName, Collection<MessageElement>> unknowns =
		 * _attributePackage.getUnknownAttributes(ResourceManager.getCurrentResource().dereference());
		 * 
		 * if (!unknowns.containsKey(getResourcePropertyRequest)) throw FaultManipulator .fillInFault( new
		 * InvalidResourcePropertyQNameFaultType(null, null, null, null, new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(
		 * "The resource property " + getResourcePropertyRequest + " is unknown.") }, null));
		 * 
		 * document.addAll(unknowns.get(getResourcePropertyRequest)); } else { document.addAll(manipulator.getAttributeValues()); }
		 */

		ArrayList<MessageElement> document = gatherProperties(new QName[] { getResourcePropertyRequest });
		MessageElement[] ret = new MessageElement[document.size()];
		document.toArray(ret);
		return new GetResourcePropertyResponse(ret);
	}

	protected void preDestroy() throws RemoteException, ResourceException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();

		AlarmIdentifier alarmID = (AlarmIdentifier) resource.getProperty(IResource.TERM_TIME_ALARM);
		if (alarmID != null)
			alarmID.cancel();
	}

	/**
	 * Quick test for overriding classes to implement should they desire to disable resource creation on this endpoint
	 * 
	 * @return true if vcgrCreate is applicable, false otherwise.
	 */
	protected boolean allowVcgrCreate() throws ResourceException, ResourceUnknownFaultType
	{
		return true;
	}

	public EndpointReferenceType CreateEPR(MessageElement[] creationParameters, String targetServiceURL)
		throws RemoteException, ResourceCreationFaultType, BaseFaultType
	{
		GenesisHashMap constructionParameters = new GenesisHashMap();

		if (creationParameters != null) {
			for (MessageElement property : creationParameters) {
				try {
					constructionParameters.put(property.getQName(), translateConstructionParameter(property));
				} catch (Exception e) {
					_logger.warn("Error while trying to createEPR.", e);
					throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(e.getLocalizedMessage()) }, null));
				}
			}
		}

		ConstructionParameters cParams =
			(ConstructionParameters) constructionParameters.get(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME);
		if (cParams == null) {
			cParams = ConstructionParameters.instantiateDefault(getClass());
			constructionParameters.put(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME, cParams);
		}

		ResourceKey rKey = createResource(constructionParameters);
		EndpointReferenceType epr = ResourceManager.createEPR(rKey, targetServiceURL, getImplementedPortTypes(rKey), getMasterType(rKey));

		WorkingContext.temporarilyAssumeNewIdentity(epr);
		WorkingContext.getCurrentWorkingContext().setProperty(WorkingContext.CURRENT_RESOURCE_KEY, rKey);

		try {
			if (!(this instanceof GeniiNoOutCalls)) {
				try {
					CallingContextImpl context = new CallingContextImpl((CallingContextImpl) null);
					context.setActiveKeyAndCertMaterial(
						new KeyAndCertMaterial((X509Certificate[]) constructionParameters.get(IResource.CERTIFICATE_CHAIN_CONSTRUCTION_PARAM),
							Container.getContainerPrivateKey()));
					rKey.dereference().setProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME, context);
				} catch (AuthZSecurityException gse) {
					throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription("Security error while initializing new resource's calling context."),
							new BaseFaultTypeDescription(gse.getLocalizedMessage()) },
						null));
				}
			} else {
				_logger.error("In createEPR 2 params, it is GeniiNoOutcalls");
			}

			Collection<MessageElement> resolverCreationParams = new Vector<MessageElement>();

			try {
				MInjectionInvoker.inject(this);
			} catch (InjectionException e) {
				throw new RemoteException("Unable to inject instance.", e);
			}

			IResource resource = rKey.dereference();
			if (resource instanceof BasicDBResource) {
				try {
					String hName = cParams.humanName();
					ResourceSummary.addResource(((BasicDBResource) resource).getConnection(), rKey.getResourceKey(), hName,
						Container.getClassForServiceURL(targetServiceURL), epr);
				} catch (Throwable cause) {
					_logger.warn("Unable to note creation of resource.", cause);
				}
			}

			// allow subclasses to do creation work
			postCreate(rKey, epr, cParams, constructionParameters, resolverCreationParams);

			return epr;
		} finally {
			WorkingContext.releaseAssumedIdentity();
		}
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public final VcgrCreateResponse vcgrCreate(VcgrCreate createRequest) throws RemoteException, ResourceCreationFaultType
	{
		if (!allowVcgrCreate()) {
			throw new RemoteException("\"vcgrCreate\" not applicable.");
		}
		EndpointReferenceType myEPR =
			(EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);

		if (myEPR == null) {
			myEPR = new EndpointReferenceType(new AttributedURITypeSmart(Container.getServiceURL(_serviceName)), null, null, null);
			WorkingContext.getCurrentWorkingContext().setProperty(WorkingContext.EPR_PROPERTY_NAME, myEPR);
		}

		GenesisHashMap constructionParameters = new GenesisHashMap();

		MessageElement[] creationParameters = createRequest.get_any();
		if (creationParameters != null) {
			for (MessageElement property : creationParameters) {
				try {
					constructionParameters.put(property.getQName(), translateConstructionParameter(property));
				} catch (Exception e) {
					if (e instanceof RemoteException)
						throw (RemoteException) e;

					throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(e.getLocalizedMessage()) }, null));
				}
			}
		}

		ConstructionParameters cParams =
			(ConstructionParameters) constructionParameters.get(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME);
		if (cParams == null) {
			cParams = ConstructionParameters.instantiateDefault(getClass());
			constructionParameters.put(ConstructionParameters.CONSTRUCTION_PARAMETERS_QNAME, cParams);
		}

		ResourceKey rKey = createResource(constructionParameters);
		AttributedURIType targetAddress = myEPR.getAddress();
		if (EPRUtils.getGeniiContainerID(myEPR) == null) {
			targetAddress = new AttributedURIType(
				String.format("%s?%s=%s", targetAddress.get_value(), EPRUtils.GENII_CONTAINER_ID_PARAMETER, Container.getContainerID()));
		}

		EndpointReferenceType epr =
			ResourceManager.createEPR(rKey, targetAddress.toString(), getImplementedPortTypes(rKey), getMasterType(rKey));

		if (!(this instanceof GeniiNoOutCalls)) {
			try {
				CallingContextImpl context = new CallingContextImpl((CallingContextImpl) null);
				context.setActiveKeyAndCertMaterial(
					new KeyAndCertMaterial((X509Certificate[]) constructionParameters.get(IResource.CERTIFICATE_CHAIN_CONSTRUCTION_PARAM),
						Container.getContainerPrivateKey()));
				rKey.dereference().setProperty(IResource.STORED_CALLING_CONTEXT_PROPERTY_NAME, context);
			} catch (AuthZSecurityException gse) {
				throw FaultManipulator.fillInFault(new ResourceCreationFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription("Security error while initializing new resource's calling context."),
						new BaseFaultTypeDescription(gse.getLocalizedMessage()) },
					null));
			}
		}
		// collection to hold creation parameters for default resolvers
		Collection<MessageElement> resolverCreationParams = new Vector<MessageElement>();

		IResource resource = rKey.dereference();
		EndpointReferenceType resolveEPR = addResolvers(rKey, epr, resolverCreationParams);
		if (resource instanceof BasicDBResource) {
			try {
				String hName = cParams.humanName();
				ResourceSummary.addResource(((BasicDBResource) resource).getConnection(), rKey.getResourceKey(), hName, getClass(),
					resolveEPR);
			} catch (Throwable cause) {
				_logger.warn("Unable to note creation of resource.", cause);
			}
		}

		// allow subclasses to do creation work
		postCreate(rKey, epr, cParams, constructionParameters, resolverCreationParams);

		if (_logger.isDebugEnabled())
			_logger.debug("Created resource \"" + rKey.getResourceKey() + "\" for service \"" + rKey.getServiceName() + "\".");

		resource.commit();
		return new VcgrCreateResponse(resolveEPR);
	}

	private EndpointReferenceType addResolvers(ResourceKey rKey, EndpointReferenceType newEPR,
		Collection<MessageElement> resolverCreationParams)
	{
		// Convert collection to array of message elements
		MessageElement[] resolverCreationParamsArray = null;
		if (!resolverCreationParams.isEmpty()) {
			int paramCnt = resolverCreationParams.size();
			resolverCreationParamsArray = new MessageElement[paramCnt];
			Iterator<MessageElement> collectionIter = resolverCreationParams.iterator();
			for (int idx = 0; idx < paramCnt; idx++) {
				resolverCreationParamsArray[idx] = collectionIter.next();
			}
		}

		// Parse config info for service to see if there is a default resolver service.
		try {
			IResolverFactoryProxy resolverFactoryProxy = getDefaultResolverFactoryProxy();
			if (resolverFactoryProxy != null) {
				EndpointReferenceType resolvedEPR = resolverFactoryProxy.createResolver(newEPR, null, resolverCreationParamsArray);
				if (resolvedEPR == null) {
					if (_logger.isDebugEnabled())
						_logger.debug("Resolver proxy failed for service " + _serviceName);
					return newEPR;
				}
				if (_logger.isDebugEnabled())
					_logger.debug("Created new resolver for instance of service " + _serviceName);
				return resolvedEPR;
			}
		} catch (Throwable error) {
			_logger.error("Create resolver failed for service " + _serviceName, error);
		}
		return newEPR;
	}

	@Override
	public void cleanupHook()
	{
		// By default, nothing needs to be done.
	}

	/**
	 * Called by the container to initialize the service. Returns true if the service is being *created* for the first time.
	 */
	@Override
	public boolean startup()
	{
		boolean serviceCreated = false;

		_logger.info("Initializing service: " + getClass().getName());

		ResourceKey rKey = null;

		try {
			WorkingContext.setCurrentWorkingContext(new WorkingContext());

			try {
				rKey = ResourceManager.getServiceResource(_serviceName);
			} catch (ResourceUnknownFaultType ruft) {
				// need to create the service resource.
				serviceCreated = true;

				GenesisHashMap constructionParameters = new GenesisHashMap();

				constructionParameters.put(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM, WSName.generateNewEPI());

				// need to create the service resource
				CertCreationSpec serviceCertSpec = null;
				X509Certificate[] containerChain = Container.getContainerCertChain();
				// If this is null, then security isn't turned on.
				if (containerChain != null) {
					serviceCertSpec = new CertCreationSpec(containerChain[0].getPublicKey(), containerChain,
						Container.getContainerPrivateKey(), getServiceCertificateLifetime());

					constructionParameters.put(IResource.CERTIFICATE_CREATION_SPEC_CONSTRUCTION_PARAM, serviceCertSpec);
				}

				rKey = ResourceManager.createServiceResource(_serviceName, constructionParameters);
				rKey.dereference().commit();
			}
		} catch (ResourceException re) {
			_logger.error(re);
		} finally {
			WorkingContext.setCurrentWorkingContext(null);
		}

		ServiceInitializationLocker.setInitialized(getClass());
		return serviceCreated;
	}

	@Override
	public void postStartup()
	{
		// Do nothing by default.
	}

	static private Long _serviceCertificateLifetime = null;
	static private Long _resourceCertificateLifetime = null;

	private void setLifetimes()
	{
		if (_serviceCertificateLifetime == null) {
			synchronized (GenesisIIBase.class)
			/*
			 * ASG, August 10, 2008. The current code may over synchronize, and depending on the cost of synchronization, may take too much
			 * time. A (most times) lock free way to check is if (_serviceCertificateLifetime == null) { synchronized(GenesisIIBase.class)
			 * Rest of code as before } Basically, if is not null (the vast majority of times) no synch is performed.
			 */
			{
				if (_serviceCertificateLifetime == null) {
					GenesisIIServiceConfiguration conf = GenesisIIServiceConfigurationFactory.configurationFor(getClass());
					_resourceCertificateLifetime = conf.defaultResourceCertificateLifetime();
					_serviceCertificateLifetime = conf.defaultServiceCertificateLifetime();
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
	protected CertCreationSpec getChildCertSpec() throws ResourceException, ResourceUnknownFaultType
	{
		X509Certificate[] containerChain = Container.getContainerCertChain();
		// If this is null, then security isn't turned on.
		if (containerChain != null) {
			return new CertCreationSpec(containerChain[0].getPublicKey(), containerChain, Container.getContainerPrivateKey(),
				getServiceCertificateLifetime());
		}

		return null;
	}

	protected SByteIOFactory createStreamableByteIOResource() throws IOException
	{
		return new SByteIOFactory(Container.getServiceURL("StreamableByteIOPortType"));
	}

	final protected IteratorBuilder<MessageElement> iteratorBuilder()
	{
		return new MessageElementIteratorBuilder();
	}

	final protected IteratorBuilder<Object> iteratorBuilder(Marshaller jaxbMarshaller)
	{
		return new JaxbIteratorBuilder(jaxbMarshaller, null);
	}

	final protected IteratorBuilder<Object> iteratorBuilder(Marshaller jaxbMarshaller, QName elementName)
	{
		return new JaxbIteratorBuilder(jaxbMarshaller, elementName);
	}

	final protected IteratorBuilder<Object> iteratorBuilder(QName axisElementName)
	{
		return new AxisSerializationIteratorBuilder(axisElementName);
	}

	public EndpointReferenceType getMyEPR(boolean withPortTypes) throws ResourceUnknownFaultType, ResourceException
	{
		String myAddress = Container.getServiceURL(_serviceName);
		ResourceKey rKey = ResourceManager.getCurrentResource();
		PortType[] implementedPortTypes = null;
		String masterPortType = null;

		if (withPortTypes) {
			implementedPortTypes = getImplementedPortTypes(rKey);
			masterPortType = getMasterType(rKey);
		}

		return ResourceManager.createEPR(rKey, myAddress, implementedPortTypes, masterPortType);
	}

	protected IResolverFactoryProxy getDefaultResolverFactoryProxy()
	{
		synchronized (GenesisIIBase.class) {
			if (_defaultResolverFactoryProxy == null) {
				GenesisIIServiceConfiguration conf = GenesisIIServiceConfigurationFactory.configurationFor(getClass());
				_defaultResolverFactoryProxy = conf.defaultResolverFactoryProxy();
			}
		}
		return _defaultResolverFactoryProxy;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public DestroyResponse destroy(Destroy destroyRequest)
		throws RemoteException, ResourceUnknownFaultType, ResourceNotDestroyedFaultType, ResourceUnavailableFaultType
	{
		ResourceKey resource = ResourceManager.getCurrentResource();
		if (_logger.isDebugEnabled())
			_logger.debug("Destroy resource \"" + resource.getResourceKey() + "\" for service \"" + resource.getServiceName() + "\".");
		preDestroy();

		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic topic = space.createPublisherTopic(RESOURCE_TERMINATION_TOPIC);
		topic.publish(new ResourceTerminationContents(Calendar.getInstance()));

		try {
			resource.destroy();
			resource.dereference().commit();
		} catch (ResourceException re) {
			_logger.error(re);
			throw re;
		}
		return null;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public SetTerminationTimeResponse setTerminationTime(SetTerminationTime setTerminationTimeRequest) throws RemoteException,
		ResourceUnknownFaultType, UnableToSetTerminationTimeFaultType, ResourceUnavailableFaultType, TerminationTimeChangeRejectedFaultType
	{
		org.apache.axis.types.Duration duration = setTerminationTimeRequest.getRequestedLifetimeDuration();
		Calendar c = setTerminationTimeRequest.getRequestedTerminationTime();

		if (c == null)
			c = duration.getAsCalendar();

		setScheduledTerminationTime(c);
		return new SetTerminationTimeResponse(c, Calendar.getInstance());
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public DeleteResourcePropertiesResponse deleteResourceProperties(DeleteResourceProperties deleteResourcePropertiesRequest)
		throws RemoteException, InvalidResourcePropertyQNameFaultType, InvalidModificationFaultType,
		DeleteResourcePropertiesRequestFailedFaultType, ResourceUnknownFaultType, UnableToModifyResourcePropertyFaultType,
		ResourceUnavailableFaultType
	{
		setResourceProperties(deleteResourcePropertiesRequest.getDelete());
		return new DeleteResourcePropertiesResponse();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetResourcePropertyDocumentResponse getResourcePropertyDocument(GetResourcePropertyDocument getResourcePropertyDocumentRequest)
		throws RemoteException, ResourceUnknownFaultType, ResourceUnavailableFaultType
	{
		ArrayList<IAttributeManipulator> manipulators = _attributePackage.getManipulators();
		ArrayList<MessageElement> elements = new ArrayList<MessageElement>();
		for (IAttributeManipulator manipulator : manipulators) {
			Collection<MessageElement> values = manipulator.getAttributeValues();
			if (values != null) {
				for (MessageElement value : values)
					if (value != null)
						elements.add(value);
			}
		}
		for (Collection<MessageElement> values : _attributePackage.getUnknownAttributes(ResourceManager.getCurrentResource().dereference())
			.values())
			elements.addAll(values);

		MessageElement[] elementsArray = new MessageElement[elements.size()];
		elements.toArray(elementsArray);

		return new GetResourcePropertyDocumentResponse(elementsArray);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public InsertResourcePropertiesResponse insertResourceProperties(InsertResourceProperties insertResourcePropertiesRequest)
		throws RemoteException, InsertResourcePropertiesRequestFailedFaultType, InvalidResourcePropertyQNameFaultType,
		InvalidModificationFaultType, ResourceUnknownFaultType, UnableToModifyResourcePropertyFaultType, ResourceUnavailableFaultType
	{
		setResourceProperties(insertResourcePropertiesRequest.getInsert());
		return new InsertResourcePropertiesResponse();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public QueryResourcePropertiesResponse queryResourceProperties(QueryResourceProperties queryResourcePropertiesRequest)
		throws RemoteException, UnknownQueryExpressionDialectFaultType, QueryEvaluationErrorFaultType, InvalidQueryExpressionFaultType,
		InvalidResourcePropertyQNameFaultType, ResourceUnknownFaultType, ResourceUnavailableFaultType
	{
		QueryExpressionType qet = queryResourcePropertiesRequest.getQueryExpression();

		// First, make sure we understand the query expression dialect
		URI dialect = qet.getDialect();
		if (!dialect.toString().equals(WSRFConstants.XPATH_QUERY_EXPRESSION_DIALECT_STRING))
			throw FaultManipulator.fillInFault(new UnknownQueryExpressionDialectFaultType(null, null, null, null, null, null));

		// Form document
		try {
			MessageElement doc = new MessageElement(new QName("http://tempuri.org", "ResourcePropertiesDocument"));
			for (MessageElement elem : getResourcePropertyDocument(new GetResourcePropertyDocument()).get_any()) {
				doc.addChild(elem);
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(baos);
			ObjectSerializer.serialize(writer, doc, doc.getQName());
			writer.flush();
			InputSource inputSource = new InputSource(new ByteArrayInputStream(baos.toByteArray()));

			String str = (String) XPathFactory.newInstance().newXPath().evaluate("/*/Ticker", inputSource, XPathConstants.STRING);
			System.err.println("The String is \"" + str + "\".");
			StreamUtils.close(writer);
			return null;
		} catch (SOAPException se) {
			_logger.error("Unable to query resource properties.", se);
			throw FaultManipulator.fillInFault(new QueryEvaluationErrorFaultType(null, null, null, null, null, null));
		} catch (IOException ioe) {
			_logger.error("Unable to query resource properties.", ioe);
			throw FaultManipulator.fillInFault(new QueryEvaluationErrorFaultType(null, null, null, null, null, null));
		} catch (XPathExpressionException xpee) {
			_logger.error("Unable to query resource properties.", xpee);
			throw FaultManipulator.fillInFault(new InvalidQueryExpressionFaultType(null, null, null, null, null, null));
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public SetResourcePropertiesResponse setResourceProperties(SetResourceProperties setResourcePropertiesRequest) throws RemoteException,
		InvalidResourcePropertyQNameFaultType, InvalidModificationFaultType, SetResourcePropertyRequestFailedFaultType,
		ResourceUnknownFaultType, UnableToModifyResourcePropertyFaultType, ResourceUnavailableFaultType
	{
		setResourceProperties(setResourcePropertiesRequest.getInsert());
		setResourceProperties(setResourcePropertiesRequest.getUpdate());
		setResourceProperties(setResourcePropertiesRequest.getDelete());

		return new SetResourcePropertiesResponse();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public UpdateResourcePropertiesResponse updateResourceProperties(UpdateResourceProperties updateResourcePropertiesRequest)
		throws RemoteException, InvalidResourcePropertyQNameFaultType, InvalidModificationFaultType, ResourceUnknownFaultType,
		UnableToModifyResourcePropertyFaultType, UpdateResourcePropertiesRequestFailedFaultType, ResourceUnavailableFaultType
	{
		setResourceProperties(updateResourcePropertiesRequest.getUpdate());
		return new UpdateResourcePropertiesResponse();
	}

	private void setResourceProperties(DeleteType del) throws RemoteException
	{
		if (del != null) {
			QName rp = del.getResourceProperty();

			IAttributeManipulator manipulator = _attributePackage.getManipulator(rp);

			if (manipulator == null) {
				_attributePackage.deleteUnknownAttributes(ResourceManager.getCurrentResource().dereference(), rp);
			} else
				manipulator.setAttributeValues(new ArrayList<MessageElement>());
		}
	}

	private void setResourceProperties(InsertType ins) throws RemoteException
	{
		HashMap<QName, Collection<MessageElement>> map = new HashMap<QName, Collection<MessageElement>>();

		if (ins != null) {
			MessageElement[] any = ins.get_any();
			for (MessageElement elem : any) {
				QName name = elem.getQName();
				if (_logger.isTraceEnabled())
					_logger.debug("setting resource property: " + name.toString());
				Collection<MessageElement> list = map.get(name);
				if (list == null)
					map.put(name, list = new ArrayList<MessageElement>());
				list.add(elem);
			}

			Map<QName, Collection<MessageElement>> unknowns = null;

			for (QName attrName : map.keySet()) {
				Collection<MessageElement> newAttrs = map.get(attrName);
				IAttributeManipulator manip = getAttributePackage().getManipulator(attrName);
				if (manip == null) {
					if (unknowns == null)
						unknowns = _attributePackage.getUnknownAttributes(ResourceManager.getCurrentResource().dereference());

					Collection<MessageElement> oldValues = unknowns.get(attrName);
					if (oldValues == null)
						oldValues = new ArrayList<MessageElement>();
					oldValues.addAll(newAttrs);
					_attributePackage.setUnknownAttributes(ResourceManager.getCurrentResource().dereference(), oldValues);
				} else {
					Collection<MessageElement> oldValues = new ArrayList<MessageElement>(manip.getAttributeValues());
					oldValues.addAll(newAttrs);
					manip.setAttributeValues(oldValues);
				}
			}
		}
	}

	private void setResourceProperties(UpdateType update) throws RemoteException
	{
		HashMap<QName, Collection<MessageElement>> map = new HashMap<QName, Collection<MessageElement>>();

		if (update != null) {
			MessageElement[] any = update.get_any();
			for (MessageElement elem : any) {
				QName name = elem.getQName();
				Collection<MessageElement> list = map.get(name);
				if (list == null)
					map.put(name, list = new ArrayList<MessageElement>());
				list.add(elem);
			}

			for (QName attrName : map.keySet()) {
				Collection<MessageElement> newAttrs = map.get(attrName);

				IAttributeManipulator manip = getAttributePackage().getManipulator(attrName);
				if (manip == null) {
					_attributePackage.setUnknownAttributes(ResourceManager.getCurrentResource().dereference(), newAttrs);
				} else
					manip.setAttributeValues(newAttrs);
			}
		}
	}

	protected Duration getInitialCacheCoherenceWindow()
	{
		return new Duration(0L);
	}

	public Duration getCacheCoherenceWindow() throws ResourceException, RemoteException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		Duration gDur = (Duration) resource.getProperty(IResource.CACHE_COHERENCE_WINDOW_PROPERTY);
		if (gDur == null)
			gDur = getInitialCacheCoherenceWindow();
		return gDur;
	}

	public void setCacheCoherenceWindow(Duration duration) throws ResourceException, RemoteException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		resource.setProperty(IResource.CACHE_COHERENCE_WINDOW_PROPERTY, duration);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public AddMatchingParameterResponseType addMatchingParameter(MatchingParameter[] addMatchingParameterRequest) throws RemoteException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		resource.addMatchingParameter(addMatchingParameterRequest);
		return new AddMatchingParameterResponseType();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RemoveMatchingParameterResponseType removeMatchingParameter(MatchingParameter[] removeMatchingParameterRequest)
		throws RemoteException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		resource.removeMatchingParameter(removeMatchingParameterRequest);
		return new RemoveMatchingParameterResponseType();
	}

	/**
	 * See the comment in WorkingContextHandler.handleRequest().
	 * 
	 * The WorkingContext contains an EPR which does not contain the correct metadata, because the client did not send the metadata. We can
	 * use this incomplete EPR for most purposes, but we do not want to give the incomplete EPR to the resolver for long-term storage.
	 * 
	 * This function puts the implemented port types in the metadata in the EPR in the working context. This should be called by the sync()
	 * method, which gives the EPR to the resolver for long-term storage.
	 */
	protected void fixMetadataInWorkingContext()
	{
		EndpointReferenceType myEPR = null;
		String resourceInterfaces = null;
		try {
			WorkingContext ctxt = (WorkingContext) WorkingContext.getCurrentWorkingContext();
			myEPR = (EndpointReferenceType) ctxt.getProperty(WorkingContext.EPR_PROPERTY_NAME);
			ResourceKey rKey = ResourceManager.getCurrentResource();
			resourceInterfaces = PortType.portTypeFactory().translate(getImplementedPortTypes(rKey));
		} catch (Throwable cause) {
			_logger.warn("Failed to get current EPR and resource.", cause);
			return;
		}
		MetadataType metadata = myEPR.getMetadata();
		MessageElement[] any = metadata.get_any();
		for (MessageElement element : any) {
			if (element.getQName().equals(OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME)) {
				element.setValue(resourceInterfaces);
				break;
			}
		}
	}

	/**
	 * This should be a class annotation (GeniiServiceConfiguration.resourceSyncRunner), but annotations cannot default to null?
	 */
	public ResourceSyncRunner getClassResourceSyncRunner()
	{
		return null;
	}

	private abstract class GenesisIIBaseAbstractIteratorBuilder<SourceType> extends AbstractIteratorBuilder<SourceType>
	{
		@Override
		final public IteratorInitializationType create() throws RemoteException
		{

			return create(null);
		}

		@Override
		final public IteratorInitializationType create(InMemoryIteratorWrapper imiw) throws RemoteException
		{

			IterableElementType[] batchElements = null;
			EndpointReferenceType iteratorEndpoint = null;

			int preferredBlockSize = preferredBatchSize();

			ArrayList<MessageElement> firstBlockList = new ArrayList<MessageElement>(preferredBlockSize);

			for (int lcv = 0; lcv < preferredBlockSize && iterator().hasNext(); lcv++) {
				MessageElement item = iterator().next();
				firstBlockList.add(item);
			}

			WSIteratorConstructionParameters consParms = new WSIteratorConstructionParameters(iterator(), preferredBatchSize(), imiw);

			try {
				MessageElement[] firstBlock = firstBlockList.toArray(new MessageElement[firstBlockList.size()]);

				if (firstBlock != null) {
					batchElements = new IterableElementType[firstBlock.length];
					for (int lcv = 0; lcv < firstBlock.length; lcv++)
						batchElements[lcv] = new IterableElementType(new MessageElement[] { firstBlock[lcv] }, new UnsignedLong(lcv));
				}

				Object tempObj = new Object();

				if (consParms.remainingContents(tempObj))
					iteratorEndpoint = new WSIteratorServiceImpl().CreateEPR(Elementals.unitaryArray(consParms.serializeToMessageElement()),
						Container.getServiceURL("WSIteratorPortType"));

				return new IteratorInitializationType(iteratorEndpoint, batchElements);
			} finally {
				StreamUtils.close(consParms);
			}
		}
	}

	final private class MessageElementIteratorBuilder extends GenesisIIBaseAbstractIteratorBuilder<MessageElement>
	{
		@Override
		final protected MessageElement serialize(MessageElement item)
		{
			return item;
		}
	}

	final private class JaxbIteratorBuilder extends GenesisIIBaseAbstractIteratorBuilder<Object>
	{
		private Marshaller _marshaller;
		private QName _name;

		private JaxbIteratorBuilder(Marshaller marshaller, QName name)
		{
			_marshaller = marshaller;
			_name = name;
		}

		@Override
		final protected MessageElement serialize(Object item) throws IOException
		{
			DOMResult result = new DOMResult();

			try {
				if (item instanceof JAXBElement<?> || _name == null) {
					_marshaller.marshal(item, result);
				} else {
					@SuppressWarnings({ "rawtypes", "unchecked" })
					JAXBElement<?> e = new JAXBElement(_name, item.getClass(), item);
					_marshaller.marshal(e, result);
				}

				return new MessageElement(((Document) result.getNode()).getDocumentElement());
			} catch (JAXBException e) {
				throw new IOException(String.format("Unable to JAXB serialize %s!", item), e);
			}
		}
	}

	final private class AxisSerializationIteratorBuilder extends GenesisIIBaseAbstractIteratorBuilder<Object>
	{
		private QName _name;

		private AxisSerializationIteratorBuilder(QName name)
		{
			_name = name;
		}

		@Override
		final protected MessageElement serialize(Object item)
		{
			return MessageElementSerializer.serialize(_name, item);
		}
	}

	public String getMasterType(ResourceKey rKey) throws ResourceException, ResourceUnknownFaultType
	{
		return _serviceName;

	}
}
