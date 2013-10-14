package edu.virginia.vcgr.genii.ui.plugins;

class UIMenuFacetDescription<FacetType extends UIMenuPlugin> extends UIPluginFacetDescription<FacetType>
{
	private String _groupName;
	private String _itemName;

	protected UIMenuFacetDescription(String groupName, String itemName, FacetType facet)
	{
		super(facet);

		_groupName = groupName;
		_itemName = itemName;
	}

	final String groupName()
	{
		return _groupName;
	}

	final String itemName()
	{
		return _itemName;
	}
}