package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Properties;

public abstract class AbstractCombinedUIMenusPlugin extends AbstractUIMenuPlugin implements UITopMenuPlugin, UIPopupMenuPlugin
{
	protected abstract void performMenuAction(UIPluginContext context, MenuType menuType) throws UIPluginException;

	@Override
	public void configureTopMenu(Properties properties) throws UIPluginException
	{
		// Ignore
	}

	@Override
	public void configurePopupMenu(Properties properties) throws UIPluginException
	{
		// Ignore
	}

	@Override
	public void performPopupMenuAction(UIPluginContext context) throws UIPluginException
	{
		performMenuAction(context, MenuType.POPUP_MENU);
	}

	@Override
	public void performTopMenuAction(UIPluginContext context) throws UIPluginException
	{
		performMenuAction(context, MenuType.TOP_MENU);
	}
}