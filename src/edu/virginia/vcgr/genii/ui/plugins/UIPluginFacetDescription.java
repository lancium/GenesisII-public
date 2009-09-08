package edu.virginia.vcgr.genii.ui.plugins;

class UIPluginFacetDescription<FacetType extends UIPlugin>
{
	private FacetType _facet;
	
	protected UIPluginFacetDescription(FacetType facet)
	{
		_facet = facet;
	}
	
	final FacetType getPlugin()
	{
		return _facet;
	}
}