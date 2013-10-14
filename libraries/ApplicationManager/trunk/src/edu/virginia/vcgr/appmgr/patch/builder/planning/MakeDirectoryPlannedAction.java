package edu.virginia.vcgr.appmgr.patch.builder.planning;

import java.util.jar.JarEntry;

import edu.virginia.vcgr.appmgr.util.plan.PlannedAction;

public class MakeDirectoryPlannedAction implements PlannedAction<PatchPlanContext>
{
	private String _entry;

	public MakeDirectoryPlannedAction(String entry)
	{
		_entry = entry;
	}

	@Override
	public void perform(PatchPlanContext planContext) throws Throwable
	{
		JarEntry entry = new JarEntry(_entry);
		planContext.getJarOutputStream().putNextEntry(entry);
	}

	@Override
	public String toString()
	{
		return String.format("Creating \"%s\".", _entry);
	}
}