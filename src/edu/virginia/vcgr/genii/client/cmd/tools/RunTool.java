package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.Token;
import org.apache.axis.types.URI;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.ActivityStatusType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.GetActivityStatusesResponseType;
import org.ggf.bes.factory.GetActivityStatusesType;
import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.appdesc.ApplicationDescriptionPortType;
import edu.virginia.vcgr.genii.appdesc.SupportDocumentType;
import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.bes.activity.BESActivityPortType;
import edu.virginia.vcgr.genii.client.appdesc.Matching;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESActivityConstants;
import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.bes.BESUtils;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.deployer.AppDeployerConstants;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.notification.INotificationHandler;
import edu.virginia.vcgr.genii.client.notification.NotificationServer;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.run.JSDLFormer;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.notification.Notify;
import edu.virginia.vcgr.genii.common.notification.Subscribe;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;

import edu.virginia.vcgr.genii.deployer.ApplicationDeployerPortType;
import edu.virginia.vcgr.genii.deployer.CreateDeploymentRequestType;
import edu.virginia.vcgr.genii.deployer.ReifyJSDLRequestType;
import edu.virginia.vcgr.genii.scheduler.ScheduleCriteriaType;
import edu.virginia.vcgr.genii.scheduler.basic.BasicSchedulerPortType;

public class RunTool extends BaseGridTool
{
	static private Pattern _STAGE_PATTERN = Pattern.compile(
	"([^\\/]+)\\/(.+)$");
	
	static private final String _DESCRIPTION =
		"Runs the indicated JSDL file at the target BES container.";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/run-usage.txt";
	
	private Object _stateLock = new Object();
	private ActivityState _state = null;
	
	private class NotificationHandler implements INotificationHandler
	{
		@Override
		public void notify(Notify notify)
		{
			try
			{
				MessageElement []any = notify.get_any();
				if (any != null)
				{
					for (MessageElement a : any)
					{
						QName name = a.getQName();
						if (name.equals(
							BESConstants.GENII_BES_NOTIFICATION_STATE_ELEMENT_QNAME))
						{
							ActivityStatusType ast = 
								(ActivityStatusType)ObjectDeserializer.toObject(
									a, ActivityStatusType.class);
							ActivityState aState = new ActivityState(ast);
							synchronized(_stateLock)
							{
								_state = aState;
								_stateLock.notify();
							}
						}
					}
				}
			}
			catch (ResourceException re)
			{
				re.printStackTrace(System.err);
			}
		}
	}
	
	private String _name = null;
	private String _asyncName = null;
	private String _jsdl = null;
	private boolean _checkStatus = false;
	
	private ArrayList<String> _stageIns = new ArrayList<String>();
	private ArrayList<String> _stageOuts = new ArrayList<String>();
	private HashMap<String, String> _variables =
		new HashMap<String, String>();
	private String _stdout = null;
	private String _stderr = null;
	private String _stdin = null;
	
	public RunTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public void setAsync_name(String asyncName)
	{
		_asyncName = asyncName;
	}
	
	public void setJsdl(String jsdl)
	{
		_jsdl = jsdl;
	}
	
	public void setCheck_status()
	{
		_checkStatus = true;
	}
	
	public void setStdout(String sout)
	{
		_stdout = sout;
	}
	
	public void setStdin(String sin)
	{
		_stdin = sin;
	}
	
	public void setStderr(String serr)
	{
		_stderr = serr;
	}
	
	public void addStage_in(String value)
	{
		_stageIns.add(value);
	}
	
	public void addStage_out(String value)
	{
		_stageOuts.add(value);
	}
	
	@Override
	public void addArgument(String argument) throws ToolException
	{
		if (argument.startsWith("--D"))
		{
			argument = argument.substring(3);
			int index = argument.indexOf('=');
			if (index <= 0)
				throw new InvalidToolUsageException(
					"Invalid format for an environment value.");
			_variables.put(argument.substring(0, index),
				argument.substring(index + 1));
		} else
		{
			super.addArgument(argument);
		}
	}
	
