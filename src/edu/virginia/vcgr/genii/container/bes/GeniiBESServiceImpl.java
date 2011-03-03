package edu.virginia.vcgr.genii.container.bes;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.BasicResourceAttributesDocumentType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.FactoryResourceAttributesDocumentType;
import org.ggf.bes.factory.GetActivityDocumentResponseType;
import org.ggf.bes.factory.GetActivityDocumentsResponseType;
import org.ggf.bes.factory.GetActivityDocumentsType;
import org.ggf.bes.factory.GetActivityStatusResponseType;
import org.ggf.bes.factory.GetActivityStatusesResponseType;
import org.ggf.bes.factory.GetActivityStatusesType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;
import org.ggf.bes.factory.InvalidRequestMessageFaultType;
import org.ggf.bes.factory.NotAcceptingNewActivitiesFaultType;
import org.ggf.bes.factory.NotAuthorizedFaultType;
import org.ggf.bes.factory.TerminateActivitiesResponseType;
import org.ggf.bes.factory.TerminateActivitiesType;
import org.ggf.bes.factory.TerminateActivityResponseType;
import org.ggf.bes.factory.UnknownActivityIdentifierFaultType;
import org.ggf.bes.factory.UnsupportedFeatureFaultType;
import org.ggf.bes.management.StartAcceptingNewActivitiesResponseType;
import org.ggf.bes.management.StartAcceptingNewActivitiesType;
import org.ggf.bes.management.StopAcceptingNewActivitiesResponseType;
import org.ggf.bes.management.StopAcceptingNewActivitiesType;
import org.ggf.jsdl.JobDefinition_Type;
import org.morgan.inject.MInject;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.BESFaultManager;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.cloud.CloudAttributesHandler;
import edu.virginia.vcgr.genii.cloud.CloudDBResourceFactory;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.MatchingParameter;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityServiceImpl;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityUtils;
import edu.virginia.vcgr.genii.container.bes.forks.BESRootRNSFork;
import edu.virginia.vcgr.genii.container.bes.resource.BESDBResourceProvider;
import edu.virginia.vcgr.genii.container.bes.resource.DBBESResourceFactory;
import edu.virginia.vcgr.genii.container.bes.resource.IBESResource;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.cservices.history.InMemoryHistoryEventSink;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ForkRoot;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkBaseService;
import edu.virginia.vcgr.genii.graph.GridDependency;
import edu.virginia.vcgr.jsdl.JobDefinition;

@ForkRoot(BESRootRNSFork.class)
@ConstructionParametersType(BESConstructionParameters.class)
@GeniiServiceConfiguration(
	resourceProvider=BESDBResourceProvider.class)
