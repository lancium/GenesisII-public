package edu.virginia.vcgr.externalapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import edu.virginia.vcgr.genii.gjt.BlockingJobToolListener;
import edu.virginia.vcgr.genii.gjt.JobTool;

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
		BlockingJobToolListener waiter = new BlockingJobToolListener();
		JobTool.launch(initialFiles, null, waiter);
		waiter.join();
	}
	
	@Override
	public String toString()
	{
		return "Grid Job Tool";
	}
}