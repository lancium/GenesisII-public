package edu.virginia.vcgr.genii.ui.plugins;

public class UIPluginException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public UIPluginException(String msg)
	{
		super(msg);
	}
	
	public UIPluginException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}