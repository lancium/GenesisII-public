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
			if (parameter.matches(besParameters))
				return true;
		}
		
		return false;
	}
	
	@Override
	final boolean supportsRequired(String parameterName, Collection<String> values)
	{
		for (MatchingParameter parameter : _parameters)
		{
			if (parameter.supportsRequired(parameterName, values))
				return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode()
	{
		int ret = 0x0;
		
		for (MatchingParameter mp : _parameters)
			ret ^= mp.hashCode();
		
		return ret;
	}
	
	/**
	 * Determines if this matching parameter set is equal to another one.
	 * 
	 * This operation is incredibly heavy weight and should not be used
	 * lightly.  It would be better if we could sort these and then do
	 * the comparison, but unfortunately, there isn't a natural sort
	 * order for matching parameters.
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(OrMatchingParameter other)
	{
		if (_parameters.size() != other._parameters.size())
			return false;
		
		if (hashCode() != other.hashCode())
			return false;
		
		return _parameters.containsAll(other._parameters);
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (other instanceof OrMatchingParameter)
			return equals((OrMatchingParameter)other);
		
		return false;
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