package edu.virginia.vcgr.genii.gjt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;

public class JobToolManager
{
	static private Logger _logger = Logger.getLogger(JobToolManager.class);

	// hmmm: make sure this is really never used!
	// static public void main(String[] args)
	// {
	// Collection<File> files = new Vector<File>(args.length);
	// for (String filename : args)
	// files.add(new File(filename));
	//
	// try {
	// launch(files, null, null);
	// } catch (Throwable cause) {
	// _logger.error("Unable to launch job tool.", cause);
	// System.err.println(cause);
	// System.exit(1);
	// }
	// }

	static public void launch(Collection<File> initialFiles, JobDefinitionListener generationListener) throws IOException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("launching new job tool.");
		// hmmm: may want to show the file set.

		// if (toolListener != null)
		// toolListener = new CountingJobToolListener(initialFiles.size(), toolListener);

		JobApplicationContext appContext = new JobApplicationContext(initialFiles, generationListener);
		appContext.start();
	}
}