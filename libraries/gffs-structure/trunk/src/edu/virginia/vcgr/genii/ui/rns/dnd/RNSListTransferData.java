package edu.virginia.vcgr.genii.ui.rns.dnd;

import java.util.Collection;

import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.rns.RNSTree;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;

public class RNSListTransferData
{
	private UIContext _sourceContext;
	private RNSTree _tree;
	private Collection<Pair<RNSTreeNode, RNSPath>> _paths;

	RNSListTransferData(RNSTree tree, UIContext sourceContext, Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		_sourceContext = sourceContext;
		_paths = paths;
		_tree = tree;
	}

	final public RNSTree tree()
	{
		return _tree;
	}

	final public UIContext sourceContext()
	{
		return _sourceContext;
	}

	final public Collection<Pair<RNSTreeNode, RNSPath>> paths()
	{
		return _paths;
	}
}