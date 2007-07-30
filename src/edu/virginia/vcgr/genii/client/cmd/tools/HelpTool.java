package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.ToolDescription;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class HelpTool extends BaseGridTool
{
	static private final String _DESCRIPTION = 
		"Prints out the usages of a tool, or the list of tools available.";
	static private final String _USAGE =
		"help [command...]";
	
	public HelpTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	public int runCommand()
			throws Throwable
	{
		Map<String, ToolDescription> tools = CommandLineRunner.getToolList(
			ConfigurationManager.getCurrentConfiguration().getClientConfiguration());
		
		List<String> arguments = getArguments();
		if (arguments.size() == 0)
		{
			SortedSet<String> toolNames = new TreeSet<String>(
				tools.keySet());
			stdout.println("Available tools:");
			for (String tool : toolNames)
			{
				ToolDescription description = tools.get(tool);
				if (description != null && !description.isHidden())
					stdout.println("\t" + tool);
			}
		} else
		{
			for (String tool : arguments)
			{
				ToolDescription description = tools.get(tool);
				if (description == null)
					stderr.println("Tool \"" + tool + "\" is unknown.");
				else
					stdout.println(description.getUsage());
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
}