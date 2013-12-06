package edu.virginia.vcgr.genii.ui.plugins.logs;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.ui.plugins.AbstractCombinedUIMenusPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.LazilyLoadedTab;
import edu.virginia.vcgr.genii.ui.plugins.MenuType;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.plugins.logs.panels.LogManagerPanel;
import edu.virginia.vcgr.genii.ui.plugins.logs.panels.entry.LogManagerEntryPanel;
import edu.virginia.vcgr.genii.ui.plugins.logs.panels.meta.LogManagerMetaPanel;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogFilledInTreeObject;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogPath;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogTree;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogTreeNode;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogTreeObject;
import edu.virginia.vcgr.genii.ui.plugins.logs.tree.LogTreeObjectType;

public class DLogPlugin extends AbstractCombinedUIMenusPlugin
{
	private LogTree _browserTree;
	private JTabbedPane _tabbed;
	
	@Override
	protected void performMenuAction(UIPluginContext context, MenuType menuType) 
			throws UIPluginException
	{
		try {
			JFrame frame = new JFrame("Log Viewer");

			LogManagerEntryPanel entryPanel = new LogManagerEntryPanel(context);
			LogManagerMetaPanel metaPanel = new LogManagerMetaPanel(context);
			
			Collection<LogManagerPanel> panels = new ArrayList<LogManagerPanel>();
			panels.add(entryPanel);
			panels.add(metaPanel);
			
			_tabbed = new JTabbedPane();
			_tabbed.addTab("Log Entries", new LazilyLoadedTab(entryPanel, entryPanel));
			_tabbed.addTab("Entry Metadata", new LazilyLoadedTab(metaPanel, metaPanel));

			_browserTree = new LogTree(context);
			_browserTree.addTreeSelectionListener(new LogSelectionListener(panels));

			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
			splitPane.setLeftComponent(new LogTreePanel(_browserTree));
			splitPane.setRightComponent(_tabbed);

			Container container = frame.getContentPane();
			container.setLayout(new GridBagLayout());

			container.add(splitPane, 
					new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, 
							GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, 
							new Insets(5, 5, 5, 5), 5, 5));

			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.pack();
			GUIUtils.centerWindow(frame);
			frame.setVisible(true);
			frame.toFront();
		} catch (Throwable cause) {
			if (cause instanceof UIPluginException)
				throw (UIPluginException) cause;
			else if (cause instanceof RuntimeException)
				throw (RuntimeException) cause;
			else
				throw new UIPluginException("Unable to create Log Manager.", cause);
		}
	}

	@Override
	final public boolean isEnabled(Collection<EndpointDescription> selectedDescriptions)
	{
		return true;
	}
	
	private class LogSelectionListener implements TreeSelectionListener
	{
		private Collection<LogManagerPanel> _panels;

		private LogSelectionListener(Collection<LogManagerPanel> panels)
		{
			_panels = panels;
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			Collection<LogPath> descriptions = new LinkedList<LogPath>();
			TreePath[] paths = _browserTree.getSelectionPaths();
			if (paths != null) {
				for (TreePath path : paths) {
					LogTreeNode node = (LogTreeNode) path.getLastPathComponent();
					LogTreeObject obj = (LogTreeObject) node.getUserObject();
					if (obj.objectType() == LogTreeObjectType.ENDPOINT_OBJECT) {
						LogFilledInTreeObject fObj = (LogFilledInTreeObject) obj;
						descriptions.add(fObj.path());
					}
				}
			}

			for (LogManagerPanel panel : _panels) {
				panel.updateStatus(descriptions);
			}
		}
	}
}