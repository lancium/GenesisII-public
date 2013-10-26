package edu.virginia.vcgr.appmgr.patch.builder.planning;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.virginia.vcgr.appmgr.util.plan.PlannedAction;

public class PatchPlan
{
	private Set<String> _madeDirectories = new HashSet<String>();
	private List<PlannedAction<PatchPlanContext>> _actions = new LinkedList<PlannedAction<PatchPlanContext>>();

	public boolean haveMadeDirectory(String name)
	{
		return _madeDirectories.contains(name);
	}

	public void markDirectoryMade(String name)
	{
		_madeDirectories.add(name);
	}

	public void addAction(PlannedAction<PatchPlanContext> action)
	{
		_actions.add(action);
	}

	public List<PlannedAction<PatchPlanContext>> getPlan()
	{
		return _actions;
	}
}