@GridDependency(BESActivityServiceImpl.class)
public class GeniiBESServiceImpl extends ResourceForkBaseService implements
	GeniiBESPortType, BESConstants
{
	static private Log _logger = LogFactory.getLog(GeniiBESServiceImpl.class);
	
	@MInject(lazy = true)
	private IBESResource _resource;
	
	private void cleanupBadActivities()
	{
		Collection<BESActivity> allActivities = BES.getAllActivities();
		boolean isGood;
		
		for (BESActivity activity : allActivities)
		{
			isGood = false;
			try
			{
				isGood = activity.isGood();
			}
			catch (Throwable cause)
			{
				_logger.warn(String.format(
					"Unable to determine is activity %s is good -- deleting it!", 
					activity.getActivityID()), cause);
			}
			
			boolean success = false;
			try
			{
				WorkingContext.temporarilyAssumeNewIdentity(
					activity.getActivityEPR());
				success = true;
				
				if (isGood)
				{
					isGood = true;
					try
					{
						Calendar createTime = 
							ResourceManager.getCurrentResource().dereference().createTime();
						if (createTime == null)
							isGood = false;
						else
							isGood = (System.currentTimeMillis() - createTime.getTimeInMillis()) <
								(1000L * 60 * 60 * 24 * 28);
					}
					catch (Throwable cause)
					{
						_logger.warn(
							"Couldn't get create time for activity.", cause);
					}
				}
				
				if (!isGood)
				{
					_logger.info(String.format(
						"Cleaning up a BES activity that we have determined is bad:  %s.", 
						activity.getActivityID()));
					
					new BESActivityServiceImpl().destroy(new Destroy());
				}
			}
			catch (Throwable cause)
			{
				_logger.error(String.format(
					"Unable to cleanup \"bad\" activity %s.", 
					activity.getActivityID()), cause);
			}
			finally
			{
				if (success)
				{
					try
					{
						WorkingContext.releaseAssumedIdentity();
					}
					catch (Throwable cause)
					{
						_logger.error("Unable to release assumed context.",
							cause);
					}
				}
			}
		}
	}
	
	@Override
	public boolean startup()
	{
		return super.startup();
	}
	
	@Override
	public void postStartup()
	{
		try
		{
			/* In order to make out calls, we have to have a working context
			 * so we go ahead and create an empty one.
			 */
			WorkingContext.setCurrentWorkingContext(new WorkingContext());
			
			/* Now we get the database connection pool configured 
			 * with this service */
			DatabaseConnectionPool connectionPool =(
				(DBBESResourceFactory)ResourceManager.getServiceResource(_serviceName
					).getProvider().getFactory()).getConnectionPool();
			
			
			//Set cloud connection DB pool
			CloudMonitor.setConnectionPool(
					(new CloudDBResourceFactory(connectionPool).getConnectionPool()));
			
			BES.loadAllInstances(connectionPool);
			
			//Load cloud activities table
			CloudMonitor.loadCloudActivities();
		}
		catch (Exception e)
		{
			_logger.error("Unable to start resource info managers.", e);
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}
		
		cleanupBadActivities();
	}
	
	protected void setAttributeHandlers()
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();

		new CloudAttributesHandler(getAttributePackage());
		new BESAttributesHandler(getAttributePackage());
	}
	
	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR,
		ConstructionParameters cParams, HashMap<QName, Object> constructionParameters,
		Collection<MessageElement> resolverCreationParameters)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, cParams, constructionParameters,
			resolverCreationParameters);
	}

	static private EndpointReferenceType _localActivityServiceEPR = null;
	
	public GeniiBESServiceImpl() throws RemoteException
	{
		super("GeniiBESPortType");
		
		addImplementedPortType(BES_FACTORY_PORT_TYPE);
		addImplementedPortType(BES_MANAGEMENT_PORT_TYPE);
		addImplementedPortType(GENII_BES_PORT_TYPE);
	}
	
	@Override
	public PortType getFinalWSResourceInterface()
	{
		return GENII_BES_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateActivityResponseType createActivity(
			CreateActivityType parameters) throws RemoteException,
			NotAcceptingNewActivitiesFaultType, InvalidRequestMessageFaultType,
			UnsupportedFeatureFaultType, NotAuthorizedFaultType
	{
		InMemoryHistoryEventSink historySink = new InMemoryHistoryEventSink();
		HistoryContext history = HistoryContextFactory.createContext(
			HistoryEventCategory.CreatingActivity, historySink);
		
		ActivityDocumentType adt = parameters.getActivityDocument();
		JobDefinition_Type jdt = adt.getJobDefinition();
		
		history.info("BES analyzing JSDL.");
		
		try
		{
			JobDefinition jaxbType = JSDLUtils.convert(jdt);
			if (jaxbType.parameterSweeps().size() > 0)
			{
				history.createErrorWriter(
					"Parameter Sweep Unsupported.").format(
						"This type of BES container does not " +
						"support Parameter Sweeps.").close();
				
				throw new UnsupportedFeatureFaultType(new String[] {
						"This BES container does not support JSDL parameter sweeps."
					}, null);
			}
		}
		catch (JAXBException je)
		{
			history.warn(je, "JAXB Error parsing JSDL");
			_logger.warn("Unable to parse using JAXB.", je);
			
			// Ignore and hope that it still works out.
		}
		
		MessageElement subscribe = null;
		
		MessageElement []any = adt.get_any();
		if (any != null)
		{
			for (MessageElement a : any)
			{
				QName name = a.getQName();
				if (name.equals(
					BESConstants.GENII_BES_NOTIFICATION_SUBSCRIBE_ELEMENT_QNAME))
				{
					subscribe = a;
				}
			}
		}
		
		if (!_resource.isAcceptingNewActivities())
		{
			history.warn("BES No Longer Accepting Jobs");
			_logger.info("BES container not currently accepting new activities.");
			
			throw new NotAcceptingNewActivitiesFaultType(null);
		}
	
		if (_localActivityServiceEPR == null) 
		{
			// only need to make this epr from scratch once (which involves
			// a get-attr rpc to the service to get its full epr)
			_localActivityServiceEPR =
				EPRUtils.makeEPR(Container.getServiceURL("BESActivityPortType"));
		}
		
		_logger.info(String.format(
			"BES with resource key \"%s\" is creating an activity.",
			_resource.getKey()));
		
		/* ASG August 28,2008, replaced RPC with direct call to CreateEPR */
		history.info("BES Creating Activity Instance");
		
		EndpointReferenceType entryReference = 
			new BESActivityServiceImpl().CreateEPR(BESActivityUtils.createCreationProperties(
				jdt, _resource.getKey(),
				(BESConstructionParameters)_resource.constructionParameters(getClass()),
				subscribe),
				Container.getServiceURL("BESActivityPortType"));

		return new CreateActivityResponseType(entryReference, adt,
			historySink.eventMessages());

	}
	
	@Override
	@RWXMapping(RWXCategory.READ)
	public GetActivityDocumentsResponseType getActivityDocuments(
			GetActivityDocumentsType parameters) throws RemoteException
	{
		Collection<GetActivityDocumentResponseType> response =
			new LinkedList<GetActivityDocumentResponseType>();
		
		for (EndpointReferenceType target : parameters.getActivityIdentifier())
		{
			try
			{
				BESActivity activity = _resource.getActivity(target);
				response.add(new GetActivityDocumentResponseType(
					target, activity.getJobDefinition(),
					null, null));
			}
			catch (Throwable cause)
			{
				response.add(new GetActivityDocumentResponseType(
					target, null, BESFaultManager.constructFault(cause), 
					null));
			}
		}
		
		return new GetActivityDocumentsResponseType(
			response.toArray(new GetActivityDocumentResponseType[0]), null);
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetActivityStatusesResponseType getActivityStatuses(
			GetActivityStatusesType parameters) throws RemoteException,
			UnknownActivityIdentifierFaultType
	{
		Collection<GetActivityStatusResponseType> response =
			new LinkedList<GetActivityStatusResponseType>();
		
		for (EndpointReferenceType target : parameters.getActivityIdentifier())
		{
			try
			{
				BESActivity activity = _resource.getActivity(target);
				activity.verifyOwner();
				Collection<Throwable> faults = activity.getFaults();
				response.add(new GetActivityStatusResponseType(
					target, activity.getState().toActivityStatusType(),
					((faults == null) || (faults.size() == 0)) ? null :
					BESFaultManager.constructFault(
						faults.toArray(new Throwable[faults.size()])),
					null));
			}
			catch (Throwable cause)
			{
				response.add(new GetActivityStatusResponseType(
					target, null, BESFaultManager.constructFault(cause), 
					null));
			}
		}
		
		return new GetActivityStatusesResponseType(
			response.toArray(new GetActivityStatusResponseType[0]), null);
	}

	static private void addAndrewsClassAttributes(String resourceName, 
			Collection<MessageElement> any)
	{
		final String classNamespace = "http://cs.virginia.edu/classes/andrews-class";
		
		final QName reliability = new QName(classNamespace, "reliability");
		final QName cost = new QName(classNamespace, "cost");
		final QName downtime = new QName(classNamespace, "downtime");
		
		Random generator = new Random(resourceName.hashCode());
		
		int n = generator.nextInt(3);
		float r = 0.80f;
		r += 0.05f * n;
		
		int c = 5 + 5 * n;
		String dt = "15-20 4 * * *";
		
		any.add(new MessageElement(reliability, r));
		any.add(new MessageElement(cost, c));
		any.add(new MessageElement(downtime, dt));
	}
	
	private void addMatchingParameters(Collection<MessageElement> any) 
		throws ResourceUnknownFaultType, ResourceException
	{
		Collection<MatchingParameter> matchingParams = 
			_resource.getMatchingParameters();
		for (MatchingParameter param : matchingParams)
		{
			MessageElement me = new MessageElement(
				GenesisIIBaseRP.MATCHING_PARAMTER_ATTR_QNAME,
				param);
			any.add(me);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.READ)
	public GetFactoryAttributesDocumentResponseType getFactoryAttributesDocument(
		GetFactoryAttributesDocumentType parameters) throws RemoteException
	{
		Collection<MessageElement> any = new ArrayList<MessageElement>(4);
		String resourceName = Hostname.getLocalHostname().toString();
		
		URI []namingProfiles = null;
		URI []besExtensions = null;
		URI localResourceManagerType = null;
		
		try
		{
			namingProfiles = new URI[] {
					new URI(BESConstants.NAMING_PROFILE_WS_ADDRESSING),
					new URI(BESConstants.NAMING_PROFILE_WS_NAMING)
				};
			besExtensions = new URI[0];
			
			BESConstructionParameters consParms = 
				(BESConstructionParameters)_resource.constructionParameters(
					getClass());
			if (consParms != null)
			{
				NativeQueueConfiguration nqconf = 
					consParms.getNativeQueueConfiguration();
				if (nqconf != null)
				{
					NativeQueue nq = nqconf.nativeQueue();
					if (nq != null)
					{
						localResourceManagerType = 
							nq.resourceManagerType().toApacheAxisURI();
					}
				}
			}
			
			if (localResourceManagerType == null)
				localResourceManagerType = new URI(
					BESConstants.LOCAL_RESOURCE_MANAGER_TYPE_SIMPLE);
		}
		catch (Throwable cause)
		{
			// This really shouldn't happen
			_logger.fatal("Unexpected exception in BES.", cause);
		}
		
		addAndrewsClassAttributes(resourceName, any);
		addMatchingParameters(any);
		
		try
		{
			MessageElement []resourceAny = null;
			MessageElement wallclockAttr =
				BESAttributesHandler.getWallclockTimeLimitAttr();
			if (wallclockAttr != null)
				resourceAny = new MessageElement[] { wallclockAttr };
			
			return new GetFactoryAttributesDocumentResponseType(
				new FactoryResourceAttributesDocumentType(
					new BasicResourceAttributesDocumentType(
						resourceName,
						BESAttributesHandler.getOperatingSystem(),
						BESAttributesHandler.getCPUArchitecture(),
						new Double((double)BESAttributesHandler.getCPUCount()),
						new Double(
							(double)BESAttributesHandler.getCPUSpeed()),
						new Double((double)BESAttributesHandler.getPhysicalMemory()),
						new Double((double)BESAttributesHandler.getVirtualMemory()),
						resourceAny),
					BESAttributesHandler.getIsAcceptingNewActivities(),
					BESAttributesHandler.getName(),
					BESAttributesHandler.getDescription(),
					BESAttributesHandler.getTotalNumberOfActivities(),
					BESAttributesHandler.getActivityReferences(),
					0, null, namingProfiles, besExtensions, 
					localResourceManagerType, 
					any.toArray(new MessageElement[any.size()])), null);
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unexpected BES exception.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public StartAcceptingNewActivitiesResponseType startAcceptingNewActivities(
			StartAcceptingNewActivitiesType parameters) throws RemoteException
	{
		_resource.setProperty(IBESResource.STORED_ACCEPTING_NEW_ACTIVITIES, 
			Boolean.TRUE);
		return new StartAcceptingNewActivitiesResponseType(null);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public StopAcceptingNewActivitiesResponseType stopAcceptingNewActivities(
			StopAcceptingNewActivitiesType parameters) throws RemoteException
	{
		_resource.setProperty(IBESResource.STORED_ACCEPTING_NEW_ACTIVITIES, 
			Boolean.FALSE);
		return new StopAcceptingNewActivitiesResponseType(null);
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public TerminateActivitiesResponseType terminateActivities(
			TerminateActivitiesType parameters) throws RemoteException,
			UnknownActivityIdentifierFaultType
	{
		Collection<TerminateActivityResponseType> responses =
			new LinkedList<TerminateActivityResponseType>();
		
		for (EndpointReferenceType aepr : parameters.getAcitivityIdentifier())
		{
			responses.add(terminateActivity(aepr));
		}
		
		return new TerminateActivitiesResponseType(
			responses.toArray(new TerminateActivityResponseType[0]), null);
	}
	
	static public TerminateActivityResponseType terminateActivity(
		EndpointReferenceType activity) throws RemoteException
	{
		try
		{
			GeniiCommon client = ClientUtils.createProxy(
				GeniiCommon.class, activity);
			client.destroy(new Destroy());
			return new TerminateActivityResponseType(activity, true, 
				null, null);
		}
		catch (Throwable cause)
		{
			return new TerminateActivityResponseType(activity, false, 
				BESFaultManager.constructFault(cause), null);
		}
	}
}
