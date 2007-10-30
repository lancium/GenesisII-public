package edu.virginia.vcgr.genii.client.gui.widgets.rns;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class RNSTreeNode extends DefaultMutableTreeNode
{
	static final long serialVersionUID = 0L;
	static private final int _MAX_FAILURES = 15;
	static private final long _CACHE_WINDOW = 1000 * 15;	// Data can be cached for 15 seconds.
	
	static public enum NodeState
	{
		NEEDS_EXPANSION,
		EXPANDING,
		EXPANDED
	}
	
	private RNSPath _target;
	private TypeInformation _typeInfo;
	private NodeState _nodeState;
	private Date _updateTimestamp;
	private int _failedCount = 0;
	
	public RNSTreeNode(RNSPath target) throws RNSPathDoesNotExistException
	{
		_target = target;
		_typeInfo = new TypeInformation(target.getEndpoint());
		
		setAllowsChildren(_typeInfo.isRNS());
		_nodeState = NodeState.NEEDS_EXPANSION;
	}
	
	public String toString()
	{
		return _target.getName();
	}
	
	public NodeState getNodeState()
	{
		return _nodeState;
	}
	
	public RNSPath getRNSPath()
	{
		return _target;
	}
	
	synchronized public void prepareExpansion(RNSTreeModel model)
	{
		if (_nodeState == NodeState.NEEDS_EXPANSION)
		{
			if (this.getChildCount() == 0)
			{
				add(new DefaultMutableTreeNode("...looking up contents...", false));
				model.nodesWereInserted(this, new int [] {0});
			} else
			{
				removeAllChildren();
				add(new DefaultMutableTreeNode("...looking up contents...", false));
				model.nodeStructureChanged(this);
			}
			
			resolveChildren(model);
		} else if (_nodeState == NodeState.EXPANDED)
		{
			if (_updateTimestamp.before(new Date()))
			{
				// What we have is stale, we need to refresh it.
				resolveChildren(model);
			}
		}
	}
	
	private void resolveChildren(RNSTreeModel model)
	{
		_nodeState = NodeState.EXPANDING;
		
		Thread th = new Thread(new RNSNodeResolver(model, this));
		th.setDaemon(false);
		th.start();
	}
	
	synchronized public void resolveChildren(RNSTreeModel model, RNSPath []children)
	{
		_nodeState = NodeState.EXPANDED;
		_updateTimestamp = new Date(System.currentTimeMillis() + _CACHE_WINDOW);
		
		if (getChildCount() > 0 && (getFirstChild() instanceof RNSTreeNode))
		{
			updateChildren(model, children);
		} else
		{
			removeAllChildren();
			try
			{
				for (RNSPath child : children)
				{
					add(new RNSTreeNode(child));
				}
				
				model.nodeStructureChanged(this);
				_failedCount = 0;
			}
			catch (Throwable cause)
			{
				resolveChildrenError(model, "Lookup Error:  " + cause.getLocalizedMessage());
			}
		}
	}
	
	private void updateChildren(RNSTreeModel model, RNSPath []children)
	{
		HashMap<String, RNSPath> newChildren = new HashMap<String, RNSPath>();
		HashMap<String, RNSTreeNode> existingChildren = new HashMap<String, RNSTreeNode>();
		ArrayList<RNSTreeNode> remove = new ArrayList<RNSTreeNode>();
		
		ArrayList<Integer> changed = new ArrayList<Integer>();
		ArrayList<Integer> added = new ArrayList<Integer>();
		
		for (RNSPath child : children)
		{
			newChildren.put(child.getName(), child);
		}
		
		Enumeration<?> cEnum = children();
		while (cEnum.hasMoreElements())
		{
			RNSTreeNode child = (RNSTreeNode)cEnum.nextElement();
			existingChildren.put(child.toString(), child);
		}
		
		int lcv = 0;
		for (String childName : existingChildren.keySet())
		{
			if (newChildren.containsKey(childName))
			{
				// The new set has this element
				RNSTreeNode oldNode = existingChildren.get(childName);
				RNSPath newPath = newChildren.get(childName);
				
				try
				{
					oldNode._target = newPath;
					oldNode._typeInfo = new TypeInformation(oldNode._target.getEndpoint());
					boolean allows = oldNode._typeInfo.isRNS();
					if (oldNode.getAllowsChildren() != allows)
					{
						changed.add(new Integer(lcv));
						oldNode.setAllowsChildren(allows);
					}
				}
				catch (Throwable t)
				{
				}
			} else
			{
				remove.add(existingChildren.get(childName));
			}
			
			lcv++;
		}
		
		for (String childName : newChildren.keySet())
		{
			if (!existingChildren.containsKey(childName))
			{
				try
				{
					add(new RNSTreeNode(newChildren.get(childName)));
					added.add(new Integer(getChildCount() - 1));
				}
				catch (Throwable t)
				{
				}
			}
		}
		
		int []removedIndices = new int[remove.size()];
		Object []removedNodes = new Object[remove.size()];
		lcv = 0;
		for (RNSTreeNode node : remove)
		{
			removedIndices[lcv] = getIndex(node);
			remove(node);
			removedNodes[lcv] = node;
			lcv++;
		}
		
		if (added.size() > 0)
		{
			int []addedIndices = new int[added.size()];
			for (lcv = 0; lcv < addedIndices.length; lcv++)
			{
				addedIndices[lcv] = added.get(lcv).intValue();
			}
			
			model.nodesWereInserted(this, addedIndices);
		}
		
		if (changed.size() > 0)
		{
			int []changedIndices = new int[changed.size()];
			for (lcv = 0; lcv < changedIndices.length; lcv++)
			{
				changedIndices[lcv] = changed.get(lcv).intValue();
			}
			
			model.nodesChanged(this, changedIndices);
		}
		
		if (removedIndices.length > 0)
		{
			model.nodesWereRemoved(this, removedIndices, removedNodes);
		}
	}
	
	synchronized public void resolveChildrenError(RNSTreeModel model, String label)
	{
		_nodeState = NodeState.EXPANDED;
		_failedCount = Math.min(_failedCount + 1, _MAX_FAILURES);
		_updateTimestamp = new Date(System.currentTimeMillis() + (_CACHE_WINDOW * _failedCount * _failedCount));
		
		removeAllChildren();
		add(new DefaultMutableTreeNode(label, false));
		model.nodeStructureChanged(this);
	}
}