package edu.virginia.vcgr.externalapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import edu.virginia.vcgr.genii.gjt.JobToolManager;
import edu.virginia.vcgr.genii.gjt.gui.GridJobToolFrame;
import edu.virginia.vcgr.genii.ui.BasicFrameWindow;

public class GridJobToolApplication extends AbstractExternalApplication
{
	public GridJobToolApplication()
	{
	}

	@Override
	protected void doRun(File content) throws Throwable
	{
		Collection<File> initialFiles = new ArrayList<File>(1);
		initialFiles.add(content);
		JobToolManager.launch(initialFiles, null);

		// hmmm: do we ever actually hit this method at all?

		while (true) {
			if (BasicFrameWindow.activeFrames(GridJobToolFrame.class) <= 0) {
				/*
				 * we have found that it's time to leave since there are no job tool frames left (although we really only think we'll see this
				 * as zero and not negative).
				 */
				break;
			}
			try {
				Thread.sleep(42);
			} catch (InterruptedException e) {
				// ignored.
			}
		}

	}

	@Override
	public String toString()
	{
		return "Grid Job Tool";
	}
}