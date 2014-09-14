package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;

public class SettableLong extends DefaultDataItem implements PostUnmarshallListener
{
	@XmlAttribute(name = "value")
	private Long _value;

	public SettableLong(Long value, ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		_value = value;

		if (pBroker != null)
			addParameterizableListener(pBroker);
		if (mBroker != null)
			addModificationListener(mBroker);
	}

	public SettableLong(ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		this(null, pBroker, mBroker);
	}

	public SettableLong(Long value)
	{
		this(value, null, null);
	}

	public SettableLong()
	{
		this(null);
	}

	final public Long value()
	{
		return _value;
	}

	final public void value(Long value)
	{
		if (_value == null) {
			if (value != null) {
				_value = value;
				fireJobDescriptionModified();
			}
		} else {
			if (value == null || !_value.equals(value)) {
				_value = value;
				fireJobDescriptionModified();
			}
		}
	}

	final public boolean equals(SettableLong other)
	{
		if (_value == null) {
			if (other._value == null)
				return true;
			else
				return false;
		} else {
			if (other._value == null)
				return false;

			return _value.equals(other._value);
		}
	}

	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof SettableLong)
			return equals((SettableLong) other);

		return false;
	}

	@Override
	public String toString()
	{
		return (_value == null) ? "" : _value.toString();
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker, ModificationBroker modificationBroker)
	{
		fireJobDescriptionModified();
	}
}