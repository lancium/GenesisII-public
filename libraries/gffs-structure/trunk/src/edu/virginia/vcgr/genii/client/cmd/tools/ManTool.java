package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolDescription;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class ManTool extends BaseGridTool
{

	static private final String _DESCRIPTION = "config/tooldocs/description/dman";
	static private final String _USAGE = "config/tooldocs/usage/uman";
	static final private String _MANPAGE = "config/tooldocs/man/man";

	public ManTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), true, ToolCategory.GENERAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected void verify() throws ToolException
	{
		if (getArguments().size() != 1)
			throw new InvalidToolUsageException();

	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		Map<String, ToolDescription> _tools =
			CommandLineRunner.getToolList(ConfigurationManager.getCurrentConfiguration().getClientConfiguration());

		String tool = getArguments().get(0);
		ToolDescription description = _tools.get(tool);
		if (description == null)
			stderr.println("Tool \"" + tool + "\" is unknown.");
		else {
			printManPage(description, stdout);
		}

		return 0;
	}

	protected static void printManPage(ToolDescription description, PrintWriter out) throws ToolException
	{
		out.println(description.getUsage());
		out.println();
		if (description.getManPage() == null)
			out.println("No manpage for this tool");
		else
			out.println(description.getManPage());
	}
}
