package edu.virginia.vcgr.genii.gjt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import org.apache.log4j.Logger;

public class JobTool
{
	static private Logger _logger = Logger.getLogger(JobTool.class);

	static private class CountingJobToolListener implements JobToolListener
	{
		private int _open;
		private JobToolListener _listener;

		private CountingJobToolListener(int open, JobToolListener listener)
		{
			_open = open;
			_listener = listener;
		}

		@Override
		synchronized public void jobWindowClosed()
		{
			_listener.jobWindowClosed();

			if (--_open <= 0)
				_listener.allJobWindowsClosed();
		}

		@Override
		public void allJobWindowsClosed()
		{
			// Nothing to do
		}
	}

	static public void main(String[] args)
	{
		Collection<File> files = new Vector<File>(args.length);
		for (String filename : args)
			files.add(new File(filename));

		try {
			launch(files, null, null);
		} catch (Throwable cause) {
			_logger.error("Unable to launch job tool.", cause);
			System.err.println(cause);
			System.exit(1);
		}
	}

	static public void launch(Collection<File> initialFiles, JobDefinitionListener generationListener,
		JobToolListener toolListener) throws IOException
	{
		if (toolListener != null)
			toolListener = new CountingJobToolListener(initialFiles.size(), toolListener);

		JobApplicationContext appContext = new JobApplicationContext(initialFiles, generationListener, toolListener);
		appContext.start();
	}
}