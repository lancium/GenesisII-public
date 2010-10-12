package edu.virginia.vcgr.genii.client.history;

import java.io.Serializable;

final public class SequenceNumber 
	implements Serializable, Comparable<SequenceNumber>
{
	static final long serialVersionUID = 0L;
	
	private int []_value;
	
	private SequenceNumber(int []value)
	{
		_value = value;
	}
	
	public SequenceNumber()
	{
		this(new int[] { 1 });
	}
	
	final public boolean isRootLevel()
	{
		return _value.length == 1;
	}
	
	final public SequenceNumber parent()
	{
		if (isRootLevel())
			return null;
		
		int []parentValue = new int[_value.length - 1];
		System.arraycopy(_value, 0, parentValue, 0, parentValue.length);
		return new SequenceNumber(parentValue);
	}
	
	final public SequenceNumber next()
	{
		int []newValue = new int[_value.length];
		System.arraycopy(_value, 0, newValue, 0, _value.length);
		newValue[newValue.length - 1]++;
		return new SequenceNumber(newValue);
	}
	
	final public SequenceNumber wrapWith(SequenceNumber parent)
	{
		int []newValue = new int[_value.length + parent._value.length];
		System.arraycopy(parent._value, 0, newValue, 0, parent._value.length);
		System.arraycopy(_value, 0,
			newValue, parent._value.length, _value.length);
		return new SequenceNumber(newValue);
	}
	
	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof SequenceNumber)
			return compareTo((SequenceNumber)other) == 0;
		
		return false;
	}
	
	@Override
	final public int hashCode()
	{
		int ret = 0x0;
		for (int value : _value)
			ret ^= (new Integer(value)).hashCode();
		
		return ret;
	}
	
	@Override
	final public String toString()
	{
		StringBuilder builder = new StringBuilder();
		for (int value : _value)
		{
			if (builder.length() > 0)
				builder.append('.');
			builder.append(Integer.toString(value));
		}
		
		return builder.toString();
	}
	
	@Override
	final public int compareTo(SequenceNumber other)
	{
		int lcv = 0;
		while (true)
		{
			if (lcv >= _value.length)
			{
				if (lcv >= other._value.length)
					return 0;
				else
					return -1;
			} else
			{
				if (lcv >= other._value.length)
					return 1;
				else
				{
					int diff = _value[lcv] - other._value[lcv];
					if (diff != 0)
						return diff;
				}
			}
			
			lcv++;
		}
	}
	
	final public boolean isFirstOfLevel()
	{
		return _value[_value.length - 1] == 1;
	}
	
	static public SequenceNumber valueOf(String representation)
	{
		String []values = representation.split("\\.");
		int []array = new int[values.length];
		for (int lcv = 0; lcv < values.length; lcv++)
			array[lcv] = Integer.parseInt(values[lcv]);
		
		return new SequenceNumber(array);
	}
}