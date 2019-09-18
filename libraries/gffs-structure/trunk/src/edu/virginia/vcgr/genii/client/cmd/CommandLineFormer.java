package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;

public class CommandLineFormer
{
	// 2019-09-18 by ASg. Updated to allow not expanding variables in the command line.
	static public String[] formCommandLine(String line) throws FileNotFoundException, IOException
	{
		return formCommandLine(line,true);
	}

	static public String[] formCommandLine(String line, boolean expandVariables) throws FileNotFoundException, IOException
	{
		boolean insideQuotes = false;
		StringBuilder builder = null;
		Collection<String> ret = new ArrayList<String>();
		if (expandVariables) line = GridUserEnvironment.replaceVariables(GridUserEnvironment.getGridUserEnvironment(), line);
		for (int begin = 0; begin < line.length(); begin++) {
			char c = line.charAt(begin);
			if (c == '"') {
				if (!insideQuotes && (builder == null))
					builder = new StringBuilder();
				insideQuotes = !insideQuotes;
			} else if (insideQuotes) {
				builder.append(c);
			} else if (Character.isWhitespace(c)) {
				if (builder != null) {
					ret.add(builder.toString());
					builder = null;
				}
			} else {
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