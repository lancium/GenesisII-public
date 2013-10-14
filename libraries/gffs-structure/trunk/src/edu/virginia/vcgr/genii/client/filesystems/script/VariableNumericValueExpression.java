package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

class VariableNumericValueExpression implements NumericValueExpression
{
	private Variables _variable;

	VariableNumericValueExpression(String variableName) throws FilterScriptException
	{
		try {
			_variable = Variables.valueOf(variableName);
		} catch (IllegalArgumentException iae) {
			throw new FilterScriptException(String.format("Variable \"%s\" not recognized!", variableName), iae);
		}
	}

	@Override
	final public double evaluate(FilesystemUsageInformation usageInformation)
	{
		switch (_variable) {
			case filesystemSize:
				return usageInformation.filesystemSize();
			case percentAvailable:
				return usageInformation.percentAvailable();
			case percentUsed:
				return usageInformation.percentUsed();
			case spaceAvailable:
				return usageInformation.spaceAvailable();
			case spaceUsable:
				return usageInformation.spaceUsable();
			case spaceUsed:
				return usageInformation.spaceUsed();
		}

		return 0.0;
	}

	@Override
	final public String toString()
	{
		return _variable.toString();
	}
}