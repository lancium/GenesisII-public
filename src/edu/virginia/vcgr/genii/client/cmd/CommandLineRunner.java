package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.tools.HelpTool;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class CommandLineRunner
{	
	private Map<String, ToolDescription> _tools;
	
	public CommandLineRunner()
	{
		_tools = getToolList(
			ConfigurationManager.getCurrentConfiguration().getClientConfiguration());
	}
	
	static private Writer openRedirect(String redirectTarget) 
		throws IOException, RNSException
	{
		if (redirectTarget.startsWith("file:"))
			return new FileWriter(redirectTarget.substring(5));
		else if (redirectTarget.startsWith("rns:"))
		{
			return new OutputStreamWriter(
				ByteIOStreamFactory.createOutputStream(
					RNSPath.getCurrent().lookup(redirectTarget.substring(4))));
		}
		
		return new FileWriter(redirectTarget);
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
}