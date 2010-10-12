package org.morgan.util;

import java.io.Serializable;

public class Pair<Type1, Type2> implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private Type1 _first;
	private Type2 _second;
	
	public Pair(Type1 first, Type2 second)
	{
		_first = first;
		_second = second;
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
	
	final public boolean equals(Pair<Type1, Type2> other)
	{
		return _first.equals(other._first) && _second.equals(other._second);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof Pair)
			return equals((Pair<Type1, Type2>)other);
		
		return false;
	}
	
	@Override
	public String toString()
	{
		return String.format("{%s, %s}", _first, _second);
	}
}
