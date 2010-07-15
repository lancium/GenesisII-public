package edu.virginia.vcgr.genii.container.q2.matching;

import java.util.Collection;
import java.util.LinkedList;

public class MatchingParameters
{
	private Collection<MatchingParameter> _matchingParameters;
	private Collection<MatchingParameter> _requiredParameters;

	public MatchingParameters()
	{
		_matchingParameters = new LinkedList<MatchingParameter>();
		_requiredParameters = new LinkedList<MatchingParameter>();
	}
	
	public MatchingParameters(Collection<MatchingParameter> params)
	{
		_matchingParameters = new LinkedList<MatchingParameter>();
		_requiredParameters = new LinkedList<MatchingParameter>();
		for(MatchingParameter p : params){
			if (p.isRequired())
				_requiredParameters.add(p);
			else
				_matchingParameters.add(p);
		}
	}
	
	public Collection<MatchingParameter> getRequired()
	{
		return _requiredParameters;
	}
	
	
	public Collection<MatchingParameter> getParameters()
	{
		Collection<MatchingParameter> tList  = new LinkedList<MatchingParameter>();
		tList.addAll(_matchingParameters);
		tList.addAll(_requiredParameters);
		return tList;
	}
	
	public void add(MatchingParameter param)
	{
		if (param.isRequired())
			_requiredParameters.add(param);
		else
			_matchingParameters.add(param);
	}
	
	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		
		for (MatchingParameter p : _requiredParameters)
		{
			result.append(p);
			result.append('\n');
		}

		for (MatchingParameter p : _matchingParameters)
		{
			result.append(p);
			result.append('\n');
		}

		return result.toString();
	}

}
