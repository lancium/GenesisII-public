package edu.virginia.vcgr.genii.ui.plugins;

class UITabFacetDescription extends UIPluginFacetDescription<UITabPlugin>
{
	private String _tabName;

	UITabFacetDescription(String tabName, UITabPlugin plugin)
	{
		super(plugin);

		_tabName = tabName;
	}

	final String tabName()
	{
		return _tabName;
	}
}
