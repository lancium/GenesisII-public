package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.InputStream;
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
import org.apache.axis.types.URI;
import org.ggf.bes.factory.ActivityDocumentType;
import org.ggf.bes.factory.CreateActivityResponseType;
import org.ggf.bes.factory.CreateActivityType;
import org.ggf.bes.factory.GetActivityStatusesResponseType;
import org.ggf.bes.factory.GetActivityStatusesType;
import org.ggf.jsdl.Application_Type;
import org.ggf.jsdl.JobDefinition_Type;
import org.ggf.jsdl.JobIdentification_Type;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSMetadataType;
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
import edu.virginia.vcgr.genii.client.deployer.AppDeployerConstants;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSIterable;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.run.JSDLFormer;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.ser.DBSerializer;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.notification.LightweightNotificationServer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeRequest;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityStateChangedContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.BESActivityTopics;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.client.gpath.*;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.GetResourcePropertyResponse;

import edu.virginia.vcgr.genii.deployer.ApplicationDeployerPortType;
import edu.virginia.vcgr.genii.deployer.CreateDeploymentRequestType;
import edu.virginia.vcgr.genii.deployer.ReifyJSDLRequestType;

public class RunTool extends BaseGridTool
{
	static private Pattern _STAGE_PATTERN = Pattern.compile("([^\\/]+)\\/(.+)$");

	static private final String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/drun";
	static private final String _USAGE_RESOURCE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/urun";
	static private final String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/run";

	private Object _stateLock = new Object();
	private ActivityState _state = null;

	private class NotificationHandler extends AbstractNotificationHandler<BESActivityStateChangedContents>
	{
		private NotificationHandler()
		{
			super(BESActivityStateChangedContents.class);
		}

		@Override
		public String handleNotification(TopicPath topic, EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference, BESActivityStateChangedContents contents) throws Exception
		{
			ActivityState state = contents.activityState();
			synchronized (_stateLock) {
				_state = state;
				_stateLock.notify();
			}
			return NotificationConstants.OK;
		}
	}

	private String _name = null;
	private String _asyncName = null;
	private String _jsdl = null;
	private boolean _checkStatus = false;

	private ArrayList<String> _stageIns = new ArrayList<String>();
	private ArrayList<String> _stageOuts = new ArrayList<String>();
	private HashMap<String, String> _variables = new HashMap<String, String>();
	private String _stdout = null;
	private String _stderr = null;
	private String _stdin = null;

