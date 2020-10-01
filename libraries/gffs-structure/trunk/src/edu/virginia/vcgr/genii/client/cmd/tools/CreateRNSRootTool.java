package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.context.ContextFileSystem;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSSpace;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class CreateRNSRootTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(CreateRNSRootTool.class);

	static private final String _DESCRIPTION = "config/tooldocs/description/dcreate-root-rns";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/ucreate-rns-root";

	private String _protocol = "https";
	private String _host = "localhost";
	private int _port = 18080;
	private String _dirPath=null;
	private boolean _selectReplica=false;
	private String _baseURLPath = "axis/services";

	public CreateRNSRootTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE_RESOURCE), true, ToolCategory.DATA);
	}

	@Option({ "selectReplica" })
	public void setselectReplica()
	{
		_selectReplica = true;
	}

	@Option({ "dirPath" })
	public void setdirPath(String dirPath)
	{
		_dirPath = dirPath;
	}

	@Option({ "protocol" })
	public void setProtocol(String protocol)
	{
		_protocol = protocol;
	}

	@Option({ "host" })
	public void setHost(String host)
	{
		_host = host;
	}

	@Option({ "port" })
	public void setPort(String portString)
	{
		_port = Integer.parseInt(portString);
	}

	@Option({ "base-path" })
	public void setBase_path(String baseURLPath)
	{
		_baseURLPath = baseURLPath;
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		String filename = getArgument(0);
		if (_dirPath !=null) {
			RNSRootFromPath(filename);
		}
		else {
			String baseURL = edu.virginia.vcgr.appmgr.net.Hostname.normalizeURL(_protocol + "://" + _host + ":" + _port + "/" + _baseURLPath);
			createRNSRoot(filename, baseURL);
		}
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
		if (_dirPath!=null) return;
		if (!_protocol.equalsIgnoreCase("http") && !_protocol.equalsIgnoreCase("https"))
			throw new InvalidToolUsageException("Protocol must be either http or https");
	}

	// 2020-09-27 by ASG. We are going to create a new root context from an existing directory in the namespace.
	public void RNSRootFromPath(String filename) {
		GeniiPath gp = new GeniiPath(_dirPath);
		RNSPath tp = gp.lookupRNS();
		// Check that the directory exists and is a directory.
		if (!tp.exists() || !tp.isRNS() ) {
			stderr.println(filename+" is does not exist or is not a directory! Aborting!");
			return;
		}
		try {
			// Now check if they are looking for a particular replica
			EndpointReferenceType epr = tp.getEndpoint();
			if (_selectReplica) {
				//ReplicateTool.listReplicas(epr, stdout);
				epr = ReplicateTool.replicaPicker(epr, stdout, stdin);
			}
			RNSPath rp = new RNSPath(epr);
			rp.createSandbox();
			// ------------------------
			ICallingContext ctxt = ContextManager.bootstrap(rp);
			DeploymentName deploymentName = new DeploymentName();
			ConnectTool.connect(ctxt, deploymentName);
			String msg = "Storing configuration to \"" + filename + "\".";
			stdout.println(msg);
			_logger.info(msg);

			ContextFileSystem.store(new File(filename), null, ctxt);
			
		}
		catch (RNSPathDoesNotExistException ex) {
			stdout.println("Path " + _dirPath + " does not exist.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createRNSRoot(String filename, String baseURL) throws IOException
	{
		RNSPath root = RNSSpace.createNewSpace(baseURL + "/EnhancedRNSPortType");
		ICallingContext ctxt = ContextManager.bootstrap(root);
		DeploymentName deploymentName = new DeploymentName();
		ConnectTool.connect(ctxt, deploymentName);
		String msg = "Storing configuration to \"" + filename + "\".";
		stdout.println(msg);
		_logger.info(msg);

		ContextFileSystem.store(new File(filename), null, ctxt);
	}
}