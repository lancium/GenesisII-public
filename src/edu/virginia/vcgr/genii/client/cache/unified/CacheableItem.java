package edu.virginia.vcgr.genii.client.cache.unified;

public class CacheableItem
{

	private Object Key;
	private Object target;
	private Object value;

	public Object getKey()
	{
		return Key;
	}

	public void setKey(Object key)
	{
		Key = key;
	}

	public Object getTarget()
	{
		return target;
	}

	public void setTarget(Object target)
	{
		this.target = target;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}
}
