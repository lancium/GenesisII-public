package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class CleanupPhase extends AbstractExecutionPhase implements Serializable
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(CleanupPhase.class);

	static final private String CLEANUP_STAGE = "cleanup";

	private File _fileToCleanup;

	public CleanupPhase(File fileToCleanup)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, CLEANUP_STAGE, false));

		_fileToCleanup = fileToCleanup;
	}

	private void removeFile(File f)
	{
		if (f.exists()) {
			if (f.isDirectory()) {
				for (File ff : f.listFiles())
					removeFile(ff);
			}

			f.delete();
		}
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Cleanup);

		try {
			history.createTraceWriter("Cleaning Up %s", _fileToCleanup.getName())
				.format("Attempting to clean-up activity by removing %s", _fileToCleanup).close();

			_logger.info(String.format("Attempting to clean-up activity by removing file \"%s\".", _fileToCleanup.getName()));
			removeFile(_fileToCleanup);
		} catch (Throwable cause) {
			history.error(cause, "Unable to Cleanup %s", _fileToCleanup.getName());
			_logger.error("Unable to clean up file.", cause);
			throw new ContinuableExecutionException("A continuable exception has occurred while " + "running a BES activity.",
				cause);
		}
	}
}