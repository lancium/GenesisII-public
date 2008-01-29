package edu.virginia.vcgr.genii.client.gui.widgets.rns;

import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class RNSTree extends JTree
{
	static final long serialVersionUID = 0L;
	
	public RNSTree(RNSPath root) throws RNSException
	{
		super(new RNSTreeModel(root.createSandbox()));
		setShowsRootHandles(true);
		
		RNSTreeModel model = (RNSTreeModel)getModel();
		model.setTree(this);
		model.prepareExpansion((RNSTreeNode)model.getRoot());
		addTreeWillExpandListener(model);
		addTreeExpansionListener(model);
	}
	
	public RNSTree() throws ConfigurationException, RNSException
	{
		this(RNSPath.getCurrent().getRoot());
	}
	
	@SuppressWarnings("unchecked")
	public boolean reloadSubtree(RNSPath path)
	{
		RNSTreeModel model = (RNSTreeModel)getModel();
		TreeNode root = (TreeNode)model.getRoot();
		String sPath = path.pwd();
		
		String []components = sPath.substring(1).split("/");
		outer:
		for (String component : components)
		{
			Enumeration<TreeNode> children = root.children();
			while (children.hasMoreElements())
			{
				TreeNode child = children.nextElement();
				if (child.toString().equals(component))
				{
					root = child;
					continue outer;
				}
			}
			
			return false;
		}
		
		if (root instanceof RNSTreeNode)
			((RNSTreeNode)root).refresh(model);
		
		model.reload(root);
		return true;
	}
}
