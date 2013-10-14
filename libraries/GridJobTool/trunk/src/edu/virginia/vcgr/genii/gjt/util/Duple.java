package edu.virginia.vcgr.genii.gjt.util;

public class Duple<Type1, Type2>
{
	private Type1 _type1;
	private Type2 _type2;

	public Duple(Type1 type1, Type2 type2)
	{
		_type1 = type1;
		_type2 = type2;
	}

	final public Type1 first()
	{
		return _type1;
	}

	final public Type2 second()
	{
		return _type2;
	}
}