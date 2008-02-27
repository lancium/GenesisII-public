package edu.virginia.vcgr.genii.container.bes;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.axis.types.UnsignedInt;
import org.apache.axis.types.UnsignedLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.BESPortType;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.FactoryResourceAttributesDocumentType;
import org.ggf.bes.factory.GetActivityDocumentResponseType;
import org.ggf.bes.factory.GetActivityStatusResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentResponseType;
import org.ggf.bes.factory.GetFactoryAttributesDocumentType;
import org.ggf.bes.factory.TerminateActivityResponseType;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobDescription_Type;
import org.ggf.jsdl.JobIdentification_Type;
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
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.GuaranteedDirectory;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.bes.activity.BESActivityPortType;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientConstructionParameters;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.HumanNameUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.notification.Subscribe;
import edu.virginia.vcgr.genii.common.notification.UserDataType;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivityUtils;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.bes.resource.IBESResource;
import edu.virginia.vcgr.genii.container.byteio.RByteIOResource;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.sysinfo.SystemInfoUtils;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class BESServiceImpl extends GenesisIIBase 
	implements BESPortType
{
	static private Log _logger = LogFactory.getLog(BESServiceImpl.class);
	
	static private QName _FILENAME_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "create-file-filename");
	static private QName _FILEPATH_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "data-filepath");
	
	static private final long _DEFAULT_TIME_TO_LIVE = 1000L * 60 * 60;
	static final String _IS_ACCEPTING_PROPERTY = "is-accepting-jobs";
	
	static protected EndpointReferenceType _localActivityServiceEpr = null;
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new BESAttributesHandler(getAttributePackage());
	}
	
	public BESServiceImpl() throws RemoteException
	{
		super("BESPortType");
		
		addImplementedPortType(WellKnownPortTypes.BES_FACTORY_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.BES_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.RNS_SERVICE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE);
	}
	
	public QName getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.BES_SERVICE_PORT_TYPE;
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType myEPR,
		HashMap<QName, Object> constructionParameters)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, myEPR, constructionParameters);
		
		IResource resource = rKey.dereference();
		
		resource.setProperty(_IS_ACCEPTING_PROPERTY, "true");
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateActivityResponseType createActivity(
			CreateActivityType createActivityRequest) throws RemoteException
	{
		IBESResource resource = null;
		ActivityDocumentType adt = createActivityRequest.getActivityDocument();
		JobDefinition_Type jobDef = adt.getJobDefinition();
		
		if (jobDef == null)
			throw new AxisFault("null Job Definition Document passed in.");
		
		jobDef = modifyJobName(jobDef);
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IBESResource)rKey.dereference();
		
		try
		{
			if (_localActivityServiceEpr == null) { 
				// only need to make this epr from scratch once (which involves
				// a get-attr rpc to the service to get its full epr)
				_localActivityServiceEpr = 
					EPRUtils.makeEPR(Container.getServiceURL("BESActivityPortType"));
			}
			
			BESActivityPortType activity = ClientUtils.createProxy(
				BESActivityPortType.class,
				_localActivityServiceEpr);
			
			VcgrCreateResponse resp = activity.vcgrCreate(
				new VcgrCreate(
					BESActivityUtils.createCreationProperties(
						jobDef, (String)resource.getKey())));
			return new CreateActivityResponseType(resp.getEndpoint(), adt);
		}
		catch (ConfigurationException ce)
		{
			throw new RemoteException(ce.getLocalizedMessage());
		}
	}

	@RWXMapping(RWXCategory.READ)
	public GetActivityDocumentResponseType[] getActivityDocuments(
		EndpointReferenceType[] getActivityDocumentsRequest)
		throws ResourceException
	{
		GetActivityDocumentResponseType []ret;
		
		ret = new GetActivityDocumentResponseType[getActivityDocumentsRequest.length];
		
		for (int lcv = 0; lcv < ret.length; lcv++)
		{
			try
			{
				ResourceKey aKey = ResourceManager.getTargetResource(
					getActivityDocumentsRequest[lcv]);
				IBESActivityResource activity = (IBESActivityResource)aKey.dereference();
				ret[lcv] = new GetActivityDocumentResponseType(
					getActivityDocumentsRequest[lcv],
					activity.getJobDefinition(), null);
			}
			catch (BaseFaultType buft)
			{
				ret[lcv] = new GetActivityDocumentResponseType(
					getActivityDocumentsRequest[lcv], null, buft);
			}
		}
		
		return ret;
	}

	@RWXMapping(RWXCategory.READ)
	public GetActivityStatusResponseType[] getActivityStatuses(
			EndpointReferenceType[] getActivityStatusesRequest)
			throws ResourceException
	{
		GetActivityStatusResponseType []ret;
		
		ret = new GetActivityStatusResponseType[getActivityStatusesRequest.length];
		
		for (int lcv = 0; lcv < ret.length; lcv++)
		{
			try
			{
				ResourceKey aKey = ResourceManager.getTargetResource(
					getActivityStatusesRequest[lcv]);
				IBESActivityResource activity = (IBESActivityResource)aKey.dereference();
				ret[lcv] = new GetActivityStatusResponseType(
					getActivityStatusesRequest[lcv],
					activity.getOverallStatus(), null);
			}
			catch (BaseFaultType buft)
			{
				ret[lcv] = new GetActivityStatusResponseType(
					getActivityStatusesRequest[lcv], null, buft);
			}
		}
		
		return ret;
	}

	@RWXMapping(RWXCategory.OPEN)
	public GetFactoryAttributesDocumentResponseType getFactoryAttributesDocument(
			GetFactoryAttributesDocumentType getFactoryAttributesDocumentRequest)
			throws RemoteException
	{
		_logger.debug("Starting to get bes factory attributes.");
		FactoryResourceAttributesDocumentType attrs =
			new FactoryResourceAttributesDocumentType(
				BESAttributesHandler.getOperatingSystem(),
				BESAttributesHandler.getCPUArchitecture(),
				new UnsignedInt(BESAttributesHandler.getCPUCount()),
				new UnsignedLong(SystemInfoUtils.getIndividualCPUSpeed()),
				new UnsignedLong(SystemInfoUtils.getPhysicalMemory()),
				new UnsignedLong(SystemInfoUtils.getVirtualMemory()),
				null,
				true,
				BESAttributesHandler.getName(),
				BESAttributesHandler.getDescription(),
				new UnsignedInt(BESAttributesHandler.getTotalNumberOfActivities()),
				new QName(GenesisIIConstants.GENESISII_NS, "legacy"),
				BESAttributesHandler.getActivityReferences(),
				null,
				new QName("http://schemas.ggf.org/bes/2006/08/bes/naming", "WS-Naming"));
		_logger.debug("Finished getting bes factory attributes.");
		return new GetFactoryAttributesDocumentResponseType(attrs);
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public TerminateActivityResponseType[] terminateActivities(
			EndpointReferenceType[] terminateActivitiesRequest)
			throws RemoteException
	{
		TerminateActivityResponseType []ret;
		
		ret = new TerminateActivityResponseType[terminateActivitiesRequest.length];
		
		for (int lcv = 0; lcv < ret.length; lcv++)
		{
			try
			{
				// call immediate-terminate on the activity
                GeniiCommon common = ClientUtils.createProxy(
                        GeniiCommon.class,
                        terminateActivitiesRequest[lcv]);

                common.destroy(new Destroy());
			}
			catch (BaseFaultType buft)
			{
				ret[lcv] = new TerminateActivityResponseType(
					terminateActivitiesRequest[lcv], false, buft);
			}
			catch (ConfigurationException cfe)
			{
				ret[lcv] = new TerminateActivityResponseType(
					terminateActivitiesRequest[lcv], false, cfe);
			}
		}
		
		return ret;	
	}

	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) throws RemoteException,
		RNSEntryExistsFaultType, ResourceUnknownFaultType,
		RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		throw new RemoteException("\"add\" not applicable.");
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
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponse createFile(CreateFile createFileRequest)
		throws RemoteException, RNSEntryExistsFaultType,
		ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
		RNSFaultType
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
			ClientConstructionParameters.createTimeToLiveProperty(
				_DEFAULT_TIME_TO_LIVE)
		};
		
		try
		{
			StreamableByteIOPortType sbyteio = ClientUtils.createProxy(
				StreamableByteIOPortType.class, EPRUtils.makeEPR(
					Container.getServiceURL("StreamableByteIOPortType")));
			VcgrCreateResponse resp = sbyteio.vcgrCreate(new VcgrCreate(parameters));
			
			return new CreateFileResponse(resp.getEndpoint());
		}
		catch (ConfigurationException ce)
		{
			throw new ResourceException(ce.getLocalizedMessage(), ce);
		}
	}
	
	@RWXMapping(RWXCategory.READ)
	public ListResponse list(List listRequest) throws RemoteException,
		ResourceUnknownFaultType, RNSEntryNotDirectoryFaultType,
		RNSFaultType
	{
		Pattern p = Pattern.compile(listRequest.getEntry_name_regexp());
		
		IBESResource resource = null;
		EndpointReferenceType []activities = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IBESResource)rKey.dereference();
		
		activities = resource.getContainedActivities();
			
		ArrayList<EntryType> aEntryList = new ArrayList<EntryType>(
			activities.length);
			
		for (EndpointReferenceType activity : activities)
		{
			/* We have to remember that an activity can dissapear from
			 * underneath us while this is going on.
			 */
			String name = null;
			try
			{
				ResourceKey activityKey = ResourceManager.getTargetResource(activity);
				IBESActivityResource activityResource =
					(IBESActivityResource)activityKey.dereference();
				name = (String)activityResource.getProperty(
					IBESActivityResource.ACTIVITY_NAME_PROPERTY);
				if (name == null)
					name = new WSName(activity).toString();
					
				if (p.matcher(name).matches())
					aEntryList.add(new EntryType(name, null, activity));
			}
			catch (ResourceUnknownFaultType unknown)
			{
				_logger.debug(
					"An activity dissapeared while we were listing it.", 
					unknown);
			}
		}
			
		EntryType []entryList = new EntryType[aEntryList.size()];
		aEntryList.toArray(entryList);
		ListResponse resp = new ListResponse(entryList);
		return resp;
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public MoveResponse move(Move moveRequest) throws RemoteException,
		ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("\"move\" not applicable.");
	}
	
	@RWXMapping(RWXCategory.READ)
	public QueryResponse query(Query queryRequest) throws RemoteException,
		ResourceUnknownFaultType, RNSFaultType
	{
		throw new RemoteException("\"query\" not applicable.");
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public String[] remove(Remove removeRequest) throws RemoteException,
		ResourceUnknownFaultType, RNSDirectoryNotEmptyFaultType,
		RNSFaultType
	{
		throw new RemoteException("\"remove\" not applicable.");
	}

	@RWXMapping(RWXCategory.OPEN)
	public void notify(Notify notify) throws RemoteException, ResourceUnknownFaultType
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
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(file);
			JobDefinition_Type jobDef = 
				(JobDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(fin), JobDefinition_Type.class);
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
				jobDef, null)));
		}
		finally
		{
			StreamUtils.close(fin);
			file.delete();
		}
	}
	
	private JobDefinition_Type modifyJobName(JobDefinition_Type jobDef)
		throws RemoteException
	{
		ArrayList<String> takenNames = new ArrayList<String>();
		
		JobDescription_Type desc = jobDef.getJobDescription();
		if (desc != null)
		{
			JobIdentification_Type ident = desc.getJobIdentification();
			if (ident != null)
			{
				String name = ident.getJobName();
				if (name != null)
				{
					ListResponse resp = list(new List(
						Pattern.quote(name) + "( \\([0-9]+\\))?"));
					EntryType []list = resp.getEntryList();
					for (EntryType entry : list)
					{
						String entryName = entry.getEntry_name();
						takenNames.add(entryName);
					}
					
					ident.setJobName(
						HumanNameUtils.generateUniqueName(name, takenNames));
				}
			}
		}
		
		// TODO we haven't finished implementing this yet.
		return jobDef;
	}
}