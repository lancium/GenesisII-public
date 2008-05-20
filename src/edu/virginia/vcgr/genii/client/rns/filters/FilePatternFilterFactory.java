package edu.virginia.vcgr.genii.client.rns.filters;

import org.morgan.util.file.FilePattern;

public class FilePatternFilterFactory extends RegexFilterFactory
{
	@Override
	public Filter createFilter(String pattern)
	{
		return new RegexFilter(FilePattern.compile(pattern));
	}
}