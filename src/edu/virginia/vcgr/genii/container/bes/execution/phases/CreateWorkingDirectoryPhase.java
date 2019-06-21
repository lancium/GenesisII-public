package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.io.FileSystemUtils;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class CreateWorkingDirectoryPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(CreateWorkingDirectoryPhase.class);

	private File _workingDirectory;
	private File _scratchDirectory;

	static private final String CREATE_WORKINGDIR_STATE = "create-workingdir";

	public CreateWorkingDirectoryPhase(BESWorkingDirectory workingDirectory, File scratchDirectory)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, CREATE_WORKINGDIR_STATE, false));
		_workingDirectory = workingDirectory.getWorkingDirectory();
		_scratchDirectory = scratchDirectory;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.CreatingActivity);

		history.createTraceWriter("Creating Job Working Directory").format("Creating job working directory:  %s", _workingDirectory).close();

		_logger.info(String.format("Creating job working directory \"%s\".", _workingDirectory));
		try {
			File cwd = new GuaranteedDirectory(_workingDirectory);

			if (OperatingSystemType.isWindows()) {
				cwd.setWritable(true, false);
			} else {
				FileSystemUtils.chmod(cwd.getAbsolutePath(),
					FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE | FileSystemUtils.MODE_USER_EXECUTE
						| FileSystemUtils.MODE_GROUP_READ | FileSystemUtils.MODE_GROUP_WRITE | FileSystemUtils.MODE_GROUP_EXECUTE);
			}

		} catch (Throwable cause) {
			history.createErrorWriter(cause, "Unable to create directory.")
				.format("Unable to create working directory %s.", _workingDirectory).close();
			throw cause;
		}
		
		try {
			// only try to link in scratch if we were given a good path and if OS is not windows based.
			if ((_scratchDirectory != null) && !OperatingSystemType.isWindows()) {
				Path newPath = Files.createSymbolicLink(_workingDirectory.toPath().resolve("scratch"), _scratchDirectory.toPath());
				_logger.debug("created new path for job's scratch directory: " + newPath);
			}
		} catch (Throwable cause) {
			history.createErrorWriter(cause, "Unable to create scratch space link in working directory.")
				.format("Unable to create scratch space link to %s in working directory %s.", _scratchDirectory, _workingDirectory).close();
			throw cause;			
		}
	}
}
