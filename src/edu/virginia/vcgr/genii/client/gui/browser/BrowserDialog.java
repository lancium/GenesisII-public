package edu.virginia.vcgr.genii.client.gui.browser;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.gui.browser.grid.IActionContext;
import edu.virginia.vcgr.genii.client.gui.browser.grid.ILongRunningAction;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.ContextMenuDescriptor;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.MainMenuDescriptor;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginManager;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.gui.widgets.rns.RNSTree;
import edu.virginia.vcgr.genii.client.gui.widgets.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class BrowserDialog extends JFrame
{
	static final long serialVersionUID = 0L;

	static private final int GROUP_SIZE_LIMIT = 4;
	
	static private Log _logger = LogFactory.getLog(BrowserDialog.class);
	
	static private LinkedHashSet<String> PREFERRED_MENU_ORDER;
	static private final String HELP_MENU_NAME = "Help";
	static public final String GENESISII_BROWSER_TITLE = "Genesis II RNS Browser";
	
	static
	{
		PREFERRED_MENU_ORDER = new LinkedHashSet<String>();
		
		PREFERRED_MENU_ORDER.add("File");
		PREFERRED_MENU_ORDER.add("Edit");
		PREFERRED_MENU_ORDER.add("View");
		PREFERRED_MENU_ORDER.add("Search");
		PREFERRED_MENU_ORDER.add("Window");
	}
	
	private PluginManager _pluginManager;
	private SelectionCallback _selectionCallback;
	private RNSTree _rnsTree;
	
	public BrowserDialog(PluginManager pluginManager)
		throws RNSException, ConfigurationException
	{
		setTitle(GENESISII_BROWSER_TITLE);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(true);
		
		_pluginManager = pluginManager;
		_rnsTree = new RNSTree();		
		_selectionCallback = new SelectionCallback();
		_rnsTree.addTreeSelectionListener(_selectionCallback);
		_rnsTree.addMouseListener(new RightClickHandler());
		
		TabWidget tabWidget = new TabWidget(_selectionCallback, _pluginManager);
		_rnsTree.addTreeSelectionListener(tabWidget);
		
		Container contentPane = getContentPane();
		contentPane.setLayout(new GridBagLayout());
		contentPane.add(new JLabel("RNS Browser"),
			new GridBagConstraints(0, 0, 1, 1, 0.3, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		contentPane.add(new JScrollPane(_rnsTree),
			new GridBagConstraints(0, 1, 1, 1, 0.3, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		contentPane.add(tabWidget,
			new GridBagConstraints(1, 0, 1, 2, 0.7, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		
		setJMenuBar(createMenuBar());
		
		pack();
	}
	
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		
		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenu =
			_pluginManager.getMainMenuPlugins();
		
		for (String menuName : PREFERRED_MENU_ORDER)
		{
			HashMap<String, Collection<MainMenuDescriptor>> menu = 
				mainMenu.get(menuName);
			if (menu != null)
				menuBar.add(createTopMenu(menuName, menu));
		}
		
		for (String menuName : mainMenu.keySet())
		{
			if (!PREFERRED_MENU_ORDER.contains(menuName))
			{
				HashMap<String, Collection<MainMenuDescriptor>> menu = 
					mainMenu.get(menuName);
				JMenu jmenu = createTopMenu(menuName, menu);
				if (menuName.equals(HELP_MENU_NAME))
					menuBar.setHelpMenu(jmenu);
				else
					menuBar.add(jmenu);
			}
		}
		
		return menuBar;
	}
	
	private JMenu createTopMenu(String menuName, 
		HashMap<String, Collection<MainMenuDescriptor>> menuDesc)
	{
		boolean first = true;
		JMenu menu = new JMenu(menuName);
		JMenu targetMenu;
		
		for (String group : menuDesc.keySet())
		{
			if (!first)
				menu.addSeparator();
			targetMenu = menu;
			
			Collection<MainMenuDescriptor> descList = menuDesc.get(group);
			if (descList.size() > GROUP_SIZE_LIMIT)
			{
				targetMenu = new JMenu(group);
				menu.add(targetMenu);
			}
			
			for (MainMenuDescriptor desc : descList)
			{
				first = false;
				MenuAction action = new MenuAction(this, _selectionCallback, desc);
				_rnsTree.addTreeSelectionListener(action);
				targetMenu.add(action);
			}
		}
		
		return menu;
	}
	
	private JPopupMenu createPopupMenu()
	{
		HashMap<String, Collection<ContextMenuDescriptor>> menuDesc
			= _pluginManager.getContextMenuPlugins();
		
		JPopupMenu ret = new JPopupMenu("Available Actions");
		boolean first = true;
		
		for (String group : menuDesc.keySet())
		{
			if (!first)
				ret.addSeparator();
			
			Collection<ContextMenuDescriptor> nonHiddenItems = 
				new ArrayList<ContextMenuDescriptor>();
			
			for (ContextMenuDescriptor desc : menuDesc.get(group))
			{
				RNSPath []selectedPaths = _selectionCallback.getSelectedPaths();
				try
				{
					if (desc.getPlugin().getStatus(selectedPaths) != PluginStatus.HIDDEN)
					{
						nonHiddenItems.add(desc);
					}
				}
				catch (PluginException pe)
				{
					_logger.error("Plugin threw exception.", pe);
				}
			}
			
			if (nonHiddenItems.size() > GROUP_SIZE_LIMIT)
			{
				JMenu menu = new JMenu(group);
				
				for (ContextMenuDescriptor desc : nonHiddenItems)
				{
					first = false;
					MenuAction action = new MenuAction(this, _selectionCallback, desc);
					menu.add(action);
				}
				
				first = false;
				ret.add(menu);
			} else
			{
				for (ContextMenuDescriptor desc : nonHiddenItems)
				{
					first = false;
					MenuAction action = new MenuAction(this, _selectionCallback, desc);
					ret.add(action);
				}
			}
		}
		
		return ret;
	}
	
	public IActionContext getActionContext()
	{
		return new ActionContext();
	}
	
	private class TreeRefresher implements Runnable
	{
		private RNSPath _subtreePath;
		
		public TreeRefresher(RNSPath subtreePath)
		{
			_subtreePath = subtreePath;
		}
		
		public void run()
		{
			_rnsTree.reloadSubtree(_subtreePath);
		}
	}
	
	private class ActionContext implements IActionContext
	{
		@Override
		public void performLongRunningAction(ILongRunningAction action)
		{
			Thread th = new Thread(new LongActionRunner(action, this));
			th.setDaemon(false);
			th.setName("Long Running Action");
			th.start();
		}

		@Override
		public void refreshSubTree(RNSPath subtreePath)
		{
			if (SwingUtilities.isEventDispatchThread())
				_rnsTree.reloadSubtree(subtreePath);
			else
				SwingUtilities.invokeLater(new TreeRefresher(subtreePath));
		}
		
		@Override
		public void reportError(String msg)
		{
			// TODO Auto-generated method stub
			_logger.error(msg);
		}

		@Override
		public void reportError(String msg, Throwable cause)
		{
			// TODO Auto-generated method stub
			_logger.error(msg, cause);
		}
	}
	
	private class SelectionCallback implements ISelectionCallback,
		TreeSelectionListener
	{
		private RNSPath[] _selectedPaths;
		
		@Override
		public RNSPath[] getSelectedPaths()
		{
			if (_selectedPaths == null)
			{
				TreePath[] paths = _rnsTree.getSelectionPaths();
				if (paths == null)
					return new RNSPath[0];
				
				ArrayList<RNSPath> ret = new ArrayList<RNSPath>();
				
				for (TreePath path : paths)
				{
					Object comp = path.getLastPathComponent();
					if (comp != null && comp instanceof RNSTreeNode)
					{
						RNSPath target = ((RNSTreeNode)comp).getRNSPath();
						if (target != null && target.exists())
							ret.add(target);
					}
				}
				
				_selectedPaths = ret.toArray(new RNSPath[0]);
			}
			
			return _selectedPaths;
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			_selectedPaths = null;
		}	
	}
	
	private class RightClickHandler extends MouseAdapter
	{
		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) 
		{
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) 
		{
			if (e.isPopupTrigger()) 
			{
				JPopupMenu popupMenu = createPopupMenu();
				if (popupMenu != null)
					popupMenu.show(e.getComponent(),
						e.getX(), e.getY());
			}
		}
	}
}