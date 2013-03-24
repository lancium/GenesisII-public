package edu.virginia.vcgr.genii.container.q2.matching;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultMatchingParameter extends MatchingParameter
{
	static private Log _logger = LogFactory.getLog(DefaultMatchingParameter.class);

	private String _property;
	private String _value;

	private MatchingParamEnum _paramType;
	private boolean _fromJSDL;

	public boolean isRequired()
	{
		return _paramType == MatchingParamEnum.requires;
	}

	public DefaultMatchingParameter(String property, String value, Boolean fromJSDL)
	{
		checkProperty(property);
		_value = value;
		_fromJSDL = fromJSDL;
	}

	private void checkProperty(String property)
	{
		int index = property.indexOf(':');
		if (index > 0) {
			String enumValue = property.substring(0, index);
			_property = property.substring(index + 1);

			_paramType = MatchingParamEnum.valueOf(enumValue);
		} else {
			_property = property;
			if (_fromJSDL)
				_paramType = MatchingParamEnum.requires;
			else
				_paramType = MatchingParamEnum.supports;
		}
	}

	@Override
	final boolean matches(MatchingParameter param)
	{
		if (_property == null) {
			_logger.warn("Found a matching parameter property with no name.");
			return true;
		}

		if (_value == null)
			if (_logger.isTraceEnabled())
				_logger.trace("Found a matching parameter property with no " + "value...assuming it's a set test.");

		if (param instanceof OrMatchingParameter)
			return param.matches(this);
		else if (param instanceof DefaultMatchingParameter) {
			DefaultMatchingParameter other = (DefaultMatchingParameter) param;
			return (_property.equals(other.getProperty()) && _value.equals(other.getValue()));
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		int ret = _paramType.hashCode();

		if (_property != null)
			ret ^= _property.hashCode();

		if (_value != null)
			ret ^= _value.hashCode();

		return ret;
	}

	private String getProperty()
	{
		return _property;
	}

	private String getValue()
	{
		return _value;
	}

	public boolean equals(DefaultMatchingParameter other)
	{
		return (_property.equals(other.getProperty()) && _value.equals(other.getValue()) && _paramType == other._paramType);
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof DefaultMatchingParameter)
			return equals((DefaultMatchingParameter) other);

		return false;
	}

	@Override
	public String toString()
	{
		// return String.format("%s = %s", _property, _value);
		return (_paramType.toString() + ":" + _property + " = " + _value);
	}

	@Override
	boolean matches(Collection<MatchingParameter> params)
	{
		if (_property == null) {
			_logger.warn("Found a matching parameter property with no name.");
			return true;
		}

		if (_value == null)
			if (_logger.isTraceEnabled())
				_logger.trace("Found a matching parameter property with no " + "value...assuming it's a set test.");

		for (MatchingParameter p : params) {
			if (this.matches(p))
				return true;
		}
		return false;

	}

	@Override
	public edu.virginia.vcgr.genii.common.MatchingParameter toAxisType()
	{
		return new edu.virginia.vcgr.genii.common.MatchingParameter(String.format("%s:%s", _paramType, _property), _value);
	}
}