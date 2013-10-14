package edu.virginia.vcgr.genii.gjt.util;

public class Triple<Type1, Type2, Type3>
{
	private Type1 _type1;
	private Type2 _type2;
	private Type3 _type3;

	public Triple(Type1 first, Type2 second, Type3 third)
	{
		_type1 = first;
		_type2 = second;
		_type3 = third;
	}

	final public Type1 first()
	{
		return _type1;
	}

	final public Type2 second()
	{
		return _type2;
	}

	final public Type3 third()
	{
		return _type3;
	}
}
