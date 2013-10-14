package edu.virginia.vcgr.appmgr.util;

import java.util.Properties;

public class HierarchicalProperties extends Properties
{
	static final long serialVersionUID = 0L;

	private Properties _parent = null;

	public HierarchicalProperties()
	{
		super();
	}

	public void setParent(Properties parent)
	{
		_parent = parent;
	}

	public String getProperty(String propertyName)
	{
		return getProperty(propertyName, null);
	}

	public String getProperty(String propertyName, String def)
	{
		String value = super.getProperty(propertyName);
		if (value == null && _parent != null)
			value = _parent.getProperty(propertyName);
		if (value == null)
			return def;

		return value;
	}
}
