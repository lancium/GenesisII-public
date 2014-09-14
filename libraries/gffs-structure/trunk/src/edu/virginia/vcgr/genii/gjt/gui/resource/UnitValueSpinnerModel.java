package edu.virginia.vcgr.genii.gjt.gui.resource;

import javax.swing.AbstractSpinnerModel;

import edu.virginia.vcgr.genii.gjt.data.UnitValue;

public class UnitValueSpinnerModel<U extends Enum<U>> extends AbstractSpinnerModel
{
	private static final long serialVersionUID = 1;

	private UnitValue<Long, U> _value;

	private long _minimum;
	private long _maximum;
	private long _step;

	public UnitValueSpinnerModel(UnitValue<Long, U> initialValue, long minimum, long maximum, long step)
	{
		_value = initialValue;
		_minimum = minimum;
		_maximum = maximum;
		_step = step;
	}

	@Override
	public Object getNextValue()
	{
		Long nextValue;

		if (_value.value() == null)
			nextValue = new Long(_minimum);
		else {
			if (_value.value() <= (_maximum - _step))
				nextValue = new Long(_value.value() + _step);
			else
				nextValue = null;
		}

		return new NullableNumber(nextValue);
	}

	@Override
	public Object getPreviousValue()
	{
		Long nextValue;

		if (_value.value() == null)
			nextValue = new Long(_maximum);
		else {
			if (_value.value() >= (_minimum + _step))
				nextValue = new Long(_value.value() - _step);
			else
				nextValue = null;
		}

		return new NullableNumber(nextValue);
	}

	@Override
	public Object getValue()
	{
		return new NullableNumber(_value.value());
	}

	@Override
	public void setValue(Object value)
	{
		NullableNumber newValue = (NullableNumber) value;
		if (!newValue.equals(new NullableNumber(_value.value()))) {
			_value.value(newValue.value());
			fireStateChanged();
		}
	}
}