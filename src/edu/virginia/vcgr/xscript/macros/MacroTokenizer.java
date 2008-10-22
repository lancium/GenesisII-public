package edu.virginia.vcgr.xscript.macros;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroTokenizer
{
	static private final Pattern PATTERN = Pattern.compile(
		"((?:(?<!\\\\)\\$\\{)|(?:(?<!\\\\)\\}))");

	private String _input;
	private Matcher _matcher;
	private MatchResult _lastResult = null;
	private int _start = 0;
	
	public MacroTokenizer(String input)
	{
		_input = input;
		_matcher = PATTERN.matcher(input);
	}
	
	public String nextToken()
	{
		if (_lastResult != null)
		{
			String ret = _lastResult.group();
			_lastResult = null;
			return ret;
		}
		
		if (!_matcher.find())
		{
			if (_start < (_input.length()))
			{
				String ret = _input.substring(_start);
				_start = _input.length();
				return ret;
			}
			
			return null;
		}
		
		_lastResult = _matcher.toMatchResult();
		if (_lastResult.start() > _start)
		{
			String ret = _input.substring(_start, _lastResult.start());
			_start = _lastResult.end();
			return ret;
		} else
		{
			String ret = _lastResult.group();
			_start = _lastResult.end();
			_lastResult = null;
			return ret;
		}
	}
	
	static public void main(String []args)
	{
		MacroTokenizer tokenizer = new MacroTokenizer(
			"Mark ${LAST} was ${one${var}two} ${${COUNT}}.");
		
		String next;
		while ( (next = tokenizer.nextToken()) != null)
		{
			System.out.println(next);
		}
	}
}