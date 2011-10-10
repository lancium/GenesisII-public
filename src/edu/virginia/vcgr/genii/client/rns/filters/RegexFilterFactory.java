package edu.virginia.vcgr.genii.client.rns.filters;

import java.util.regex.Pattern;

public class RegexFilterFactory implements FilterFactory
{
	@Override
	public Filter createFilter(String pattern)
	{
		return new RegexFilter(Pattern.compile(pattern));
	}
	
	@Override
	public boolean isFilterNeeded(String pattern) {
		return true;
	}
	
	static protected class RegexFilter implements Filter
	{
		private Pattern _regex;
		
		public RegexFilter(Pattern regex)
		{
			_regex = regex;
		}
		
		public boolean matches(String text)
		{
			return _regex.matcher(text).matches();
		}
	}

	
}