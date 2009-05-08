package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.ArrayList;

import org.morgan.util.cmdline.CommandLine;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.io.ReducedCommandLine;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclAuthZClientTool;

public class GamlChmodTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Sets read/write/execute GAML authZ permissions for a target.";
	static final private String _USAGE =
		"chmod <target> " + AclAuthZClientTool.CHMOD_SYNTAX;
	
	protected ArrayList<String> commandLine = new ArrayList<String>();
	
	public GamlChmodTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	public void addArgument(String argument) throws ToolException
	{
		commandLine.add(argument);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		CommandLine cLine = new CommandLine(commandLine);
		ReducedCommandLine reducedCline = 
			new ReducedCommandLine(cLine, 1);

		// create an instance of the ACL client tool
		AclAuthZClientTool clientTool = new AclAuthZClientTool();

		// create a proxy to the target
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(
				cLine.getArgument(0), 
				RNSPathQueryFlags.MUST_EXIST);
		
		GenesisIIBaseRP rp =  
			(GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
				path.getEndpoint(), GenesisIIBaseRP.class);
		
		// get the authz config from the target's attributes
		AuthZConfig config = null;
		config = rp.getAuthZConfig();
		if (config == null) {
			config = clientTool.getEmptyAuthZConfig();
		}
		
		config = clientTool.chmod(reducedCline, config);
		
		// upload new authz config to resource
		rp.setAuthZConfig(config);
		
		return 0;
	}
	
	@Override
	protected void verify() throws ToolException
	{
		// create an instance of the ACL client tool
		AclAuthZClientTool clientTool = new AclAuthZClientTool();

		CommandLine cLine = new CommandLine(commandLine);
		ReducedCommandLine reducedCline = 
			new ReducedCommandLine(cLine, 1);
		if (!clientTool.validateChmodSyntax(reducedCline)) 
		{
			throw new InvalidToolUsageException();
		}
		
	}
}