package org.morgan.util.file;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePattern
{
	static final private String FILE_PATTERN_STRING= "(\\\\?[?*+])";
	
	static final private Pattern FILE_PATTERN_PATTERN =
		Pattern.compile(FILE_PATTERN_STRING);
	
	
	static public Pattern compile(String filePattern)
	{
		boolean quoting = false;
		Collection<Segment> segments = new ArrayList<Segment>();
		int lastEnd = 0;
		Matcher matcher = FILE_PATTERN_PATTERN.matcher(filePattern);
		while (matcher.find())
		{
			String group = matcher.group(1);
			segments.add(new Segment(filePattern.substring(
				lastEnd, matcher.start()), true));
			if (group.startsWith("\\"))
				segments.add(new Segment(group.substring(1), true));
			else
				segments.add(new Segment("." + group, false));
			lastEnd = matcher.end();
		}
		
		segments.add(new Segment(filePattern.substring(lastEnd), true));
		StringBuilder builder = new StringBuilder("^");
		for (Segment seg : segments)
		{
			String str = seg.toString();
			if (str.length() == 0)
				continue;
			
			if (seg.quoted())
			{
				if (quoting)
					builder.append(str);
				else
				{
					builder.append("\\Q" + str);
					quoting = true;
				}
			} else
			{
				if (quoting)
				{
					builder.append("\\E" + str);
					quoting = false;
				} else
					builder.append(str);
			}
		}
		
		if (quoting)
			builder.append("\\E");
		builder.append("$");
		
		return Pattern.compile(builder.toString());
	}
		
	static private class Segment
	{
		private String _value;
		private boolean _quoted;
		
		public Segment(String value, boolean quoted)
		{
			_value = value;
			_quoted = quoted;
		}
		
		public String toString()
		{
			return _value;
		}
		
		public boolean quoted()
		{
			return _quoted;
		}
	}
	
	static public void main(String []args) throws Throwable
	{
		System.err.println("Pattern for file pattern * is:  \"" + compile("*") + "\".");
	}

	static public boolean isFilterNeeded(String pattern) 
	{
		
		int len = FILE_PATTERN_STRING.length();	//loop-invariant
		for(int lcv=0; lcv<len; ++lcv)
		{
			if(pattern.indexOf(new String(FILE_PATTERN_STRING.substring(lcv, lcv+1)))!=-1)
				return true;
		}
		
		return false;
	}
}