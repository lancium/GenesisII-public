package edu.virginia.vcgr.genii.ui.plugins;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.configuration.ConfigurationException;
import org.xml.sax.SAXException;

public class UIPlugins
{
	static private Log _logger = LogFactory.getLog(UIPlugins.class);
	
	static private final int MAX_GROUP_SIZE = 4;
	
	static private Collection<UIPluginDescription> _plugins;
	
	static
	{
		try
		{
			_plugins = UIPluginConfigParser.parse();
		}
		catch (ParserConfigurationException e)
		{
			_logger.fatal("Unable to configure XML parser for plugins.", e);
			throw new ConfigurationException(
				"Unable to configure XML parser for plugins.", e);
		}
		catch (SAXException e)
		{
			_logger.fatal("Unable to parse XML config for plugins.", e);
			throw new ConfigurationException(
				"Unable to parse XML config for plugins.", e);
		}
		catch (IOException e)
		{
			_logger.fatal("Unable to read plugin configuration.", e);
			throw new ConfigurationException(
				"Unable to read plugin configuration.", e);
		}
		catch (UIPluginException e)
		{
			_logger.fatal("Unable to configure plugins.", e);
			throw new ConfigurationException(
				"Unable to configure plugins.", e);
		}
	}
	
	private Map<String, Map<String, Collection<UITopMenuPluginAction>>> _topMenuActions =
		new LinkedHashMap<String, Map<String, Collection<UITopMenuPluginAction>>>();
	private Map<String, Collection<UIPopupMenuPluginAction>> _popupMenuActions =
		new LinkedHashMap<String, Collection<UIPopupMenuPluginAction>>();
	private Collection<Pair<String, UITabPlugin>> _tabs =
		new LinkedList<Pair<String,UITabPlugin>>();
	
	private Collection<UIMenuPluginAction<? extends UIMenuPlugin>> _allActions =
		new LinkedList<UIMenuPluginAction<? extends UIMenuPlugin>>();
	
	private UIPluginContext _context;
	
	public UIPlugins(UIPluginContext context)
	{
		_context = context;
		
		for (UIPluginDescription desc : _plugins)
		{
			UITabFacetDescription tabDesc = desc.tabFacetDescription();
			if (tabDesc != null)
			{
				_tabs.add(new Pair<String, UITabPlugin>(tabDesc.tabName(),
					tabDesc.getPlugin()));
			}
			
			UIPopupMenuFacetDescription popDesc =
				desc.popupMenuFacetDescription();
			if (popDesc != null)
			{
				UIPopupMenuPluginAction action =
					new UIPopupMenuPluginAction(popDesc.getPlugin(), 
						popDesc.itemName(), context);
				action.updateStatus(new Vector<EndpointDescription>(0));
				_allActions.add(action);
				Collection<UIPopupMenuPluginAction> actions =
					_popupMenuActions.get(popDesc.groupName());
				if (actions == null)
					_popupMenuActions.put(popDesc.groupName(),
						actions = new LinkedList<UIPopupMenuPluginAction>());
				actions.add(action);
			}
			
			UITopMenuFacetDescription topDesc =
				desc.topMenuFacetDescription();
			if (topDesc != null)
			{
				UITopMenuPluginAction action =
					new UITopMenuPluginAction(
						topDesc.getPlugin(), topDesc.itemName(), context);
				action.updateStatus(new Vector<EndpointDescription>(0));
				_allActions.add(action);
				Map<String, Collection<UITopMenuPluginAction>> menu = 
					_topMenuActions.get(topDesc.menuName());
				if (menu == null)
					_topMenuActions.put(topDesc.menuName(),
						menu = new LinkedHashMap<String, Collection<UITopMenuPluginAction>>());
				Collection<UITopMenuPluginAction> actions = menu.get(topDesc.groupName());
				if (actions == null)
					menu.put(topDesc.groupName(), actions = new LinkedList<UITopMenuPluginAction>());
				actions.add(action);
			}
		}
	}
	
	public void updateStatuses(Collection<EndpointDescription> descriptions)
	{
		for (UIMenuPluginAction<? extends UIMenuPlugin> action : _allActions)
		{
			action.updateStatus(descriptions);
		}
	}
	
