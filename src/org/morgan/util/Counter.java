package org.morgan.util;

public class Counter
{
	private int _counter;
	
	public Counter(int initial)
	{
		_counter = initial;
	}
	
	public Counter()
	{
		this(0);
	}
	
	final public void modify(int delta)
	{
		_counter += delta;
	}
	
	final public int get()
	{
		return _counter;
	}
}