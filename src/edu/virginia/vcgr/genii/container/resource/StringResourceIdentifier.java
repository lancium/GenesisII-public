package edu.virginia.vcgr.genii.container.resource;

final public class StringResourceIdentifier implements ResourceIdentifier<String>
{
	private String _key;

	public StringResourceIdentifier(String key)
	{
		_key = key;
	}

	@Override
	final public String key()
	{
		return _key;
	}
}