	public RunTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE_RESOURCE), false, ToolCategory.EXECUTION);
		addManPage(new FileResource(_MANPAGE));
	}

	@Option({ "name" })
	public void setName(String name)
	{
		_name = name;
	}

	@Option({ "async-name" })
	public void setAsync_name(String asyncName)
	{
		_asyncName = asyncName;
	}

	@Option({ "jsdl" })
	public void setJsdl(String jsdl)
	{
		_jsdl = jsdl;
	}

	@Option({ "check-status" })
	public void setCheck_status()
	{
		_checkStatus = true;
	}

	@Option({ "stdout" })
	public void setStdout(String sout)
	{
		_stdout = sout;
	}

	@Option({ "stdin" })
	public void setStdin(String sin)
	{
		_stdin = sin;
	}

	@Option({ "stderr" })
	public void setStderr(String serr)
	{
		_stderr = serr;
	}

	@Option(value = { "stage-in" }, maxOccurances = "unbounded")
	public void addStage_in(String value)
	{
		_stageIns.add(value);
	}

	@Option(value = { "stage-out" }, maxOccurances = "unbounded")
	public void addStage_out(String value)
	{
		_stageOuts.add(value);
	}

	@Override
	public void addArgument(String argument) throws ToolException
	{
		if (argument.startsWith("--D")) {
			argument = argument.substring(3);
			int index = argument.indexOf('=');
			if (index <= 0)
				throw new InvalidToolUsageException("Invalid format for an environment value.");
			_variables.put(argument.substring(0, index), argument.substring(index + 1));
		} else {
			super.addArgument(argument);
		}
	}

	static private boolean equals(ActivityState one, ActivityState two)
	{
		if (one == null) {
			if (two == null)
				return true;
			else
				return false;
		} else {
			if (two == null)
				return false;
		}

		return one.equals(two);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		LightweightNotificationServer server = null;

		GeniiPath gPath = new GeniiPath(_asyncName);
		if (gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("[async-name] must be a grid path. ");
		String asyncPath = gPath.path();
		EndpointReferenceType activity = null;
		GeniiPath gPath0 = new GeniiPath(getArgument(0));
		if (gPath0.pathType() != GeniiPathType.Grid)
			if (_checkStatus)
				throw new InvalidToolUsageException("<job-path> must be a grid path");
			else
				throw new InvalidToolUsageException("<bes-container> | <scheduler> must be a grid path");

		RNSPath besOrSchedPath;
		EndpointReferenceType besContainer;

		if (_checkStatus) {
			RNSPath path = lookup(gPath0, RNSPathQueryFlags.MUST_EXIST);

			ActivityState state = checkStatus(path.getEndpoint());
			stdout.println("Status:  " + state);
			if (state.isFailedState())
				throw getError(path.getEndpoint());
			return 0;
		} else {
			SubscribeRequest subscribeRequest = null;

			if (asyncPath.equals("")) {
				server = LightweightNotificationServer.createStandardServer();
				server.start();

				TopicQueryExpression topicFilter = BESActivityTopics.ACTIVITY_STATE_CHANGED_TOPIC.asConcreteQueryExpression();

				subscribeRequest = server.createSubscribeRequest(topicFilter, null, null);

				server.registerNotificationHandler(topicFilter, new NotificationHandler());
			}

			if (_jsdl != null) {
				String jobName = _name;
				besOrSchedPath = lookup(gPath0, RNSPathQueryFlags.MUST_EXIST);
				besContainer = getBESContainer(besOrSchedPath);
				activity = submitJob(_jsdl, besContainer, jobName, subscribeRequest);
			} else {
				besOrSchedPath = lookup(gPath0, RNSPathQueryFlags.MUST_EXIST);
				besContainer = getBESContainer(besOrSchedPath);
				String jobName = _name;
				JobDefinition_Type jobDef = createJobDefinition(jobName, getArguments().subList(1, getArguments().size()));
				ObjectSerializer.serialize(stdout, jobDef, new QName("http://example.org", "job-definition"));
				stdout.flush();
				activity = submitJob(jobDef, besContainer, subscribeRequest);
			}
		}

		if (!asyncPath.equals("")) {
			RNSPath path = lookup(gPath, RNSPathQueryFlags.MUST_NOT_EXIST);
			path.link(activity);

			return 0;
		}

		try {
			ActivityState lastState = null;
			long startTime = System.currentTimeMillis();

			while (true) {
				synchronized (_stateLock) {
					if (!equals(_state, lastState)) {
						lastState = _state;
						stdout.println("Status:  " + lastState);
						if (lastState.isFinalState()) {
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
					if (diffTime >= 10000) {
						_state = checkStatus(besContainer, activity);
						startTime = System.currentTimeMillis();
					}
				}
			}
		} finally {
			if (server != null)
				server.stop();
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_checkStatus) {
			if (numArguments() != 1)
				throw new InvalidToolUsageException();

		} else if (_jsdl != null) {
			if (numArguments() != 1)
				throw new InvalidToolUsageException("No BES container specified.");
		} else if (_name != null) {
			if (numArguments() < 2)
				throw new InvalidToolUsageException("No BES container or executable specified.");
		} else {
			throw new InvalidToolUsageException("Missing required flags --check-status, --jsdl, or --name");
		}
	}

	public JobDefinition_Type createJobDefinition(String jobName, java.util.List<String> cLine)
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
		for (String stage : _stageIns) {
			Matcher matcher = _STAGE_PATTERN.matcher(stage);
			if (!matcher.matches())
				throw new IllegalArgumentException("Data stage description \"" + stage
					+ "\" does not match required pattern of " + "<filename>/<stage-uri>");
			stages.put(matcher.group(1), new URI(matcher.group(2)));
		}

		stages = former.outDataStages();
		for (String stage : _stageOuts) {
			Matcher matcher = _STAGE_PATTERN.matcher(stage);
			if (!matcher.matches())
				throw new IllegalArgumentException("Data stage description \"" + stage
					+ "\" does not match required pattern of " + "<filename>/<stage-uri>");
			stages.put(matcher.group(1), new URI(matcher.group(2)));
		}

		return former.formJSDL();
	}

	static public ActivityState checkStatus(String jobPath) throws RemoteException, RNSException, IOException,
		InvalidToolUsageException
	{
		GeniiPath gPath = new GeniiPath(jobPath);
		RNSPath path = lookup(gPath, RNSPathQueryFlags.MUST_EXIST);

		return checkStatus(path.getEndpoint());
	}

	static public ActivityState checkStatus(EndpointReferenceType activity) throws ResourceException, RemoteException
	{
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, activity);
		GetResourcePropertyResponse resp = common.getResourceProperty(BESActivityConstants.STATUS_ATTR);

		MessageElement elem = (MessageElement) (resp.get_any()[0].getChildElements().next());
		return new ActivityState(elem);
	}

	static public ActivityState checkStatus(EndpointReferenceType besContainer, EndpointReferenceType activity)
		throws ResourceException, RemoteException, RNSPathDoesNotExistException
	{
		GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, besContainer);
		GetActivityStatusesResponseType resp = bes.getActivityStatuses(new GetActivityStatusesType(
			new EndpointReferenceType[] { activity }, null));
		return new ActivityState(resp.getResponse(0).getActivityStatus());
	}

	static public EndpointReferenceType submitJob(String jsdlFileName, EndpointReferenceType besContainer, String optJobName,
		SubscribeRequest subscribeRequest) throws IOException, ResourceException, RNSException
	{
		InputStream fin = null;

		try {
			GeniiPath gPath = new GeniiPath(jsdlFileName);
			if (gPath.pathType() == GeniiPathType.Grid)
				fin = gPath.openInputStream();
			else
				fin = new FileInputStream(gPath.path());
			JobDefinition_Type jobDef = (JobDefinition_Type) ObjectDeserializer.deserialize(new InputSource(fin),
				JobDefinition_Type.class);

			if (optJobName != null) {
				JobIdentification_Type ident = jobDef.getJobDescription().getJobIdentification();
				if (ident != null) {
					ident.setJobName(optJobName);
				} else {
					jobDef.getJobDescription().setJobIdentification(
						new JobIdentification_Type(optJobName, null, null, null, null));
				}
			}

			return submitJob(jobDef, besContainer, subscribeRequest);
		} finally {
			StreamUtils.close(fin);
		}
	}

	static public EndpointReferenceType submitJob(JobDefinition_Type jobDef, EndpointReferenceType besContainer,
		SubscribeRequest subscribeRequest) throws ResourceException, RNSException, RemoteException
	{
		GeniiBESPortType bes = ClientUtils.createProxy(GeniiBESPortType.class, besContainer);

		jobDef = deployAndReify(bes, jobDef);
		if (jobDef == null)
			throw new ResourceException("Unable to find a suitable deployment.");

		ActivityDocumentType adt = new ActivityDocumentType(jobDef, null);
		if (subscribeRequest != null)
			BESUtils.addSubscription(adt, subscribeRequest);

		CreateActivityType createActivityRequest = new CreateActivityType(adt, null);

		CreateActivityResponseType response = bes.createActivity(createActivityRequest);
		return response.getActivityIdentifier();

	}

	static private EndpointReferenceType getBESContainer(RNSPath besOrSchedPath) throws RNSPathDoesNotExistException,
		GenesisIISecurityException, ResourceException, RemoteException
	{
		EndpointReferenceType target = besOrSchedPath.getEndpoint();

		return target;
	}

	static public final String GENII_APP_NS = "http://vcgr.cs.virginia.edu/genii/application";
	static private QName _GENII_APP_PATH_ELEMENT = new QName(GENII_APP_NS, "ApplicationPath");
	static private QName _GENII_APP_ENDPOINT_ELEMENT = new QName(GENII_APP_NS, "ApplicationEndpoint");

	static private JobDefinition_Type deployAndReify(GeniiBESPortType bes, JobDefinition_Type jobDef) throws RNSException,
		ResourceException, RemoteException
	{
		EndpointReferenceType applicationDescriptionEPR = null;

		Application_Type application = jobDef.getJobDescription().getApplication();
		if (application != null) {
			MessageElement[] any = application.get_any();
			if (any != null) {
				for (MessageElement element : any) {
					QName name = element.getQName();
					if (name.equals(_GENII_APP_PATH_ELEMENT)) {
						applicationDescriptionEPR = getApplicationDescriptionEndpoint(element.getAttribute("path"));
						break;
					} else if (name.equals(_GENII_APP_ENDPOINT_ELEMENT)) {
						applicationDescriptionEPR = ObjectDeserializer.toObject(element, EndpointReferenceType.class);
						break;
					}
				}
			}
		}

		if (applicationDescriptionEPR != null) {
			Collection<EndpointReferenceType> deployers = getPossibleBESDeployers(bes);

			if (deployers.size() == 0)
				throw new ConfigurationException("Couldn't find an appropriate deployer for the application.");

			EndpointReferenceType deploymentEPR = createDeployment(deployers.iterator().next(), applicationDescriptionEPR);
			if (deploymentEPR == null)
				return null;

			ApplicationDeployerPortType deployment = ClientUtils.createProxy(ApplicationDeployerPortType.class, deploymentEPR);
			jobDef = deployment.reifyJSDL(new ReifyJSDLRequestType(jobDef)).getReifiedDocument();
		}

		return jobDef;
	}

	static private EndpointReferenceType getApplicationDescriptionEndpoint(String path) throws RNSException
	{
		if (path == null)
			throw new RuntimeException("Missing required JSDL attribute" + " \"path\" inside ApplicationPath element.");

		RNSPath rPath = RNSPath.getCurrent().lookup(path, RNSPathQueryFlags.MUST_EXIST);

		return rPath.getEndpoint();
	}

	static private Collection<EndpointReferenceType> getPossibleBESDeployers(GeniiBESPortType bes)
		throws ResourceUnknownFaultType, RemoteException
	{
		ArrayList<EndpointReferenceType> ret = new ArrayList<EndpointReferenceType>();

		GetResourcePropertyResponse response = bes.getResourceProperty(BESConstants.DEPLOYER_EPR_ATTR);
		MessageElement[] any = response.get_any();
		if (any == null)
			return ret;

		for (MessageElement element : any) {
			ret.add(ObjectDeserializer.toObject(element, EndpointReferenceType.class));
		}

		return ret;
	}

	static private EndpointReferenceType createDeployment(EndpointReferenceType deployerService,
		EndpointReferenceType applicationDescription) throws RemoteException
	{
		ApplicationDeployerPortType deployer = ClientUtils.createProxy(ApplicationDeployerPortType.class, deployerService);

		EndpointReferenceType deploymentDescription = pickDeploymentDescription(deployer, applicationDescription);

		if (deploymentDescription == null)
			return null;

		return deployer.createDeployment(new CreateDeploymentRequestType(deploymentDescription)).getDeployment();
	}

	static private Random _generator = new Random();

	static private EndpointReferenceType pickDeploymentDescription(ApplicationDeployerPortType deployer,
		EndpointReferenceType applicationDescription) throws RemoteException
	{
		ArrayList<RNSEntryResponseType> _acceptableChoices = new ArrayList<RNSEntryResponseType>();

		SupportDocumentType[] have = determineSupport(deployer);

		ApplicationDescriptionPortType application = ClientUtils.createProxy(ApplicationDescriptionPortType.class,
			applicationDescription);
		RNSIterable iterable = new RNSIterable(application.lookup(null), null, 100);
		for (RNSEntryResponseType entry : iterable) {
			RNSMetadataType mdt = entry.getMetadata();
			for (MessageElement element : mdt.get_any()) {
				SupportDocumentType doc = ObjectDeserializer.toObject(element, SupportDocumentType.class);
				if (Matching.matches(doc, have))
					_acceptableChoices.add(entry);
			}
		}

		StreamUtils.close(iterable.getIterable());

		if (_acceptableChoices.size() == 0)
			return null;

		return _acceptableChoices.get(_generator.nextInt(_acceptableChoices.size())).getEndpoint();
	}

	static private SupportDocumentType[] determineSupport(ApplicationDeployerPortType deployer)
		throws ResourceUnknownFaultType, RemoteException
	{
		GetResourcePropertyResponse resp = deployer
			.getResourceProperty(AppDeployerConstants.DEPLOYER_SUPPORT_DOCUMENT_ATTR_QNAME);

		MessageElement[] any = resp.get_any();
		SupportDocumentType[] ret = new SupportDocumentType[any.length];

		for (int lcv = 0; lcv < any.length; lcv++) {
			ret[lcv] = ObjectDeserializer.toObject(any[lcv], SupportDocumentType.class);
		}

		return ret;
	}

	static private Throwable getError(EndpointReferenceType activity) throws RemoteException, IOException,
		ClassNotFoundException
	{
		TypeInformation typeInfo = new TypeInformation(activity);
		if (typeInfo.isBESActivity()) {
			BESActivityPortType act = ClientUtils.createProxy(BESActivityPortType.class, activity);
			byte[] serializedFault = act.getError(null).getSerializedFault();
			if (serializedFault == null)
				throw new IOException("BES Activity in unknown state.");
			return (Throwable) DBSerializer.deserialize(serializedFault);
		} else {
			return new IOException("Activity not a standard Genesis II activity -- Error information unavailable!");
		}
	}
}
