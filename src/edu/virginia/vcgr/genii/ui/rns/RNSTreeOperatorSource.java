package edu.virginia.vcgr.genii.ui.rns;

import java.util.Collection;

import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class RNSTreeOperatorSource implements OperatorSource
{
	private RNSTree _sourceTree;
	private Collection<Pair<RNSTreeNode, RNSPath>> _sourcePaths;
	
	public RNSTreeOperatorSource(RNSTree sourceTree,
		Collection<Pair<RNSTreeNode, RNSPath>> sourcePaths)
	{
		_sourceTree = sourceTree;
		_sourcePaths = sourcePaths;
	}
	
	@Override
	public boolean isFilesystemSource()
	{
		return false;
	}

	@Override
	public boolean isRNSSource()
	{
		return true;
	}
	
	public RNSTree sourceTree()
	{
		return _sourceTree;
	}
	
	public Collection<Pair<RNSTreeNode, RNSPath>> sourcePaths()
	{
		return _sourcePaths;
	}
}