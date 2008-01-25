package edu.virginia.vcgr.genii.client.gui.browser;

public class PluginException extends Exception
{
	static final long serialVersionUID = 0L;
	
	public PluginException(String msg)
	{
		super(msg);
	}
	
	public PluginException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}