	public void addTopLevelMenus(JMenuBar menuBar)
	{
		Map<String, JMenu> menus = new LinkedHashMap<String, JMenu>();
		
		for (int lcv = 0; lcv < menuBar.getMenuCount(); lcv++)
		{
			JMenu menu = menuBar.getMenu(lcv);
			menus.put(menu.getText(), menu);
		}
		
		for (String menuName : _topMenuActions.keySet())
		{
			Map<String, Collection<UITopMenuPluginAction>> groups = 
				_topMenuActions.get(menuName);
			JMenu menu = menus.get(menuName);
			if (menu == null)
			{
				menus.put(menuName, menu = new JMenu(menuName));
				menuBar.add(menu);
			}
			
			Collection<UITopMenuPluginAction> singles =
				new LinkedList<UITopMenuPluginAction>();
			Map<String, Collection<Action>> groupActions =
				new LinkedHashMap<String, Collection<Action>>();
			
			for (String groupName : groups.keySet())
			{
				Collection<UITopMenuPluginAction> actions =
					groups.get(groupName);
				if (actions.size() <= 1)
					singles.addAll(actions);
				else
					groupActions.put(groupName, new Vector<Action>(actions));
			}
			
			boolean first = true;
			for (String group : groupActions.keySet())
			{
				if (!first)
					menu.addSeparator();
				first = false;
				
				Collection<Action> actions = groupActions.get(group);
				if (actions.size() > MAX_GROUP_SIZE)
				{
					JMenu subMenu = new JMenu(group);
					for (Action a : actions)
						subMenu.add(a);
				} else
				{
					for (Action a : actions)
						menu.add(a);
				}
			}
			
			if (!first && singles.size() > 0)
				menu.addSeparator();

			for (Action a : singles)
			{
				menu.add(a);
			}
		}
	}
	
	public JPopupMenu createPopupMenu()
	{
		Collection<Action> singles = new LinkedList<Action>();
		Map<String, Collection<Action>> actionGroups =
			new LinkedHashMap<String, Collection<Action>>();
		
		for (String group : _popupMenuActions.keySet())
		{
			Collection<UIPopupMenuPluginAction> actions = 
				_popupMenuActions.get(group);
			Collection<UIPopupMenuPluginAction> enabledActions =
				new LinkedList<UIPopupMenuPluginAction>();
			for (UIPopupMenuPluginAction a : actions)
			{
				if (a.isEnabled())
					enabledActions.add(a);
			}
			
			if (enabledActions.size() <= 1)
				singles.addAll(enabledActions);
			else
				actionGroups.put(group, new Vector<Action>(enabledActions));
		}
		
		if (singles.size() > 0 || actionGroups.size() > 0)
		{
			JPopupMenu menu = new JPopupMenu("Popup Menu");
			boolean first = true;
			for (String group : actionGroups.keySet())
			{
				Collection<Action> actions = actionGroups.get(group);
				
				if (!first)
					menu.addSeparator();
				if (actions.size() > MAX_GROUP_SIZE)
				{
					JMenu subMenu = new JMenu(group);
					for (Action a : actions)
						subMenu.add(a);
				} else
				{
					for (Action a : actions)
						menu.add(a);
				}
				first = false;
			}
			
			if (!first && singles.size() > 0)
				menu.addSeparator();
			
			for (Action a : singles)
			{
				menu.add(a);
			}
			
			return menu;
		}
		
		return null;
	}
	
	public void setTabPanes(JTabbedPane tabbedPane, 
		Collection<EndpointDescription> targets)
	{
		tabbedPane.removeAll();
		for (Pair<String, UITabPlugin> pair : _tabs)
		{
			if (pair.second().isEnabled(targets))
			{
				tabbedPane.addTab(pair.first(), pair.second().getComponent(
					_context));
			}
		}
	}
	
	public void fireMenuAction(
		Class<? extends UIMenuPlugin> pluginType)
	{
		UIMenuPluginAction<? extends UIMenuPlugin> action = null;
		
		for (UIMenuPluginAction<? extends UIMenuPlugin> a : _allActions)
		{
			if (pluginType.equals(a.pluginClass()))
				action = a;
		}
		
		if (action == null)
			throw new IllegalArgumentException(String.format(
				"Couldn't find plugin type %s.", pluginType));
		
		action.actionPerformed(new ActionEvent(this, 0, null));
	}
}