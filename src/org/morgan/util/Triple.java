package org.morgan.util;

public class Triple<Type1, Type2, Type3>
{
	private Type1 _first;
	private Type2 _second;
	private Type3 _third;
	
	public Triple(Type1 first, Type2 second, Type3 third)
	{
		_first = first;
		_second = second;
		_third = third;
	}
	
	final public void first(Type1 newValue)
	{
		_first = newValue;
	}
	
	final public Type1 first()
	{
		return _first;
	}
	
	final public void second(Type2 newValue)
	{
		_second = newValue;
	}
	
	final public Type2 second()
	{
		return _second;
	}
	
	final public void third(Type3 newValue)
	{
		_third = newValue;
	}
	
	final public Type3 third()
	{
		return _third;
	}
	
	final public boolean equals(Triple<Type1, Type2, Type3> other)
	{
		return _first.equals(other._first) && _second.equals(other._second) &&
			_third.equals(other._third);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof Triple)
			return equals((Triple<Type1, Type2, Type3>)other);
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return String.format("{%s, %s, %s}", _first, _second, _third);
	}
}
