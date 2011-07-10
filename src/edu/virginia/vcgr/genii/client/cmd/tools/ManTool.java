package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.PrintWriter;
import java.util.Map;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolDescription;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class ManTool extends BaseGridTool{

	static private final String _DESCRIPTION = 
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dman";
	static private final String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uman";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/man";
	
	public ManTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), 
				true, ToolCategory.GENERAL);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected void verify() throws ToolException {
		if (getArguments().size() != 1)
			throw new InvalidToolUsageException(); 
		
	}

	@Override
	protected int runCommand() throws Throwable {
		Map<String, ToolDescription> _tools = CommandLineRunner.getToolList(
				ConfigurationManager.getCurrentConfiguration().getClientConfiguration());

		String tool = getArguments().get(0);
		ToolDescription description = _tools.get(tool);
		if (description == null)
			stderr.println("Tool \"" + tool + "\" is unknown.");
		else{
			printManPage(description, stdout);
		}
		
		return 0;
	}
	
	protected static void printManPage(ToolDescription description, PrintWriter out) throws ToolException{
		out.println(description.getUsage());
		out.println();
		if (description.getManPage() == null)
			out.println("No manpage for this tool");
		else
			out.println(description.getManPage());
	}
}
