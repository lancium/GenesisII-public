package edu.virginia.vcgr.genii.client.gui.browser;

public abstract class AbstractPlugin implements IPlugin
{
	private String _name;
	
	protected AbstractPlugin(String name)
	{
		_name = name;
	}
	
	@Override
	public String getName()
	{
		return _name;
	}
}