package edu.virginia.vcgr.genii.container.bes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedLong;
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
import org.ggf.jsdl.JobIdentification_Type;
import org.ggf.jsdl.JobMultiDefinition_Type;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.Move;
import org.ggf.rns.MoveResponse;
import org.ggf.rns.Query;
import org.ggf.rns.QueryResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.bes.BESFaultManager;
import edu.virginia.vcgr.genii.client.bes.GeniiBESConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.utils.creation.CreationProperties;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.UserDataType;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityServiceImpl;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityUtils;
import edu.virginia.vcgr.genii.container.bes.activity.NoSuchActivityFault;
import edu.virginia.vcgr.genii.container.bes.resource.DBBESResourceFactory;
import edu.virginia.vcgr.genii.container.bes.resource.IBESResource;
import edu.virginia.vcgr.genii.container.byteio.RByteIOResource;
import edu.virginia.vcgr.genii.container.byteio.StreamableByteIOServiceImpl;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.db.DatabaseConnectionPool;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.sysinfo.SystemInfoUtils;

public class GeniiBESServiceImpl extends GenesisIIBase implements
	GeniiBESPortType, BESConstants
{
	static private Log _logger = LogFactory.getLog(GeniiBESServiceImpl.class);
	
	static private final long _DEFAULT_TIME_TO_LIVE = 1000L * 60 * 60;
	static private QName _FILENAME_QNAME =
        new QName(GenesisIIConstants.GENESISII_NS, "create-file-filename");
    static private QName _FILEPATH_QNAME =
        new QName(GenesisIIConstants.GENESISII_NS, "data-filepath");

	@Override
	public boolean startup()
	{
		boolean ret = super.startup();
		
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
			
			BES.loadAllInstances(connectionPool);
		}
		catch (Exception e)
		{
			_logger.error("Unable to start resource info managers.", e);
		}
		finally
		{
			WorkingContext.setCurrentWorkingContext(null);
		}

		return ret;
	}
	
	protected void setAttributeHandlers()
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();

		new BESAttributesHandler(getAttributePackage());
	}
	
	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR,
			HashMap<QName, Object> constructionParameters,
			Collection<MessageElement> resolverCreationParameters)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, constructionParameters,
			resolverCreationParameters);
		
		Properties props = (Properties)constructionParameters.get(
			CreationProperties.CREATION_PROPERTIES_QNAME);
		if (props != null)
		{
			String queueProvider = props.getProperty(
				GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY);
			if (queueProvider != null)
			{
				IResource resource = key.dereference();
				resource.setProperty(
					GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY, 
					props);
			}
		}
	}

	static private EndpointReferenceType _localActivityServiceEPR = null;
	
	public GeniiBESServiceImpl() throws RemoteException
	{
		super("GeniiBESPortType");
		
		addImplementedPortType(BES_FACTORY_PORT_TYPE);
		addImplementedPortType(BES_MANAGEMENT_PORT_TYPE);
		addImplementedPortType(GENII_BES_PORT_TYPE);
		addImplementedPortType(RNSConstants.RNS_PORT_TYPE);
	}
	
	@Override
	public PortType getFinalWSResourceInterface()
	{
		return GENII_BES_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public AddResponse add(Add addRequest) throws RemoteException,
			RNSEntryExistsFaultType, RNSFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType
	{
		throw new RemoteException("Method \"add\" is not implemented.");
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateActivityResponseType createActivity(
			CreateActivityType parameters) throws RemoteException,
			NotAcceptingNewActivitiesFaultType, InvalidRequestMessageFaultType,
			UnsupportedFeatureFaultType, NotAuthorizedFaultType
	{
		ActivityDocumentType adt = parameters.getActivityDocument();
		JobDefinition_Type jdt = adt.getJobDefinition();
		ResourceKey key = ResourceManager.getCurrentResource();
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
		
		IBESResource resource = (IBESResource)key.dereference();
		 
		if (!resource.isAcceptingNewActivities())
			throw new NotAcceptingNewActivitiesFaultType(null);
	

		if (_localActivityServiceEPR == null) 
		{
			// only need to make this epr from scratch once (which involves
			// a get-attr rpc to the service to get its full epr)
			_localActivityServiceEPR =
				EPRUtils.makeEPR(Container.getServiceURL("BESActivityPortType"));
		}
		
		_logger.debug(String.format(
			"BES with resource key \"%s\" is creating an activity.",
			key.getResourceKey()));
		
		/* ASG August 28,2008, replaced RPC with direct call to CreateEPR */
		EndpointReferenceType entryReference = 
			new BESActivityServiceImpl().CreateEPR(BESActivityUtils.createCreationProperties(
							jdt, (String)resource.getKey(), 
							(Properties)resource.getProperty(
								GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY),
							subscribe),
					Container.getServiceURL("BESActivityPortType"));

/*		
		BESActivityPortType activity = ClientUtils.createProxy(
		BESActivityPortType.class,
		_localActivityServiceEPR);
		
		VcgrCreateResponse resp = activity.vcgrCreate(
			new VcgrCreate(
				BESActivityUtils.createCreationProperties(
					jdt, (String)resource.getKey(), 
					(Properties)resource.getProperty(
						GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY))));
		System.err.println("Time to create activity " + (System.currentTimeMillis() - start));
		return new CreateActivityResponseType(resp.getEndpoint(), adt, null);
	*/	

		return new CreateActivityResponseType(entryReference, adt, null);

	}
	
	static private UserDataType createUserData(String filename, String filepath)
    {
        return new UserDataType(new MessageElement[] {
            new MessageElement(
                _FILENAME_QNAME, filename),
            new MessageElement(
                _FILEPATH_QNAME, filepath)
        });
    }
	
	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
			throws RemoteException, RNSEntryExistsFaultType, RNSFaultType,
			ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType
	{
		MessageElement []parameters = null;

        File filePath;

        try
        {
            File userDir = ConfigurationManager.getCurrentConfiguration().getUserDirectory();
            GuaranteedDirectory sbyteiodir = new GuaranteedDirectory(userDir, "sbyteio");
            filePath = File.createTempFile("sbyteio", ".dat", sbyteiodir);
        }
        catch (IOException ioe)
        {
            throw new ResourceException(ioe.getLocalizedMessage(), ioe);
        }

        Subscribe subscribeRequest = new Subscribe(new Token(
            WellknownTopics.SBYTEIO_INSTANCE_DYING),
            new UnsignedLong(_DEFAULT_TIME_TO_LIVE),
            (EndpointReferenceType)WorkingContext.getCurrentWorkingContext(
                ).getProperty(WorkingContext.EPR_PROPERTY_NAME),
            createUserData(createFileRequest.getFilename(),
                filePath.getAbsolutePath()));


        parameters = new MessageElement [] {
            new MessageElement(RByteIOResource.FILE_PATH_PROPERTY,
                filePath.getAbsolutePath()),
            new MessageElement(
                ByteIOConstants.SBYTEIO_SUBSCRIBE_CONSTRUCTION_PARAMETER,
                subscribeRequest),
            new MessageElement(
                ByteIOConstants.MUST_DESTROY_PROPERTY,
                Boolean.FALSE),
            new MessageElement(
            	ByteIOConstants.SBYTEIO_DESTROY_ON_CLOSE_FLAG,
            	Boolean.TRUE),
            ClientConstructionParameters.createTimeToLiveProperty(
                _DEFAULT_TIME_TO_LIVE)
        };

		/* ASG August 28,2008, replaced RPC with direct call to CreateEPR */
		EndpointReferenceType entryReference = 
			new StreamableByteIOServiceImpl().CreateEPR(parameters,
					Container.getServiceURL("StreamableByteIOPortType"));
        return new CreateFileResponse(entryReference);

        /*
        StreamableByteIOPortType sbyteio = ClientUtils.createProxy(
        	StreamableByteIOPortType.class, EPRUtils.makeEPR(
        		Container.getServiceURL("StreamableByteIOPortType")));
        VcgrCreateResponse resp = sbyteio.vcgrCreate(new VcgrCreate(parameters));
        return new CreateFileResponse(resp.getEndpoint());
         */
    }

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetActivityDocumentsResponseType getActivityDocuments(
			GetActivityDocumentsType parameters) throws RemoteException
	{
		Collection<GetActivityDocumentResponseType> response =
			new LinkedList<GetActivityDocumentResponseType>();
		
		IBESResource resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		
		for (EndpointReferenceType target : parameters.getActivityIdentifier())
		{
			try
			{
				BESActivity activity = resource.getActivity(target);
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
		
		IBESResource resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		
		for (EndpointReferenceType target : parameters.getActivityIdentifier())
		{
			try
			{
				BESActivity activity = resource.getActivity(target);
				activity.verifyOwner();
				response.add(new GetActivityStatusResponseType(
					target, activity.getState().toActivityStatusType(),
					BESFaultManager.constructFault(
						activity.getFaults().toArray(new Throwable[0])),
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
			localResourceManagerType = new URI(
				BESConstants.LOCAL_RESOURCE_MANAGER_TYPE_SIMPLE);
		}
		catch (Throwable cause)
		{
			// This really shouldn't happen
			_logger.fatal("Unexpected exception in BES.", cause);
		}
		
		addAndrewsClassAttributes(resourceName, any);
		
		try
		{
			return new GetFactoryAttributesDocumentResponseType(
				new FactoryResourceAttributesDocumentType(
					new BasicResourceAttributesDocumentType(
						resourceName,
						BESAttributesHandler.getOperatingSystem(),
						BESAttributesHandler.getCPUArchitecture(),
						new Double((double)BESAttributesHandler.getCPUCount()),
						new Double(
							(double)SystemInfoUtils.getIndividualCPUSpeed()),
						new Double((double)SystemInfoUtils.getPhysicalMemory()),
						new Double((double)SystemInfoUtils.getVirtualMemory()),
						null),
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
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSEntryNotDirectoryFaultType
	{
		Collection<EntryType> response =
			new LinkedList<EntryType>();
		
		IBESResource resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		
		try
		{
			String query = listRequest.getEntryName();
			for (BESActivity activity : resource.getContainedActivities())
			{
				String name = activity.getJobName();
				if (query == null || query.equals(name))
				{
					try
					{
						response.add(new EntryType(
							name, null, activity.getActivityEPR()));
					}
					catch (NoSuchActivityFault nsaf)
					{
						_logger.debug("We lost an activity between the " +
							"time we looked it up and the time we got " +
							"it's EPR.", nsaf);
					}
				}
			}
			
			return new ListResponse(response.toArray(new EntryType[0]));
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unexpected BES exception.", sqe);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		throw new RemoteException("Method \"move\" is not implemented.");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) throws RemoteException,
			ResourceUnknownFaultType
	{
		try
		{
            String topic = notify.getTopic().toString();
            if (topic.equals(WellknownTopics.SBYTEIO_INSTANCE_DYING))
            {
                UserDataType userData = notify.getUserData();
                if (userData == null || (userData.get_any() == null) )
                    throw new RemoteException(
                        "Missing required user data for notification");
                MessageElement []data = userData.get_any();
                if (data.length != 2)
                    throw new RemoteException(
                        "Missing required user data for notification");
                String name = null;
                String filepath = null;

                for (MessageElement elem : data)
                {
                    QName elemName = elem.getQName();
                    if (elemName.equals(_FILENAME_QNAME))
                    {
                        name = elem.getValue();
                    } else if (elemName.equals(_FILEPATH_QNAME))
                    {
                        filepath = elem.getValue();
                    } else
                    {
                        throw new RemoteException(
                            "Unknown user data found in notification.");
                    }
                }

                if (name == null)
                    throw new ResourceException(
                        "Couldn't locate name parameter in UserData for notification.");
                if (filepath == null)
                    throw new ResourceException(
                        "Couldn't locate filepath parameter in UserData " +
                        "for notification.");

                if (!name.endsWith(".txt"))
                    name += ".txt";

                submitJob(name, filepath);
            }
        }
        catch (Throwable t)
        {
            _logger.warn(t.getLocalizedMessage(), t);
        }
	}
	
	private void submitJob(String jobName, String filepath)
		throws IOException
	{
		File file = new File(filepath);
		
		try
		{
			if (!submitJobTrySingle(jobName, file))
				submitJobTryMulti(jobName, file);
		}
		finally
		{
			file.delete();
		}
	}
	
	private boolean submitJobTrySingle(String jobName, File file)
		throws IOException
	{
		JobDefinition_Type jobDef = null;
		
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(file);
			jobDef =
				(JobDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(fin), JobDefinition_Type.class);
			
			if (jobDef == null)
				return false;

			if (jobDef.getJobDescription() == null)
				return false;
			
			if (jobName != null)
			{
				JobIdentification_Type ident =
					jobDef.getJobDescription().getJobIdentification();
				if (ident != null)
				{
					ident.setJobName(jobName);
				} else
				{
					jobDef.getJobDescription().setJobIdentification(
						new JobIdentification_Type(jobName, null, null,
						null, null));
				}
			}
			
			createActivity(new CreateActivityType(new ActivityDocumentType(
				jobDef, null), null));
			return true;
		}
		catch (IOException ioe)
		{
			throw ioe;
		}
		catch (Throwable cause)
		{
			return false;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	private boolean submitJobTryMulti(String jobName, File file)
		throws IOException
	{
		JobDefinition_Type []jobDefs = null;
		
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(file);
			jobDefs =
				((JobMultiDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(fin), JobMultiDefinition_Type.class)).getJobDefinition();
			
			if (jobDefs == null)
				return false;
			
			if (jobDefs.length == 0)
				return true;
			 
			if (jobName != null)
			{
				for (JobDefinition_Type jobDef : jobDefs)
				{
					JobIdentification_Type ident =
						jobDef.getJobDescription().getJobIdentification();
					if (ident != null)
					{
						ident.setJobName(jobName);
					} else
					{
						jobDef.getJobDescription().setJobIdentification(
							new JobIdentification_Type(jobName, null, null,
							null, null));
					}
				}
			}
			
			for (JobDefinition_Type jobDef : jobDefs)
			{
				createActivity(new CreateActivityType(new ActivityDocumentType(
					jobDef, null), null));
			}
			
			return true;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType
	{
		throw new RemoteException("Method \"query\" is not implemented.");
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public String[] remove(Remove removeRequest) throws RemoteException,
			RNSFaultType, ResourceUnknownFaultType,
			RNSDirectoryNotEmptyFaultType
	{
		Collection<String> response =
			new LinkedList<String>();
		
		IBESResource resource = 
			(IBESResource)ResourceManager.getCurrentResource().dereference();
		
		try
		{
			String query = removeRequest.getEntryName();
			for (BESActivity activity : resource.getContainedActivities())
			{
				String name = activity.getJobName();
				if (query == null || query.equals(name))
				{
					TerminateActivitiesResponseType tat;
					try
					{
						tat = terminateActivities(new TerminateActivitiesType(
							new EndpointReferenceType[] { 
								activity.getActivityEPR() }, null));
						if (tat.getResponse(0).getFault() == null)
							response.add(name);
						else
							_logger.error("Unable to remove activity \"" + 
								name + "\":  " + tat.getResponse(0).getFault());
					}
					catch (NoSuchActivityFault nsaf)
					{
						_logger.debug("We lost an activity between the time " +
							"we looked it up and when we asked for it's EPR.", 
							nsaf);
					}
				}
			}
			
			return response.toArray(new String[0]);
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
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.setProperty(IBESResource.STORED_ACCEPTING_NEW_ACTIVITIES, 
			Boolean.TRUE);
		return new StartAcceptingNewActivitiesResponseType(null);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public StopAcceptingNewActivitiesResponseType stopAcceptingNewActivities(
			StopAcceptingNewActivitiesType parameters) throws RemoteException
	{
		IResource resource = 
			ResourceManager.getCurrentResource().dereference();
		resource.setProperty(IBESResource.STORED_ACCEPTING_NEW_ACTIVITIES, 
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
			try
			{
				GeniiCommon client = ClientUtils.createProxy(
					GeniiCommon.class, aepr);
				client.destroy(new Destroy());
				responses.add(new TerminateActivityResponseType(aepr, true, 
					null, null));
			}
			catch (Throwable cause)
			{
				responses.add(new TerminateActivityResponseType(aepr, false, 
					BESFaultManager.constructFault(cause), null));
			}
		}
		
		return new TerminateActivitiesResponseType(
			responses.toArray(new TerminateActivityResponseType[0]), null);
	}
}