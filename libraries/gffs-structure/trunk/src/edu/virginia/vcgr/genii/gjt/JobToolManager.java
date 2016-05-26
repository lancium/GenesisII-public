package edu.virginia.vcgr.genii.gjt;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.log4j.Logger;

public class JobToolManager
{
	static private Logger _logger = Logger.getLogger(JobToolManager.class);

	static public void launch(Collection<File> initialFiles, JobDefinitionListener generationListener) throws IOException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("launching new job tool.");
		JobApplicationContext appContext = new JobApplicationContext(initialFiles, generationListener);
		appContext.start();
	}
}