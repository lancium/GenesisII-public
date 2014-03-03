package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;
import edu.virginia.vcgr.genii.gjt.units.FriendlyTimeUnit;

public class TimeValue extends DefaultDataItem implements UnitValue<Long, FriendlyTimeUnit>, PostUnmarshallListener
{
	@XmlAttribute(name = "value")
	private Long _value;

	@XmlAttribute(name = "units")
	private FriendlyTimeUnit _units;

	public TimeValue()
	{
		_value = null;
		_units = FriendlyTimeUnit.Hours;
	}

	public TimeValue(ParameterizableBroker pBroker, ModificationBroker mBroker)
	{
		this();

		addParameterizableListener(pBroker);
		addModificationListener(mBroker);
	}

	@Override
	final public Long value()
	{
		return _value;
	}

	@Override
	final public void value(Long value)
	{
		_value = value;
		fireJobDescriptionModified();
	}

	@Override
	final public FriendlyTimeUnit units()
	{
		return _units;
	}

	@Override
	final public void units(FriendlyTimeUnit units)
	{
		_units = units;
		fireJobDescriptionModified();
	}

	@Override
	public void postUnmarshall(ParameterizableBroker parameterBroker, ModificationBroker modificationBroker)
	{
		fireJobDescriptionModified();
	}
}