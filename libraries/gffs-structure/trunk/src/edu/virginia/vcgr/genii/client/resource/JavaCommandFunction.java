package edu.virginia.vcgr.genii.client.resource;

import edu.virginia.vcgr.genii.common.XMLCommandFunction;
import edu.virginia.vcgr.genii.common.XMLCommandParameter;

public class JavaCommandFunction
{
	private String _functionName;
	private String _functionDescription;
	private JavaCommandParameter[] _parameters;

	JavaCommandFunction(XMLCommandFunction f)
	{
		_functionName = f.getName();
		_functionDescription = f.getDescription();

		XMLCommandParameter[] parameters = f.getParameter();
		if (parameters == null)
			parameters = new XMLCommandParameter[0];

		_parameters = new JavaCommandParameter[parameters.length];
		for (int lcv = 0; lcv < parameters.length; lcv++)
			_parameters[lcv] = new JavaCommandParameter(parameters[lcv]);
	}

	final public String name()
	{
		return _functionName;
	}

	final public String description()
	{
		return _functionDescription;
	}

	final public JavaCommandParameter[] parameters()
	{
		return _parameters;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder(_functionName + "(");
		for (int lcv = 0; lcv < _parameters.length; lcv++) {
			if (lcv != 0)
				builder.append(", ");
			builder.append(_parameters[lcv]);
		}
		builder.append(")");
		return builder.toString();
	}
}