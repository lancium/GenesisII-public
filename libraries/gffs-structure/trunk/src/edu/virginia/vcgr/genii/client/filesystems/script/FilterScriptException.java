package edu.virginia.vcgr.genii.client.filesystems.script;

public class FilterScriptException extends Exception
{
	static final long serialVersionUID = 0L;

	public FilterScriptException(String msg)
	{
		super(msg);
	}

	public FilterScriptException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}