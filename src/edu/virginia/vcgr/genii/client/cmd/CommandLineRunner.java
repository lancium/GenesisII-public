package edu.virginia.vcgr.genii.client.cmd;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.tools.HelpTool;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class CommandLineRunner
{	
	private Map<String, ToolDescription> _tools;
	
	public CommandLineRunner()
	{
		_tools = getToolList(
			ConfigurationManager.getCurrentConfiguration().getClientConfiguration());
	}
	
	public int runCommand(String []cLine, 
		PrintStream out, PrintStream err, BufferedReader in) throws Throwable
	{
		int resultSoFar = 0;
		
		if (cLine == null || cLine.length == 0)
		{
			cLine = new String[] { "help" };
			resultSoFar = 1;
			out = err;
		}
		
		ToolDescription desc = _tools.get(cLine[0]);
		if (desc == null)
			throw new ToolException("Couldn't find tool \"" + cLine[0] + "\".");
		
		ITool instance = desc.getToolInstance();
		for (int lcv = 1; lcv < cLine.length; lcv++)
			instance.addArgument(cLine[lcv]);
		return Math.max(resultSoFar, instance.run(out, err, in));
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
}