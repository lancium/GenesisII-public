package edu.virginia.vcgr.genii.container.q2.matching;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

class OrMatchingParameter extends MatchingParameter
{
	private Collection<MatchingParameter> _parameters =
		new LinkedList<MatchingParameter>();
	
	
	OrMatchingParameter()
	{
	}
	
	final void addMatchingParameter(MatchingParameter parameter)
	{
		_parameters.add(parameter);
	}
	
	@Override
	final boolean matches(Map<String, Collection<String>> besParameters)
	{
		for (MatchingParameter parameter : _parameters)
		{
			if (!parameter.matches(besParameters))
				return false;
		}
		
		return true;
	}
	
	@Override
	public String toString()
	{
		if (_parameters.size() == 0)
			return "";
		else if (_parameters.size() == 1)
			return _parameters.iterator().next().toString();
		else
		{
			StringBuilder ret = new StringBuilder();
			for (MatchingParameter parameter : _parameters)
			{
				if (ret.length() > 0)
					ret.append(" | ");
				
				ret.append(parameter);
			}
			
			return String.format("(%s)", ret);
		}
	}
}