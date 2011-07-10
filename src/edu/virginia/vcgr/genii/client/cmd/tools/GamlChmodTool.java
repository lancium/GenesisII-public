package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.ArrayList;

import org.morgan.util.cmdline.CommandLine;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.io.ReducedCommandLine;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclAuthZClientTool;
import edu.virginia.vcgr.genii.client.gpath.*;

public class GamlChmodTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dchmod";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uchmod";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/chmod";
	
	protected ArrayList<String> commandLine = new ArrayList<String>();
	
	public GamlChmodTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), 
				false, ToolCategory.SECURITY);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Override
	public void addArgument(String argument) throws ToolException
	{
		commandLine.add(argument);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		parseCommandLine();
		CommandLine cLine = new CommandLine(commandLine);
		ReducedCommandLine reducedCline = 
			new ReducedCommandLine(cLine, 1);

		// create an instance of the ACL client tool
		AclAuthZClientTool clientTool = new AclAuthZClientTool();

		// create a proxy to the target
		GeniiPath gPath = new GeniiPath(cLine.getArgument(0));
		RNSPath path = lookup(
				gPath, 
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
	
	//Deals with new syntax for local and grid paths.
	private void parseCommandLine() throws
	InvalidToolUsageException
	{
		int size = commandLine.size();
		for ( int i = 0; i < size; i++)
		{
			if(commandLine.get(i).equals("--local-src"))
				throw new InvalidToolUsageException("--local-src flag is no longer supported.  " +
					"Use 'local:' to indicate a local path. ");
			GeniiPath gPath = new GeniiPath(commandLine.get(i));
			if(i != 2)
			{
				if(gPath.pathType() != GeniiPathType.Grid)
					throw new InvalidToolUsageException("Unexpected local path. ");
			}
			else
				if(gPath.pathType() == GeniiPathType.Local)
					commandLine.add("--local-src");
			commandLine.set(i, gPath.path());
		}
	}
}