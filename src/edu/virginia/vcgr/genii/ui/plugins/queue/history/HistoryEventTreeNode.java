package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.virginia.vcgr.genii.client.history.HistoryEvent;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.history.HistoryEventData;
import edu.virginia.vcgr.genii.client.history.HistoryEventLevel;
import edu.virginia.vcgr.genii.client.history.SequenceNumber;
import edu.virginia.vcgr.genii.client.history.SimpleStringHistoryEventSource;

class HistoryEventTreeNode
{
	static private Comparator<HistoryEventTreeNode> TREE_NODE_COMPARATOR =
		new Comparator<HistoryEventTreeNode>()
		{	
			@Override
			final public int compare(
				HistoryEventTreeNode o1, HistoryEventTreeNode o2)
			{
				return HistoryEvent.SEQUENCE_NUMBER_COMPARATOR.compare(
					o1.event(), o2.event());
			}
		};
	
	private HistoryEvent _event;
	
	private HistoryEventTreeNode _parent;
	private Map<SequenceNumber, HistoryEventTreeNode> _children = 
		new HashMap<SequenceNumber, HistoryEventTreeNode>();
	
	HistoryEventLevel branchLevel()
	{
		HistoryEventLevel level = _event.eventLevel();
		for (HistoryEventTreeNode child : _children.values())
		{
			HistoryEventLevel cLevel = child.branchLevel();
			if (cLevel.compareTo(level) > 0)
				level = cLevel;
		}
		
		return level;
	}
	
	Set<HistoryEventCategory> branchCategories()
	{
		EnumSet<HistoryEventCategory> categories = EnumSet.noneOf(
			HistoryEventCategory.class);
		
		categories.add(_event.eventCategory());
		for (HistoryEventTreeNode child : _children.values())
			categories.addAll(child.branchCategories());

		return categories;
	}
	
	HistoryEventTreeNode(HistoryEventTreeNode parent, HistoryEvent event)
	{
		_event = event;
		_parent = parent;
	}
	
	final HistoryEvent event()
	{
		return _event;
	}
	
	final HistoryEventTreeNode parent()
	{
		return _parent;
	}
	
	final int childCount()
	{
		return _children.size();
	}
	
	final List<HistoryEventTreeNode> children()
	{
		List<HistoryEventTreeNode> ret = 
			new ArrayList<HistoryEventTreeNode>(_children.values());
		Collections.sort(ret, TREE_NODE_COMPARATOR);
		return ret;
	}
	
	final HistoryEventTreeNode addChild(HistoryEvent event)
	{
		HistoryEventTreeNode node = new HistoryEventTreeNode(this, event);
		_children.put(event.eventNumber(), node);
		return node;
	}
	
	@Override
	final public String toString()
	{
		return (_event == null) ? "null" :
			_event.eventNumber().toString();
	}
	static private HistoryEventTreeNode findParent(
		HistoryEventTreeNode root, 
		Map<SequenceNumber, HistoryEventTreeNode> nodeMap, 
		SequenceNumber childNumber)
	{
		SequenceNumber parentNumber = childNumber.parent();
		HistoryEventTreeNode parentNode = nodeMap.get(parentNumber);
		if (parentNode == null)
		{
			HistoryEvent parentEvent = new HistoryEvent(
				parentNumber, Calendar.getInstance(),
				new SimpleStringHistoryEventSource("Faux Source", null),
				HistoryEventLevel.Trace, HistoryEventCategory.Default,
				new HashMap<String, String>(),
				new HistoryEventData("Faux Event"));
			
			if (parentNumber.isRootLevel())
			{
				nodeMap.put(parentNumber, 
					parentNode = root.addChild(parentEvent));
			} else
			{
				nodeMap.put(parentNumber, 
					parentNode = findParent(root, nodeMap, 
						parentNumber).addChild(parentEvent));
			}
		}
		
		return parentNode;
	}
	
	static private void addFilteredChildren(HistoryEventTreeNode originalRoot,
		HistoryEventFilter filter, HistoryEventTreeNode newRoot)
	{
		for (HistoryEventTreeNode child : originalRoot._children.values())
		{
			Set<HistoryEventCategory> childSet = child.branchCategories();
			childSet.retainAll(filter.categoryFilter());
			if ((filter.levelFilter().compareTo(child.branchLevel()) <= 0) &&
				(childSet.size() > 0))
			{
				HistoryEventTreeNode newChild = newRoot.addChild(
					child.event());
				addFilteredChildren(child, filter, newChild);
			}
		}
	}
	
	static HistoryEventTreeNode formTree(Collection<HistoryEvent> events)
	{
		ArrayList<HistoryEvent> sortedEvents = new ArrayList<HistoryEvent>(
			events);
		Collections.sort(sortedEvents, HistoryEvent.SEQUENCE_NUMBER_COMPARATOR);
		HistoryEventTreeNode root = new HistoryEventTreeNode(null, null);
		Map<SequenceNumber, HistoryEventTreeNode> nodeMap = 
			new HashMap<SequenceNumber, HistoryEventTreeNode>();
		
		for (HistoryEvent event : sortedEvents)
		{
			SequenceNumber number = event.eventNumber();
			if (number.isRootLevel())
				nodeMap.put(number, root.addChild(event));
			else
			{
				HistoryEventTreeNode parent = findParent(root, nodeMap, number);
				nodeMap.put(number, parent.addChild(event));
			}
		}
		
		return root;
	}
	
	static HistoryEventTreeNode formTree(HistoryEventTreeNode originalRoot,
		HistoryEventFilter filter)
	{
		HistoryEventTreeNode newRoot = new HistoryEventTreeNode(null, null);
		addFilteredChildren(originalRoot, filter, newRoot);
		return newRoot;
	}
}