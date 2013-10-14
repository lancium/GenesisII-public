package edu.virginia.vcgr.appmgr.patch.builder.planning;

import java.io.PrintStream;
import java.util.jar.JarEntry;

import edu.virginia.vcgr.appmgr.patch.builder.PatchDescription;
import edu.virginia.vcgr.appmgr.util.plan.PlannedAction;

public class WritePatchDescriptionPlannedAction implements PlannedAction<PatchPlanContext>
{
	private PatchDescription _description;

	public WritePatchDescriptionPlannedAction(PatchDescription description)
	{
		_description = description;
	}

	@Override
	public void perform(PatchPlanContext planContext) throws Throwable
	{
		JarEntry entry;

		entry = new JarEntry("META-INF/patch/patch-description.xml");
		planContext.getJarOutputStream().putNextEntry(entry);

		PrintStream pStream = new PrintStream(planContext.getJarOutputStream());
		_description.emit(pStream);
		pStream.flush();
		planContext.getJarOutputStream().closeEntry();
	}

	@Override
	public String toString()
	{
		return String.format("Writing \"%s\".", "META-INF/patch/patch-description.xml");
	}
}