	static private boolean equals(ActivityState one, ActivityState two)
	{
		if (one == null)
		{
			if (two == null)
				return true;
			else
				return false;
		} else
		{
			if (two == null)
				return false;
		}
		
		return one.equals(two);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		NotificationServer server = null;
		
		String asyncPath = _asyncName;
		EndpointReferenceType activity = null;
		
		RNSPath besOrSchedPath;
		EndpointReferenceType besContainer;
		
		if (_checkStatus)
		{
			RNSPath path = RNSPath.getCurrent();
			path = path.lookup(getArgument(0), RNSPathQueryFlags.MUST_EXIST);
			
			ActivityState state = checkStatus(path.getEndpoint());
			stdout.println("Status:  " + state);
			if (state.isFailedState())
				throw getError(path.getEndpoint());
			return 0;
		} else 
		{
			Subscribe subscribeRequest = null;
			
			if (asyncPath == null)
			{
				server = NotificationServer.createStandardServer();
				server.start();
				EndpointReferenceType target = server.addNotificationHandler(
					Pattern.compile(
						String.format("^%s$", Pattern.quote(
							WellknownTopics.BES_ACTIVITY_STATUS_CHANGE))),
					new NotificationHandler());
				subscribeRequest = new Subscribe(
					new Token(WellknownTopics.BES_ACTIVITY_STATUS_CHANGE),
					null, target, null);	
			}
			
			if (_jsdl != null)
			{
				String jobName = _name;
				RNSPath path = ContextManager.getCurrentContext(
					).getCurrentPath();
				besOrSchedPath = path.lookup(getArgument(0), 
					RNSPathQueryFlags.MUST_EXIST);
				besContainer = getBESContainer(besOrSchedPath);
				activity = submitJob(
					_jsdl, besContainer, jobName, subscribeRequest);
			} else
			{
				RNSPath path = ContextManager.getCurrentContext(
					).getCurrentPath();
				besOrSchedPath = path.lookup(getArgument(0), 
					RNSPathQueryFlags.MUST_EXIST);
				besContainer = getBESContainer(besOrSchedPath);
				String jobName = _name;
				JobDefinition_Type jobDef = createJobDefinition(
					jobName, getArguments().subList(1, getArguments().size()));
				ObjectSerializer.serialize(stdout, jobDef,
						new QName("http://example.org", "job-definition"));
				stdout.flush();
				activity = submitJob(jobDef, besContainer, subscribeRequest);
			}
		}
		
		if (asyncPath != null)
		{
			RNSPath path = RNSPath.getCurrent();
			path = path.lookup(asyncPath, RNSPathQueryFlags.MUST_NOT_EXIST);
			path.link(activity);
			
			return 0;
		}
		
		try
		{
			ActivityState lastState = null;
			long startTime = System.currentTimeMillis();
			
			while (true)
			{
				synchronized(_stateLock)
				{
					if (!equals(_state, lastState))
					{
						lastState = _state;
						stdout.println("Status:  " + lastState);
						if (lastState.isFinalState())
						{
							Throwable error = null;
							if (lastState.isFailedState())
								error = getError(activity);
							GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, activity);
							common.destroy(new Destroy());
			
							if (error != null)
								throw error;
							
							return 0;
						}
					}
					
					long diffTime = System.currentTimeMillis() - startTime;
					long waitTime = 10000 - diffTime;
					if (waitTime < 1)
						waitTime = 1;
					_stateLock.wait(waitTime);
					diffTime = System.currentTimeMillis() - startTime;
					if (diffTime >= 10000)
					{
						_state = checkStatus(besContainer, activity);
						startTime = System.currentTimeMillis();
					}
				}
			}
		}
		finally
		{
			if (server != null)
				server.stop();
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_checkStatus)
		{
			if (numArguments() != 1)
				throw new InvalidToolUsageException();
			else if (_jsdl != null)
			{
				if (numArguments() != 1)
					throw new InvalidToolUsageException();
			} else
			{
				if (numArguments() < 2 || _name == null)
					throw new InvalidToolUsageException();
			}
		} else if (_jsdl != null)
		{
			if (numArguments() != 1)
				throw new InvalidToolUsageException("No BES container specified.");
		} else if (_name != null)
		{
			if (numArguments() < 2)
				throw new InvalidToolUsageException("No BES container or executable specified.");
		} else
		{
			throw new InvalidToolUsageException("Missing required flags --check-status, --jsdl, or --name");
		}
	}
	
	public JobDefinition_Type createJobDefinition(
		String jobName, java.util.List<String> cLine)
			throws URI.MalformedURIException
	{
		JSDLFormer former = new JSDLFormer(jobName, cLine);
		
		if (_stdout != null)
			former.redirectStdout(_stdout);
		if (_stderr != null)
			former.redirectStderr(_stderr);
		if (_stdin != null)
			former.redirectStdin(_stdin);
		
		former.environment().putAll(_variables);
		Map<String, URI> stages = former.inDataStages();
		for (String stage : _stageIns)
		{
			Matcher matcher = _STAGE_PATTERN.matcher(stage);
			if (!matcher.matches())
				throw new IllegalArgumentException("Data stage description \"" +
					stage + "\" does not match required pattern of " +
					"<filename>/<stage-uri>");
			stages.put(matcher.group(1), new URI(matcher.group(2)));
		}
		
		stages = former.outDataStages();
		for (String stage : _stageOuts)
		{
			Matcher matcher = _STAGE_PATTERN.matcher(stage);
			if (!matcher.matches())
				throw new IllegalArgumentException("Data stage description \"" +
					stage + "\" does not match required pattern of " +
					"<filename>/<stage-uri>");
			stages.put(matcher.group(1), new URI(matcher.group(2)));
		}
		
		return former.formJSDL();
	}
	
	static public ActivityState checkStatus(String jobPath)
		throws RemoteException, RNSException, IOException
	{
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(jobPath, RNSPathQueryFlags.MUST_EXIST);
		
		return checkStatus(path.getEndpoint());
	}
	
	static public ActivityState checkStatus(EndpointReferenceType activity)
		throws ResourceException, RemoteException
	{
		GeniiCommon common = ClientUtils.createProxy(
				GeniiCommon.class, activity);
		GetResourcePropertyResponse resp = common.getResourceProperty(
			BESActivityConstants.STATUS_ATTR);
		
		MessageElement elem = (MessageElement)(resp.get_any()[0].getChildElements().next());
		return new ActivityState(elem);
	}
	
	static public ActivityState checkStatus(
		EndpointReferenceType besContainer, EndpointReferenceType activity)
		throws ResourceException, RemoteException,
			RNSPathDoesNotExistException
	{
		GeniiBESPortType bes = ClientUtils.createProxy(
			GeniiBESPortType.class, besContainer);
		GetActivityStatusesResponseType resp = bes.getActivityStatuses(
			new GetActivityStatusesType(new EndpointReferenceType[] { activity }, null));
		return new ActivityState(resp.getResponse(0).getActivityStatus());
	}
	
	static public EndpointReferenceType submitJob(String jsdlFileName,
		EndpointReferenceType besContainer, String optJobName,
		Subscribe subscribeRequest)
			throws IOException, ResourceException,
				RNSException
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(jsdlFileName);
			JobDefinition_Type jobDef = 
				(JobDefinition_Type)ObjectDeserializer.deserialize(
					new InputSource(fin), JobDefinition_Type.class);
			if (optJobName != null)
			{
				JobIdentification_Type ident =
					jobDef.getJobDescription().getJobIdentification();
				if (ident != null)
				{
					ident.setJobName(optJobName);
				} else
				{
					jobDef.getJobDescription().setJobIdentification(
						new JobIdentification_Type(optJobName, null, null,
							null, null));
				}
			}
			
			return submitJob(jobDef, besContainer, subscribeRequest);
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	static public EndpointReferenceType submitJob(JobDefinition_Type jobDef,
		EndpointReferenceType besContainer, Subscribe subscribeRequest) 
		throws ResourceException,
			RNSException, RemoteException
	{
		GeniiBESPortType bes = ClientUtils.createProxy(
			GeniiBESPortType.class, besContainer);
		
		jobDef = deployAndReify(bes, jobDef);
		if (jobDef == null)
				throw new ResourceException(
					"Unable to find a suitable deployment.");
		
		ActivityDocumentType adt = new ActivityDocumentType(jobDef, null);
		if (subscribeRequest != null)
			BESUtils.addSubscription(adt, subscribeRequest);
		
		CreateActivityType createActivityRequest = new CreateActivityType(
			adt, null);
		CreateActivityResponseType response = 
			bes.createActivity(createActivityRequest);
		return response.getActivityIdentifier();
	}
	
	static private EndpointReferenceType getBESContainer(RNSPath besOrSchedPath)
		throws RNSPathDoesNotExistException, GenesisIISecurityException, 
			ResourceException, RemoteException
	{
		EndpointReferenceType target = besOrSchedPath.getEndpoint();
		TypeInformation tInfo = new TypeInformation(target);
		if (tInfo.isScheduler())
		{
			BasicSchedulerPortType scheduler = ClientUtils.createProxy(
				BasicSchedulerPortType.class, target);
			EndpointReferenceType []result = scheduler.scheduleActivities(
				new ScheduleCriteriaType[] {
					new ScheduleCriteriaType()
				});
			if (result == null || result.length != 1)
				throw new ResourceException(
					"Scheduler didn't return a reasonable schedule.");
			target = result[0];
		}
		
		return target;
	}
	
	static public final String GENII_APP_NS =
		"http://vcgr.cs.virginia.edu/genii/application";
	static private QName _GENII_APP_PATH_ELEMENT =
		new QName(GENII_APP_NS, "ApplicationPath");
	static private QName _GENII_APP_ENDPOINT_ELEMENT =
		new QName(GENII_APP_NS, "ApplicationEndpoint");
	
	static private JobDefinition_Type deployAndReify(
		GeniiBESPortType bes, JobDefinition_Type jobDef)
		throws RNSException, ResourceException,
			RemoteException
	{
		EndpointReferenceType applicationDescriptionEPR = null;
		
		Application_Type application = 
			jobDef.getJobDescription().getApplication();
		if (application != null)
		{
			MessageElement []any = application.get_any();
			if (any != null)
			{
				for (MessageElement element : any)
				{
					QName name = element.getQName();
					if (name.equals(_GENII_APP_PATH_ELEMENT))
					{
						applicationDescriptionEPR = 
							getApplicationDescriptionEndpoint(
								element.getAttribute("path"));
						break;
					} else if (name.equals(_GENII_APP_ENDPOINT_ELEMENT))
					{
						applicationDescriptionEPR = 
							ObjectDeserializer.toObject(
								element, EndpointReferenceType.class);
						break;
					}
				}
			}
		}
		
		if (applicationDescriptionEPR != null)
		{
			Collection<EndpointReferenceType> deployers =
				getPossibleBESDeployers(bes);
			
			// TODO -- right now, for testing and expediency, we assume
			// that any deployer can handle the deployment.  No matching
			// takes place.
			if (deployers.size() == 0)
				throw new ConfigurationException(
					"Couldn't find an appropriate deployer for the application.");
			
			EndpointReferenceType deploymentEPR = createDeployment(
				deployers.iterator().next(), applicationDescriptionEPR);
			if (deploymentEPR == null)
				return null;
			
			ApplicationDeployerPortType deployment =
				ClientUtils.createProxy(ApplicationDeployerPortType.class,
					deploymentEPR);
			jobDef = deployment.reifyJSDL(
				new ReifyJSDLRequestType(jobDef)).getReifiedDocument();
		}
		
		return jobDef;
	}
	
	static private EndpointReferenceType getApplicationDescriptionEndpoint(
		String path) throws RNSException
	{
		if (path == null)
			throw new RuntimeException("Missing required JSDL attribute" +
				" \"path\" inside ApplicationPath element.");
		
		RNSPath rPath = RNSPath.getCurrent().lookup(
			path, RNSPathQueryFlags.MUST_EXIST);
		
		return rPath.getEndpoint();
	}
	
	static private Collection<EndpointReferenceType> getPossibleBESDeployers(
		GeniiBESPortType bes) throws ResourceUnknownFaultType, RemoteException
	{
		ArrayList<EndpointReferenceType> ret =
			new ArrayList<EndpointReferenceType>();
		
		GetResourcePropertyResponse response =
			bes.getResourceProperty(BESConstants.DEPLOYER_EPR_ATTR);
		MessageElement []any = response.get_any();
		if (any == null)
			return ret;
		
		for (MessageElement element : any)
		{
			ret.add(ObjectDeserializer.toObject(
				element, EndpointReferenceType.class));
		}
		
		return ret;
	}
	
	static private EndpointReferenceType createDeployment(
		EndpointReferenceType deployerService,
		EndpointReferenceType applicationDescription)
		throws RemoteException
	{
		ApplicationDeployerPortType deployer =
			ClientUtils.createProxy(ApplicationDeployerPortType.class,
				deployerService);
		
		EndpointReferenceType deploymentDescription = 
			pickDeploymentDescription(deployer, applicationDescription);
		
		if (deploymentDescription == null)
			return null;
		
		return deployer.createDeployment(new CreateDeploymentRequestType(
			deploymentDescription)).getDeployment();
	}
	
	static private Random _generator = new Random();
	static private EndpointReferenceType pickDeploymentDescription(
		ApplicationDeployerPortType deployer,
		EndpointReferenceType applicationDescription)
		throws RemoteException
	{
		ArrayList<EntryType> _acceptableChoices = 
			new ArrayList<EntryType>();
		
		SupportDocumentType []have = determineSupport(deployer);
		
		ApplicationDescriptionPortType application =
			ClientUtils.createProxy(ApplicationDescriptionPortType.class,
				applicationDescription);
		ListResponse resp = application.list(new List(
			null));
		for (EntryType entry : resp.getEntryList())
		{
			for (MessageElement element : entry.get_any())
			{
				SupportDocumentType doc = ObjectDeserializer.toObject(
					element, SupportDocumentType.class);
				if (Matching.matches(doc, have))
					_acceptableChoices.add(entry);
			}
		}

		if (_acceptableChoices.size() == 0)
			return null;
		
		return _acceptableChoices.get(_generator.nextInt(
			_acceptableChoices.size())).getEntry_reference();
	}
	
	static private SupportDocumentType[] determineSupport(
		ApplicationDeployerPortType deployer)
		throws ResourceUnknownFaultType, RemoteException
	{
		GetResourcePropertyResponse resp = deployer.getResourceProperty(
				AppDeployerConstants.DEPLOYER_SUPPORT_DOCUMENT_ATTR_QNAME);
		
		MessageElement []any = resp.get_any();
		SupportDocumentType []ret = new SupportDocumentType[any.length];
		
		for (int lcv = 0; lcv < any.length; lcv++)
		{
			ret[lcv] = ObjectDeserializer.toObject(
				any[lcv], SupportDocumentType.class);
		}
		
		return ret;
	}
	
	static private Throwable getError(EndpointReferenceType activity)
		throws RemoteException, IOException, ClassNotFoundException
	{
		BESActivityPortType act = ClientUtils.createProxy(BESActivityPortType.class, activity);
		byte []serializedFault = act.getError(null).getSerializedFault();
		if (serializedFault == null)
			throw new IOException("BES Activity in unknown state.");
		return (Throwable)DBSerializer.deserialize(serializedFault);
	}
}
