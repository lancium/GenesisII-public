package edu.virginia.vcgr.xscript;

public class ReturnFromFunctionException extends Exception
{
	static final long serialVersionUID = 0L;

	private Object _result;

	public ReturnFromFunctionException(Object result)
	{
		_result = result;
	}

	public Object getResult()
	{
		return _result;
	}
}