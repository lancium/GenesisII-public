package edu.virginia.vcgr.genii.client.invoke;

class MethodDescription
{
	private String _methodName;
	private Class<?>[] _parameterTypes;
	private int _hashCode;

	/**
	 * The method name and parameter types passed in here are assumed to be from the port type
	 * interface
	 * 
	 * @param methodName
	 * @param parameterTypes
	 */
	MethodDescription(String methodName, Class<?>[] parameterTypes)
	{
		_methodName = methodName;
		_parameterTypes = parameterTypes;
		_hashCode = methodName.hashCode();
		for (Class<?> pType : parameterTypes)
			_hashCode ^= pType.hashCode();
	}

	public int hashCode()
	{
		return _hashCode;
	}

	public boolean equals(MethodDescription other)
	{
		if (_hashCode != other._hashCode)
			return false;
		if (!_methodName.equals(other._methodName))
			return false;
		if (_parameterTypes.length != other._parameterTypes.length)
			return false;
		for (int lcv = 0; lcv < _parameterTypes.length; lcv++)
			if (!_parameterTypes[lcv].equals(other._parameterTypes[lcv]))
				return false;

		return true;
	}

	public boolean equals(Object other)
	{
		if (other instanceof MethodDescription)
			return equals((MethodDescription) other);

		return false;
	}
}