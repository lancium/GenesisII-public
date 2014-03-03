package edu.virginia.vcgr.genii.ui.plugins;

class UITopMenuFacetDescription extends UIMenuFacetDescription<UITopMenuPlugin>
{
	private String _menuName;

	UITopMenuFacetDescription(String menuName, String groupName, String itemName, UITopMenuPlugin plugin)
	{
		super(groupName, itemName, plugin);

		_menuName = menuName;
	}

	final String menuName()
	{
		return _menuName;
	}
}
