package edu.virginia.vcgr.genii.ui.shell.grid;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import java.lang.reflect.AccessibleObject;

import edu.virginia.vcgr.genii.client.cmd.CommandLineFormer;
import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.ToolDescription;
import edu.virginia.vcgr.genii.client.cmd.tools.Option;
import edu.virginia.vcgr.genii.client.cmd.tools.OptionSetter;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.IContextResolver;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.shell.Display;
import edu.virginia.vcgr.genii.ui.shell.ExecutionContext;
import edu.virginia.vcgr.genii.ui.shell.WordCompleter;

public class GridExecutionContext implements ExecutionContext
{
	private ICallingContext _callingContext;
	private CommandLineRunner _runner;
	private SortedSet<String> _sortedCommands;
	private Map<String, ToolDescription> _tools;
	
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
		_tools = CommandLineRunner.getToolList(
				ConfigurationManager.getCurrentConfiguration().getClientConfiguration());
		
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
	public WordCompleter optionCompleter() 
	{
		return new OptionCompleter();
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
		public String[] completions(String originalPartial) throws Exception
		{
			Collection<String> ret = new LinkedList<String>();
			String directory;
			String partialFile;
			
			GeniiPath path = new GeniiPath(originalPartial);
			String partial = path.path();
			
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
			
			if (path.pathType() == GeniiPathType.Grid)
			{
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
							TypeInformation type = new TypeInformation(entry.getEndpoint());
							ret.add(originalPartial + entryName.substring(
								partialFile.length()) +
								(type.isRNS() ? "/" : ""));
						}
					}
					
					return ret.toArray(new String[ret.size()]);
				}
				finally
				{
					_callingContext = ContextManager.getCurrentContext();
					ContextManager.setResolver(resolver);
				}
			} else
			{
				File directoryPath = new File(directory);
				for (File entry : directoryPath.listFiles())
				{
					String entryName = entry.getName();
					
					if (entryName.startsWith(partialFile))
					{
						ret.add(originalPartial + entryName.substring(
							partialFile.length()) +
							(entry.isDirectory() ? "/" : ""));
					}
				}
				
				return ret.toArray(new String[ret.size()]);
			}
		}
	}
	
	private class OptionCompleter implements WordCompleter
	{

		@Override
		public String[] completions(String partial) throws Exception {
			
			String stem;
			int last = partial.lastIndexOf(" ");
			if(partial.contains("="))
			{
				String lastWord = partial.substring(last+1);
				if(lastWord.contains("="))
				{
					int eql = lastWord.indexOf('=');
					stem = lastWord.substring(0,eql+1);
					PathCompleter comp = new PathCompleter();
					String paths[];
					if(eql+1 < lastWord.length())
						paths = comp.completions(lastWord.substring(
								eql+1));
					else
						paths = comp.completions("");
					Collection<String> ret = new ArrayList<String>();
					for (String str: paths)
					{
						ret.add(stem + str);
					}
					return ret.toArray(new String[ret.size()]);
				}
			}
			
			boolean isLong;
			char temp;
			
			try
			{
				temp = partial.charAt(last + 2);
			}
			catch (IndexOutOfBoundsException iobe)
			{
				temp=' ';
			}
			isLong = (temp == '-');
			try
			{
				if(isLong)
					stem = partial.substring(last+3);
				else
					stem = partial.substring(last+2);
			}
			catch (IndexOutOfBoundsException iobe)
			{
				stem = "";
			}
			
			int index = partial.indexOf(' ');
			String cmd = partial.substring(0, index);
			ToolDescription desc = _tools.get(cmd);
			AccessibleObject options[] = new OptionSetter(
					desc.getToolInstance()).getOptions();
			ArrayList<String> ret = new ArrayList<String>();
			int shortsFound = 0;
			int shortsToFind = 0;
			char[] shorts = {};
			if(!isLong && stem.length() > 0)
			{
				shortsToFind = stem.length();
				shorts = stem.toCharArray();
			}
		
			
			for (AccessibleObject opt : options)
			{
				Option option = opt.getAnnotation(Option.class);
				if(option == null)
					continue;
				String[] value = option.value();
				for(String val : value)
				{
					if(shortsToFind > shortsFound && val.length() == 1)
						for(char c : shorts)
						{
							if(val.toCharArray()[0] == c)
							{
								shortsFound++;
								break;
							}
						}
					if(!isLong)
					{
						if(val.length() == 1 && !stem.contains(val))
							ret.add("-" + stem + val);
						else if(stem.equals("") && val.length() > 1)
						{
							ret.add("--" + val);
						}
					}
					else if(val.startsWith(stem) && val.length() > 1)
						ret.add("--" + val);
				}
			}
			if(shortsToFind > 0)
				if(shortsFound < shortsToFind)
					ret.clear();
			
			return ret.toArray(new String[ret.size()]);
		}
		
	}
}