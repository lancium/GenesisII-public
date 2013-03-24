package edu.virginia.vcgr.genii.client.history;

import javax.swing.Icon;

import edu.virginia.vcgr.genii.client.utils.icon.IconProvider;

public class DefaultHistoryCategoryInformation implements HistoryCategoryInformation
{
	static private final int DEFAULT_ICON_SIZE = 16;

	private IconProvider _iconProvider;
	private Icon _icon = null;
	private String _description;
	private String _name;

	public DefaultHistoryCategoryInformation(String name, String description, IconProvider iconProvider)
	{
		if (name == null)
			throw new IllegalArgumentException("Name cannot be null.");

		_name = name;
		_description = description;

		if (iconProvider == null)
			iconProvider = new DefaultIconProvider(DEFAULT_ICON_SIZE);

		_iconProvider = iconProvider;
	}

	public DefaultHistoryCategoryInformation(String name, String description)
	{
		this(name, description, null);
	}

	public DefaultHistoryCategoryInformation(String name, IconProvider icon)
	{
		this(name, null, icon);
	}

	public DefaultHistoryCategoryInformation(String name)
	{
		this(name, null, null);
	}

	@Override
	synchronized public Icon categoryIcon()
	{
		if (_icon == null)
			_icon = _iconProvider.createIcon();

		return _icon;
	}

	@Override
	public String description()
	{
		return _description;
	}

	@Override
	public String toString()
	{
		return _name;
	}
}