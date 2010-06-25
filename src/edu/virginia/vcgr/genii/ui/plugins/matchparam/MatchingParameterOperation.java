package edu.virginia.vcgr.genii.ui.plugins.matchparam;

import org.morgan.util.Pair;

enum MatchingParameterOperation
{
	Add("Adding (%s, %s)"),
	Delete("Deleting (%s, %s)");
	
	private String _formatString;
	
	private MatchingParameterOperation(String formatString)
	{
		_formatString = formatString;
	}
	
	final public String toString(Pair<String, String> parameter)
	{
		return String.format(_formatString,
			parameter.first(), parameter.second());
	}
}