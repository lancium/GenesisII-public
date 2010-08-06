package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class HistoryTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Display the list of up to 500 previous commands with line numbers.";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/history-usage.txt";
	
	private boolean _clear = false;
	private int _max = 500;
	
	@Option({"clear", "c"})
	public void setClear()
	{
		_clear = true;
	}
	
	public HistoryTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
	}

	@Override
	protected void verify() throws ToolException {
		if (numArguments() > 1)
			throw new InvalidToolUsageException();
	}

	@Override
	protected int runCommand() throws Throwable {
		if(_clear)
			if(getArguments().size() != 0)
				throw new InvalidToolUsageException("Too many arguments");
			else
			{
				CommandLineRunner.clearHistory();
				return 0;
			}
			
		if (numArguments() == 1)
		{
			try
			{
				_max = Integer.parseInt(getArgument(0));
			}
			catch (NumberFormatException e)
			{
				throw new InvalidToolUsageException("Argument must be a non-negative integer.");
			}
			if (_max < 0)
				throw new InvalidToolUsageException("Argument cannot be negative.");
		}
		
		ArrayList<String[]> history = CommandLineRunner.history();
		int size = history.size();
		int i = size - _max;
		if(i < 0)
			i = 0;
		while (i < size)
		{
			stdout.print(i + " ");
			String[] cLine = history.get(i);
			for(String word : cLine)
			{
				stdout.print(word + " ");
			}
			stdout.print("\n");
			i++;
		}
		stdout.println();
		return 0;
	}
	
}
