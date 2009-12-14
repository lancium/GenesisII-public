package edu.virginia.vcgr.genii.client.nativeq.execution;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PatternParserProcessStreamSink implements ProcessStreamSink
{
	private Map<Pattern, List<Matcher>> _patternMap;
	
	PatternParserProcessStreamSink(Map<Pattern, List<Matcher>> patternMap)
	{
		_patternMap = patternMap;
	}
	
	@Override
	final public void addOutputLine(String outputLine) throws IOException
	{
		for (Pattern pattern : _patternMap.keySet())
		{
			Matcher matcher = pattern.matcher(outputLine);
			if (matcher.matches())
				_patternMap.get(pattern).add(matcher);
		}
	}
}