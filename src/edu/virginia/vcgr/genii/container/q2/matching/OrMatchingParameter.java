package edu.virginia.vcgr.genii.container.q2.matching;

import java.util.Collection;
import java.util.LinkedList;

class OrMatchingParameter extends MatchingParameter
{
	// Does not support nested ORs
	private Collection<DefaultMatchingParameter> _parameters;

	OrMatchingParameter()
	{
		_parameters = new LinkedList<DefaultMatchingParameter>();
	}

	final void addMatchingParameter(DefaultMatchingParameter parameter)
	{
		_parameters.add(parameter);
	}

	@Override
	final boolean matches(MatchingParameter param)
	{
		for (MatchingParameter parameter : _parameters) {
			if (parameter.matches(param))
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
	 * This operation is incredibly heavy weight and should not be used lightly. It would be better
	 * if we could sort these and then do the comparison, but unfortunately, there isn't a natural
	 * sort order for matching parameters.
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
			return equals((OrMatchingParameter) other);

		return false;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();

		for (MatchingParameter p : _parameters) {
			result.append(p);
			result.append('\n');
		}

		return result.toString();
	}

	@Override
	boolean isRequired()
	{
		// ORS always required
		return true;
	}

	@Override
	boolean matches(Collection<MatchingParameter> params)
	{
		for (MatchingParameter p : params) {
			if (this.matches(p))
				return true;
		}
		return false;
	}

	@Override
	public edu.virginia.vcgr.genii.common.MatchingParameter toAxisType()
	{
		throw new UnsupportedOperationException("Or parameters not supported for axis type!");
	}
}