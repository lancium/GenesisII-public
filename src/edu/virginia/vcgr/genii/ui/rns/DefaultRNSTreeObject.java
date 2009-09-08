package edu.virginia.vcgr.genii.ui.rns;

public class DefaultRNSTreeObject implements RNSTreeObject
{
	private RNSTreeObjectType _objectType;
	
	protected DefaultRNSTreeObject(RNSTreeObjectType type)
	{
		_objectType = type;
	}
	
	@Override
	final public RNSTreeObjectType objectType()
	{
		return _objectType;
	}
	
	@Override
	public String toString()
	{
		if (_objectType == RNSTreeObjectType.ERROR_OBJECT)
			return "Error...";
		else if (_objectType == RNSTreeObjectType.EXPANDING_OBJECT)
			return "Expanding...";
		else
			return "Endpoint...";
	}
	
	@Override
	public boolean allowsChildren()
	{
		return false;
	}
	
	static public DefaultRNSTreeObject createExpandingObject()
	{
		return new DefaultRNSTreeObject(RNSTreeObjectType.EXPANDING_OBJECT);
	}
	
	static public DefaultRNSTreeObject createErrorObject()
	{
		return new DefaultRNSTreeObject(RNSTreeObjectType.ERROR_OBJECT);
	}
}