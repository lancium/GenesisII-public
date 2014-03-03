package edu.virginia.vcgr.genii.gjt.data.variables;

class VariableCounter
{
	private int _counter;

	VariableCounter(int initialValue)
	{
		_counter = initialValue;
	}

	final int modify(int delta)
	{
		_counter += delta;
		return _counter;
	}

	final int get()
	{
		return _counter;
	}

	final void set(int value)
	{
		_counter = value;
	}
}
