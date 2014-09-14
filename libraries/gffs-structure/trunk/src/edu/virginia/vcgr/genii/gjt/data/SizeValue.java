package edu.virginia.vcgr.genii.gjt.data;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;
import edu.virginia.vcgr.genii.gjt.data.xml.PostUnmarshallListener;
import edu.virginia.vcgr.genii.gjt.units.SizeUnit;

public class SizeValue extends DefaultDataItem implements UnitValue<Long, SizeUnit>, PostUnmarshallListener
{
	@XmlAttribute(name = "value")
	private Long _value;

	@XmlAttribute(name = "units")
	private SizeUnit _units;

	public SizeValue()
	{
		_value = null;
		_units = SizeUnit.Bytes;
	}

	public SizeValue(ParameterizableBroker pBroker, ModificationBroker mBroker)
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
	final public SizeUnit units()
	{
		return _units;
	}

	@Override
	final public void units(SizeUnit units)
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