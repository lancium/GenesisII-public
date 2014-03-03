package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

public class DefaultLogTreeObject implements LogTreeObject {
	private LogTreeObjectType _objectType;

	protected DefaultLogTreeObject(LogTreeObjectType type) {
		_objectType = type;
	}

	@Override
	final public LogTreeObjectType objectType() {
		return _objectType;
	}

	@Override
	public String toString() {
		if (_objectType == LogTreeObjectType.ERROR_OBJECT)
			return "Error...";
		else if (_objectType == LogTreeObjectType.EXPANDING_OBJECT)
			return "Expanding...";
		else
			return "Endpoint...";
	}

	@Override
	public boolean allowsChildren() {
		return false;
	}

	static public DefaultLogTreeObject createExpandingObject() {
		return new DefaultLogTreeObject(LogTreeObjectType.EXPANDING_OBJECT);
	}

	static public DefaultLogTreeObject createErrorObject() {
		return new DefaultLogTreeObject(LogTreeObjectType.ERROR_OBJECT);
	}
}