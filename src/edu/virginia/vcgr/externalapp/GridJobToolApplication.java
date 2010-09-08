package edu.virginia.vcgr.externalapp;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import edu.virginia.vcgr.genii.gjt.JobTool;
import edu.virginia.vcgr.genii.gjt.JobToolListener;

public class GridJobToolApplication extends AbstractExternalApplication
{
	public GridJobToolApplication()
	{
	}

	static private class Waiter implements JobToolListener
	{
		private boolean _done = false;
		
		@Override
		final public void jobToolClosed()
		{
			synchronized(this)
			{
				_done = true;
				this.notifyAll();
			}
		}
		
		private void join() throws InterruptedException 
		{
			synchronized(this)
			{
				while (!_done)
					wait();
			}
		}
	}
	
	@Override
	protected void doRun(File content) throws Throwable
	{
		Collection<File> initialFiles = new ArrayList<File>(1);
		initialFiles.add(content);
		Waiter waiter = new Waiter();
		JobTool.launch(initialFiles, null, waiter);
		waiter.join();
	}
	
	@Override
	public String toString()
	{
		return "Grid Job Tool";
	}
}