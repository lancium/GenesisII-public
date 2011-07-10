package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolDescription;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class HelpTool extends BaseGridTool
{
	static private final String _DESCRIPTION = 
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dhelp";
	static private final String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uhelp";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/help";
	

	private Map<String, ToolDescription> _tools;
	private boolean _verbose = false;

	public HelpTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), 
				true, ToolCategory.GENERAL);
		addManPage(new FileResource(_MANPAGE));
	}

	@Option({"verbose", "v"})
	public void setVerbose()
	{
		_verbose = true;
	}

	public int runCommand()
	throws Throwable
	{
		_tools = CommandLineRunner.getToolList(
				ConfigurationManager.getCurrentConfiguration().getClientConfiguration());

		List<String> arguments = getArguments();
		if (arguments.size() == 0)
		{

			stdout.println("Available tools:");

			SortedMap<String, ToolCategory> categoryMap = 
				new TreeMap<String, ToolCategory>();

			for (ToolCategory cat : ToolCategory.values()){
				categoryMap.put(cat.getDescription(), cat);
			}

			for (ToolCategory cat : categoryMap.values()){
				printCategory(cat);
			}


		} else
		{

			String tool = getArguments().get(0);
			ToolDescription description = _tools.get(tool);
			if (description == null)
				stderr.println("Tool \"" + tool + "\" is unknown.");
			else{
				if (_verbose)
					ManTool.printManPage(description, stdout);
				else
					printHelp(description, stdout);
			}
		}

		return 0;
	}


	protected static void printHelp(ToolDescription description, PrintWriter out) throws ToolException{
		out.println(description.getToolDescription());
		out.println();
		out.println(description.getUsage());
	}

	private void printCategory(ToolCategory cat) throws Throwable{
		if (!cat.isHidden()){
			SortedSet<String> toolNames = new TreeSet<String>();

			for(Entry<String, ToolDescription> tool : _tools.entrySet()){
				if (tool.getValue().getCategory().equals(cat)){
					toolNames.add(tool.getKey());
				}
			}

			if(toolNames.size() > 0){
				stdout.println(cat.getDescription());
				printTools(toolNames);
			}

		}


	}

	private void printTools(Iterable<String> toolNames) throws Throwable{
		for (String tool : toolNames)
		{
			ToolDescription description = _tools.get(tool);
			if (description != null && !description.isHidden())
				stdout.println("\t" + tool);
			
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if ((getArguments().size() > 1) || (getArguments().size() < 0)){
			throw new InvalidToolUsageException(); 
		}
	}
}