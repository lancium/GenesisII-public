package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclAuthZClientTool;

public class AuthZTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dauthz";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uauthz";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/authz";
	
	public AuthZTool()
	{
		super(new FileResource(_DESCRIPTION), 
				new FileResource(_USAGE), false, ToolCategory.SECURITY);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath path = lookup(new GeniiPath(getArgument(0)),
			RNSPathQueryFlags.MUST_EXIST);
		
		// get the authz config from the target's attributes
		GenesisIIBaseRP rp = (GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
			path.getEndpoint(), GenesisIIBaseRP.class);
		AuthZConfig config = rp.getAuthZConfig();

		boolean done = false;
		while (!done)
		{
			// display retrieved authz config
			stdout.println("Authorization configuration for " + getArgument(0) + ":\n");

			if ((config == null) || (config.get_any() == null)) {
				stdout.println("No authorization module set.");
			} else {
				AclAuthZClientTool.displayAuthZConfig(config, stdout, stderr, stdin);
			}

			boolean chosen = false;
			while (!chosen)
			{
				stdout.println("\nOptions:");
				stdout.println("  [1] Modify AuthZ config");
				stdout.println("  [2] Submit AuthZ config");
				stdout.println("  [3] Cancel");
				stdout.print("Please make a selection: ");
				stdout.flush();
				String input = stdin.readLine();
				if (input == null)
					return 0;
				stdout.println();
				int choice = 0;					
				try {
					choice = Integer.parseInt(input);
				} catch (NumberFormatException e) {
					stdout.println("Invalid choice.");
					continue;
				}
				switch (choice) {
				case 1: 
					config = AclAuthZClientTool.modifyAuthZConfig(config, stdout, stderr, stdin);
					if (config == null)
						return 0;
					chosen = true;
					break;
				case 2: 
					rp.setAuthZConfig(config);
					chosen = true;
					done = true;
					break;
				case 3:
					chosen = true;
					done = true;
					break;
				default: 
					stdout.println("Invalid choice.");
				}
			}
		}
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}