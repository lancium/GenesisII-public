package edu.virginia.vcgr.genii.ui.plugins;

class UIPluginDescription
{
	private UITopMenuFacetDescription _topMenuFacetDescription;
	private UIPopupMenuFacetDescription _popupMenuFacetDescription;
	private UITabFacetDescription _tabFacetDescription;
	
	UIPluginDescription(
		UITopMenuFacetDescription topMenuFacetDescription,
		UIPopupMenuFacetDescription popupMenuFacetDescription,
		UITabFacetDescription tabFacetDescription)
	{
		_topMenuFacetDescription = topMenuFacetDescription;
		_popupMenuFacetDescription = popupMenuFacetDescription;
		_tabFacetDescription = tabFacetDescription;
	}
	
	final UITopMenuFacetDescription topMenuFacetDescription()
	{
		return _topMenuFacetDescription;
	}
	
	final UIPopupMenuFacetDescription popupMenuFacetDescription()
	{
		return _popupMenuFacetDescription;
	}
	
	final UITabFacetDescription tabFacetDescription()
	{
		return _tabFacetDescription;
	}
}