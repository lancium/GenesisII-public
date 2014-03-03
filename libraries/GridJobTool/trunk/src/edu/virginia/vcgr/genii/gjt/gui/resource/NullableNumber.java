package edu.virginia.vcgr.genii.gjt.gui.resource;

public class NullableNumber
{
	private Long _value;

	public NullableNumber(Long value)
	{
		_value = value;
	}

	public NullableNumber()
	{
		this(null);
	}

	final public Long value()
	{
		return _value;
	}

	final public void value(Long value)
	{
		_value = value;
	}

	final public boolean equals(NullableNumber other)
	{
		if (_value == null) {
			if (other._value == null)
				return true;
			else
				return false;
		} else {
			if (other._value == null)
				return false;
			else
				return _value.equals(other._value);
		}
	}

	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof NullableNumber)
			return equals((NullableNumber) other);

		return false;
	}

	@Override
	final public String toString()
	{
		if (_value == null)
			return "";

		return _value.toString();
	}
}