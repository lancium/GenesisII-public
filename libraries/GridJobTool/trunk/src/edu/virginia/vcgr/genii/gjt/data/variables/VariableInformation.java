package edu.virginia.vcgr.genii.gjt.data.variables;

public class VariableInformation
{
	private String _variable;
	private int _offset;

	public VariableInformation(String variable, int offset)
	{
		_variable = variable;
		_offset = offset;
	}

	public String variable()
	{
		return _variable;
	}

	public int offset()
	{
		return _offset;
	}
}