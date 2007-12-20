package edu.virginia.vcgr.genii.client.cmd;

import java.util.ArrayList;
import java.util.Collection;

public class CommandLineFormer
{
	static public String[] formCommandLine(String line) throws ToolException
	{
		boolean insideQuotes = false;
		StringBuilder builder = null;
		Collection<String> ret = new ArrayList<String>();
		
		for (int begin= 0; begin < line.length(); begin++)
		{
			char c = line.charAt(begin);
			if (c == '"')
			{
				if (!insideQuotes && (builder == null))
					builder = new StringBuilder();
				insideQuotes = !insideQuotes;
			} else if (insideQuotes) 
			{
				builder.append(c);
			} else if (Character.isWhitespace(c))
			{
				if (builder != null)
				{
					ret.add(builder.toString());
					builder = null;
				}
			} else
			{
				if (builder == null)
					builder = new StringBuilder();
				builder.append(c);
			}
		}
		
		if (builder != null)
			ret.add(builder.toString());
		
		return ret.toArray(new String[0]);
	}
}