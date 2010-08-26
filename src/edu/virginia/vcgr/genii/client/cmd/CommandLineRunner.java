package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.xml.namespace.QName;

import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.ITool;
import edu.virginia.vcgr.genii.client.cmd.ToolDescription;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.HelpTool;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.rns.RNSException;

public class CommandLineRunner
{	
	private Map<String, ToolDescription> _tools;
	private static ArrayList<String[]> _history = new ArrayList<String[]>();
	
	static private String[] editCommandLine(String []commandLine) throws FileNotFoundException, IOException
	{
		StringBuilder builder = new StringBuilder();
		for (String arg : commandLine)
		{
			if (builder.length() > 0)
				builder.append(' ');
			
			if (arg.matches("^.*\\s.*$"))
				builder.append("\"" + arg + "\"");
			else
				builder.append(arg);
		}
		
		String value = JOptionPane.showInputDialog("Edit command", 
			builder.toString());
		if (value == null)
			return null;
		
		return CommandLineFormer.formCommandLine(value);
	}
	
	public CommandLineRunner()
	{
		_tools = getToolList(
			ConfigurationManager.getCurrentConfiguration().getClientConfiguration());
	}
	
	static private Writer openRedirect(String redirectTarget) 
		throws IOException, RNSException
	{
		return new OutputStreamWriter(
			new GeniiPath(redirectTarget).openOutputStream());
	}
	
	public int runCommand(String []cLine, Writer out, Writer err, Reader in)
		throws Throwable
	{
		Writer target = out;
		int resultSoFar = 0;
		
		if (cLine == null || cLine.length == 0)
		{
			cLine = new String[] { "help" };
			resultSoFar = 1;
			out = err;
		}
		
		if (cLine[0].startsWith("!"))
		{
			String arg = cLine[0].substring(1);
			boolean edit = false;
			
			if (arg.startsWith("e"))
			{
				edit = true;
				arg = arg.substring(1);
			}
			
			int index = Integer.parseInt(arg);

			try
			{
				cLine = _history.get(index);
				if (edit)
					cLine = editCommandLine(cLine);
				if (cLine == null)
					return 0;
			}
			catch (Exception e)
			{
				throw new ToolException("Could not retrieve requested event.");
			}
		}
			
		
		if(_history.size() > 500)
			_history.remove(0);
		_history.add(cLine);
		
		
		ToolDescription desc = _tools.get(cLine[0]);
		if (desc == null)
			throw new ToolException("Couldn't find tool \"" + cLine[0] + "\".");
		
		ITool instance = desc.getToolInstance();
		try
		{
			for (int lcv = 1; lcv < cLine.length; lcv++)
			{
				String argument = cLine[lcv];
				if (argument.equals(">"))
				{
					if (++lcv >= cLine.length)
						throw new ToolException(
							"Unexpected end of command line.");
					String redirectTarget = cLine[lcv++];
					if (lcv < cLine.length)
						throw new ToolException(
							"Additional arguments found after file redirect.");
					target = openRedirect(redirectTarget);
					break;
				}
				
				instance.addArgument(argument);
			}
			
			return Math.max(resultSoFar, instance.run(target, err, in));
		}
		finally
		{
			if (target != out)
				target.close();
		}
	}
	
	final public Map<String, ToolDescription> getToolList()
	{
		return Collections.unmodifiableMap(_tools);
	}
	
	@SuppressWarnings("unchecked")
	static public Map<String, ToolDescription> 
		getToolList(XMLConfiguration conf)
	{
		HashMap<String, ToolDescription> ret;
		
		ArrayList<Object> tools;
		
		synchronized(conf)
		{
			tools = conf.retrieveSections(
				new QName(GenesisIIConstants.GENESISII_NS, "tools"));
		}
		
		ret  = new HashMap<String, ToolDescription>(tools.size());
		
		for(Object oToolMap : tools)
		{
			HashMap<String, Class> toolMap = (HashMap<String, Class>)oToolMap;
			for (String toolName : toolMap.keySet())
			{
				Class<? extends ITool> toolClass = toolMap.get(toolName);
				ret.put(toolName, new ToolDescription(toolClass, toolName));
			}
		}
		
		ret.put("help", new ToolDescription(HelpTool.class, "help"));
		
		return ret;
	}
	
	public static ArrayList<String[]> history()
	{
		return _history;
	}
	
	public static void clearHistory()
	{
		_history.clear();
	}
}