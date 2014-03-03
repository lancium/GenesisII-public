package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.context.ContextFileSystem;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSSpace;

public class CreateRNSRootTool extends BaseGridTool {
	static private Log _logger = LogFactory.getLog(CreateRNSRootTool.class);

	static private final String _DESCRIPTION = "config/tooldocs/description/dcreate-root-rns";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/ucreate-rns-root";

	private String _protocol = "https";
	private String _host = "localhost";
	private int _port = 18080;
	private String _baseURLPath = "axis/services";

	public CreateRNSRootTool() {
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(
				_USAGE_RESOURCE), true, ToolCategory.DATA);
	}

	@Option({ "protocol" })
	public void setProtocol(String protocol) {
		_protocol = protocol;
	}

	@Option({ "host" })
	public void setHost(String host) {
		_host = host;
	}

	@Option({ "port" })
	public void setPort(String portString) {
		_port = Integer.parseInt(portString);
	}

	@Option({ "base-path" })
	public void setBase_path(String baseURLPath) {
		_baseURLPath = baseURLPath;
	}

	@Override
	protected int runCommand() throws Throwable {
		String filename = getArgument(0);

		String baseURL = Hostname.normalizeURL(_protocol + "://" + _host + ":"
				+ _port + "/" + _baseURLPath);
		createRNSRoot(filename, baseURL);
		return 0;
	}

	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 1)
			throw new InvalidToolUsageException();

		if (!_protocol.equalsIgnoreCase("http")
				&& !_protocol.equalsIgnoreCase("https"))
			throw new InvalidToolUsageException(
					"Protocol must be either http or https");
	}

	public void createRNSRoot(String filename, String baseURL)
			throws SAXException, ParserConfigurationException, IOException {
		RNSPath root = RNSSpace
				.createNewSpace(baseURL + "/EnhancedRNSPortType");
		ICallingContext ctxt = ContextManager.bootstrap(root);
		DeploymentName deploymentName = new DeploymentName();
		ConnectTool.connect(ctxt, deploymentName);
		String msg = "Storing configuration to \"" + filename + "\".";
		stdout.println(msg);
		_logger.info(msg);

		ContextFileSystem.store(new File(filename), null, ctxt);
	}
}