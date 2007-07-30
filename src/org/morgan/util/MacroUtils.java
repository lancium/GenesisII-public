package org.morgan.util;

import java.util.Properties;

public class MacroUtils
{
	static public String replaceMacros(Properties variables, String source)
	{
		int lcv;
		int start = -1;
		int end;
		int len = source.length();
		StringBuffer buffer = new StringBuffer(len * 2);
		String macroName;
		
		for (lcv = 0; lcv < len; lcv++)
		{
			char c = source.charAt(lcv);
			if (start < 0)
			{
				if (c == '$')
					start = lcv;
				else
					buffer.append(c);
			} else if (start + 1 == lcv)
			{
				if (c != '{')
				{
					buffer.append('$');
					buffer.append(c);
				}
			} else
			{
				if (c == '}')
				{
					end = lcv;
					macroName = source.substring(start + 2, end);
					buffer.append(variables.getProperty(macroName));
					start = -1;
				}
			}
		}
		
		if (start >= 0)
			buffer.append(source.substring(start));
		
		return buffer.toString();
	}
}