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

/**
 * The BrowserDialog class is the main frame for the browser dialog.
 * 
 * @author mmm2a
 */
public class BrowserDialog extends JFrame
{
	static final long serialVersionUID = 0L;

	static private final int GROUP_SIZE_LIMIT = 4;

	static private Log _logger = LogFactory.getLog(BrowserDialog.class);

	static private LinkedHashSet<String> PREFERRED_MENU_ORDER;
	static private final String HELP_MENU_NAME = "Help";
	static public final String GENESISII_BROWSER_TITLE = "Genesis II RNS Browser";

	static {
		/*
		 * Create a list of top menu names that can easily be referenced by a hash table, but can be
		 * accessed in the order in which they were added. This list is used to priority the order
		 * that menus appear in along the top. If a plugin shows up in one of these menus, then that
		 * menu will be placed, relative to others, according to this list. If a plugin is in a menu
		 * NOT included on this list, then it simply goes at the end.
		 */
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

	/**
	 * Given a plugin manager (with configured plugins), create a new browser dialog that we can
	 * display.
	 * 
	 * @param pluginManager
	 *            The plugin manager that contains the plugins to use for this browser.
	 * 
	 * @throws RNSException
	 * @throws ConfigurationException
	 */
	public BrowserDialog(PluginManager pluginManager) throws RNSException
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
		contentPane.add(new JLabel("RNS Browser"), new GridBagConstraints(0, 0, 1, 1, 0.3, 0.0, GridBagConstraints.WEST,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		contentPane.add(new JScrollPane(_rnsTree), new GridBagConstraints(0, 1, 1, 1, 0.3, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		contentPane.add(tabWidget, new GridBagConstraints(1, 0, 1, 2, 0.7, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		setJMenuBar(createMenuBar());

		pack();
	}

	/**
	 * Create the top menu bar for the browser.
	 * 
	 * @return The top menu bar.
	 */
	private JMenuBar createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();

		HashMap<String, HashMap<String, Collection<MainMenuDescriptor>>> mainMenu = _pluginManager.getMainMenuPlugins();

		/*
		 * First, go through the preferred order and see if there are any plugins in the list that
		 * should be added in a specific order.
		 */
		for (String menuName : PREFERRED_MENU_ORDER) {
			HashMap<String, Collection<MainMenuDescriptor>> menu = mainMenu.get(menuName);
			if (menu != null)
				menuBar.add(createTopMenu(menuName, menu));
		}

		/*
		 * Now, go through all the plugins and add all the ones which weren't already taken care of
		 * during the preferred menu order code above.
		 */
		for (String menuName : mainMenu.keySet()) {
			if (!PREFERRED_MENU_ORDER.contains(menuName)) {
				HashMap<String, Collection<MainMenuDescriptor>> menu = mainMenu.get(menuName);
				JMenu jmenu = createTopMenu(menuName, menu);
				if (menuName.equals(HELP_MENU_NAME))
					menuBar.setHelpMenu(jmenu);
				else
					menuBar.add(jmenu);
			}
		}

		return menuBar;
	}

	/**
	 * This internal function creates a top menu pull-down.
	 * 
	 * @param menuName
	 *            The name of the menu to create.
	 * @param menuDesc
	 *            The collection of menu descriptions for this pulldown menu.
	 * 
	 * @return The newly created menu.
	 */
	private JMenu createTopMenu(String menuName, HashMap<String, Collection<MainMenuDescriptor>> menuDesc)
	{
		boolean first = true;
		JMenu menu = new JMenu(menuName);
		JMenu targetMenu;

		/* Iterate through all the groups */
		for (String group : menuDesc.keySet()) {
			/*
			 * If it's a new group and we have already added at least one group before, then add a
			 * seperator
			 */
			if (!first)
				menu.addSeparator();

			targetMenu = menu;
			Collection<MainMenuDescriptor> descList = menuDesc.get(group);

			/*
			 * If this group has more then the group limit number of items, then go ahead and make
			 * it a seperate pop-up menu off of the main menu.
			 */
			if (descList.size() > GROUP_SIZE_LIMIT) {
				targetMenu = new JMenu(group);
				menu.add(targetMenu);
			}

			/* Now, go through the menu items creating the menus indicated. */
			for (MainMenuDescriptor desc : descList) {
				first = false;
				MenuAction action = new MenuAction(this, _selectionCallback, desc);
				_rnsTree.addTreeSelectionListener(action);
				targetMenu.add(action);
			}
		}

		return menu;
	}

	/**
	 * Create a popup menu for the items currently selected in the RNStree.
	 * 
	 * @return The newly created popup-menu.
	 */
	private JPopupMenu createPopupMenu()
	{
		HashMap<String, Collection<ContextMenuDescriptor>> menuDesc = _pluginManager.getContextMenuPlugins();

		JPopupMenu ret = new JPopupMenu("Available Actions");
		boolean first = true;

		/* Iterate through the groups */
		for (String group : menuDesc.keySet()) {
			/* If we've already added groups before, then put a seperator */
			if (!first)
				ret.addSeparator();

			/*
			 * We're now going to figure out which of the plugins are NOT hidden according to their
			 * status.
			 */
			Collection<ContextMenuDescriptor> nonHiddenItems = new ArrayList<ContextMenuDescriptor>();
			for (ContextMenuDescriptor desc : menuDesc.get(group)) {
				RNSPath[] selectedPaths = _selectionCallback.getSelectedPaths();
				try {
					if (desc.getPlugin().getStatus(selectedPaths) != PluginStatus.HIDDEN)
						nonHiddenItems.add(desc);
				} catch (PluginException pe) {
					_logger.error("Plugin threw exception.", pe);
				}
			}

			/*
			 * Now that we know how many plugins in this group are NOT hidden, we check to see if
			 * the number is larger then our group limit. If it is, we instead create a side
			 * popup-menu to handle the large group.
			 */
			if (nonHiddenItems.size() > GROUP_SIZE_LIMIT) {
				JMenu menu = new JMenu(group);

				for (ContextMenuDescriptor desc : nonHiddenItems) {
					first = false;
					MenuAction action = new MenuAction(this, _selectionCallback, desc);
					menu.add(action);
				}

				first = false;
				ret.add(menu);
			} else {
				/*
				 * If the group wasn't too large, just go ahead and add the items to the popup menu.
				 */
				for (ContextMenuDescriptor desc : nonHiddenItems) {
					first = false;
					MenuAction action = new MenuAction(this, _selectionCallback, desc);
					ret.add(action);
				}
			}
		}

		return ret;
	}

	/**
	 * Create a new action context that we can give to plugins to allow them to request "favors"
	 * from us.
	 * 
	 * @return The newly created action context.
	 */
	public IActionContext getActionContext()
	{
		return new ActionContext(this);
	}

	/**
	 * The tree refresher is a simple internal class that gets queued up by the event dispatch
	 * mechanism so that we can guarantee that it modifies the RNS tree on the event dispatch
	 * thread.
	 * 
	 * @author mmm2a
	 */
	private class TreeRefresher implements Runnable
	{
		private RNSPath _subtreePath;

		/**
		 * Create a new TreeRefresher.
		 * 
		 * @param subtreePath
		 *            The RNSPath in the tree to refresh.
		 */
		public TreeRefresher(RNSPath subtreePath)
		{
			_subtreePath = subtreePath;
		}

		@Override
		public void run()
		{
			_rnsTree.reloadSubtree(_subtreePath);
		}
	}

	/**
	 * This private class is the implementation of the ActionContext that plugins can use to ask the
	 * browser to do things for them.
	 * 
	 * @author mmm2a
	 */
	private class ActionContext implements IActionContext
	{
		private BrowserDialog _browser;

		/**
		 * Create a new ActionContext.
		 * 
		 * @param browser
		 *            The browser that will handle the favors.
		 */
		public ActionContext(BrowserDialog browser)
		{
			_browser = browser;
		}

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
			_logger.error(msg);
			ErrorDialog.showErrorDialog(_browser, msg);
		}

		@Override
		public void reportError(String msg, Throwable cause)
		{
			_logger.error(msg, cause);
			ErrorDialog.showErrorDialog(_browser, msg, cause);
		}
	}

	/**
	 * The selection callback class is used to allow other pieces of code to ask for the currently
	 * selected items. This is given to actions so that they know how to find the items.
	 * 
	 * @author mmm2a
	 */
	private class SelectionCallback implements ISelectionCallback, TreeSelectionListener
	{
		private RNSPath[] _selectedPaths;

		@Override
		public RNSPath[] getSelectedPaths()
		{
			if (_selectedPaths == null) {
				TreePath[] paths = _rnsTree.getSelectionPaths();
				if (paths == null)
					return new RNSPath[0];

				ArrayList<RNSPath> ret = new ArrayList<RNSPath>();

				for (TreePath path : paths) {
					Object comp = path.getLastPathComponent();
					if (comp != null && comp instanceof RNSTreeNode) {
						RNSPath target = ((RNSTreeNode) comp).getRNSPath();
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

	/**
	 * This is a class which is registered with the RNSTree to receive mouse clicks that might
	 * trigger the popup-context menu.
	 * 
	 * @author mmm2a
	 */
	private class RightClickHandler extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			maybeShowPopup(e);
		}

		/**
		 * Determines whether or not a popup-event has happend, and if so, handles the popup menu.
		 * 
		 * @param e
		 *            The mouse event that triggered the check for a popup action.
		 */
		private void maybeShowPopup(MouseEvent e)
		{
			if (e.isPopupTrigger()) {
				JPopupMenu popupMenu = createPopupMenu();
				if (popupMenu != null)
					popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
}