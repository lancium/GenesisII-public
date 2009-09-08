package edu.virginia.vcgr.genii.ui.shell.grid;

import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.cmd.CommandLineFormer;
import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.ToolDescription;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.shell.Display;
import edu.virginia.vcgr.genii.ui.shell.ExecutionContext;
import edu.virginia.vcgr.genii.ui.shell.WordCompleter;

public class GridExecutionContext implements ExecutionContext
{
	private ICallingContext _callingContext;
	private CommandLineRunner _runner;
	private SortedSet<String> _sortedCommands;
	
	private void executeGridCommand(String []cLine, Display display, Reader stdin) 
		throws Exception
	{
		IContextResolver resolver = ContextManager.getResolver();
		
		try
		{
			ContextManager.setResolver(
				new MemoryBasedContextResolver(_callingContext));
			_runner.runCommand(cLine, display.output(), display.error(), stdin);
		}
		catch (Exception e)
		{
			throw e;
		}
		catch (Throwable cause)
		{
			throw new RuntimeException("Unable to execute command.", cause);
		}
		finally
		{
			_callingContext = ContextManager.getCurrentContext();
			ContextManager.setResolver(resolver);
		}
	}
	
	public GridExecutionContext(ICallingContext callingContext)
	{
		_callingContext = callingContext;
		_runner = new CommandLineRunner();
		_sortedCommands = new TreeSet<String>();
		
		for (String toolName : _runner.getToolList().keySet())
		{
			ToolDescription description = _runner.getToolList().get(toolName);
			try
			{
				if (!description.isHidden())
					_sortedCommands.add(toolName);
			}
			catch (Throwable cause)
			{
				// Just skip it.
			}
		}
		
		_sortedCommands.add("exit");
		_sortedCommands.add("quit");
	}
	
	@Override
	public WordCompleter commandCompleter()
	{
		return new CommandCompleter();
	}

	@Override
	public WordCompleter pathCompleter()
	{
		return new PathCompleter();
	}
	
	@Override
	public void executeCommand(String commandLine, Display display, Reader stdin)
		throws Exception
	{
		String []cLine = CommandLineFormer.formCommandLine(commandLine);
		if (cLine.length == 0)
			return;
		
		executeGridCommand(cLine, display, stdin);
	}
	
	private class CommandCompleter implements WordCompleter
	{
		@Override
		public String[] completions(String partial)
		{
			Collection<String> completions = new Vector<String>(
				_sortedCommands.size());
			for (String cmd : _sortedCommands)
			{
				if (cmd.startsWith(partial))
					completions.add(cmd);
			}
			
			return completions.toArray(new String[completions.size()]);
		}	
	}
	
	private class PathCompleter implements WordCompleter
	{
		@Override
		public String[] completions(String partial) throws Exception
		{
			Collection<String> ret = new LinkedList<String>();
			String directory;
			String partialFile;
			
			int index = partial.lastIndexOf('/');
			if (index < 0)
			{
				directory = ".";
				partialFile = partial;
			} else
			{
				directory = partial.substring(0, index);
				partialFile = partial.substring(index + 1);
			}
			
			if (directory.length() == 0)
				directory = "/";
			
			IContextResolver resolver = ContextManager.getResolver();
			
			try
			{
				ContextManager.setResolver(
					new MemoryBasedContextResolver(_callingContext));
				RNSPath directoryPath = RNSPath.getCurrent().lookup(directory);
				for (RNSPath entry : directoryPath.listContents())
				{
					String entryName = entry.getName();
					
					if (entryName.startsWith(partialFile))
					{
						ret.add(partial + entryName.substring(
							partialFile.length()));
					}
				}
				
				return ret.toArray(new String[ret.size()]);
			}
			finally
			{
				_callingContext = ContextManager.getCurrentContext();
				ContextManager.setResolver(resolver);
			}
		}
	}
}