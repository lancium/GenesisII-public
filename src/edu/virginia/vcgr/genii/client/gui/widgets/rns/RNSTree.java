package edu.virginia.vcgr.genii.client.gui.widgets.rns;

import javax.swing.JTree;

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
}
