package edu.virginia.vcgr.appmgr.patch.builder.planning;

import java.io.File;
import java.io.FileInputStream;
import java.util.jar.JarEntry;

import edu.virginia.vcgr.appmgr.io.IOUtils;
import edu.virginia.vcgr.appmgr.util.plan.PlannedAction;

public class WriteFilePlannedAction implements PlannedAction<PatchPlanContext>
{
	private String _entry;

	public WriteFilePlannedAction(String entry)
	{
		_entry = entry;
	}

	@Override
	public void perform(PatchPlanContext planContext) throws Throwable
	{
		FileInputStream fin = null;
		File sourceFile = planContext.getSourceFile(_entry);
		File targetPath = new File("common", _entry);

		JarEntry jEntry = new JarEntry(targetPath.getPath());
		planContext.getJarOutputStream().putNextEntry(jEntry);
		try {
			fin = new FileInputStream(sourceFile);
			IOUtils.copy(fin, planContext.getJarOutputStream());
		} finally {
			IOUtils.close(fin);
		}
		planContext.getJarOutputStream().closeEntry();
	}

	@Override
	public String toString()
	{
		return String.format("Writing \"%s\".", new File("common", _entry));
	}
}