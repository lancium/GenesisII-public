package edu.virginia.vcgr.genii.ui.rns;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.RNSTreePopupListener;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.UIFrame;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPlugins;

class RNSBrowserTearoffWindow extends UIFrame
{
	static final long serialVersionUID = 0L;
	
	RNSBrowserTearoffWindow(ApplicationContext applicationContext,
		UIContext uiContext, RNSTree tree)
	{
		super(uiContext, "Browser");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		JScrollPane scroller = new JScrollPane(tree);
		scroller.setMinimumSize(RNSTree.DESIRED_BROWSER_SIZE);
		scroller.setPreferredSize(RNSTree.DESIRED_BROWSER_SIZE);
		
		content.add(scroller, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		
		UIPlugins plugins = new UIPlugins(
			new UIPluginContext(_uiContext, tree, tree));
		plugins.addTopLevelMenus(getJMenuBar());
		tree.addTreeSelectionListener(new RNSSelectionListener(plugins, tree));
		
		getMenuFactory().addHelpMenu(_uiContext, getJMenuBar());
		
		tree.addMouseListener(new RNSTreePopupListener(plugins));
	}
	
	private class RNSSelectionListener implements TreeSelectionListener
	{
		private UIPlugins _plugins;
		private RNSTree _tree;
		
		private RNSSelectionListener(UIPlugins plugins, RNSTree tree)
		{
			_plugins = plugins;
			_tree = tree;
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			Collection<EndpointDescription> descriptions =
				new LinkedList<EndpointDescription>();
			TreePath []paths = _tree.getSelectionPaths();
			if (paths != null)
			{
				for (TreePath path : paths)
				{
					RNSTreeNode node = (RNSTreeNode)path.getLastPathComponent();
					RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
					if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
					{
						RNSFilledInTreeObject fObj = (RNSFilledInTreeObject)obj;
						descriptions.add(new EndpointDescription(fObj.typeInformation(),
							fObj.endpointType(), fObj.isLocal()));
					}
				}
			}
			
			_plugins.updateStatuses(descriptions);
		}
	}
}