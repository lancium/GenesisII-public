package edu.virginia.vcgr.genii.client.dialog;

public class SimpleMenuItem implements MenuItem
{
	private String _tag;
	private Object _content;
	
	public SimpleMenuItem(String tag, Object content)
	{
		_tag = tag;
		_content = content;
	}
	
	@Override
	public String getTag()
	{
		return _tag;
	}

	@Override
	public String toString()
	{
		return _content.toString();
	}
	
	@Override
	public Object getContent()
	{
		return _content;
	}
}
