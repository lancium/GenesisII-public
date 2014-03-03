package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

class ConstantBooleanExpression implements BooleanExpression
{
	private boolean _value;

	ConstantBooleanExpression(boolean value)
	{
		_value = value;
	}

	@Override
	final public boolean evaluate(FilesystemUsageInformation usageInformation)
	{
		return _value;
	}

	@Override
	final public String toString()
	{
		return Boolean.toString(_value);
	}
}