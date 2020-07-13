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

public class CompleteAccountingPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(CompleteAccountingPhase.class);

	private File _accountingDir;
	private File _finishedDir;

	static private final String CREATE_WORKINGDIR_STATE = "create-workingdir";

	public CompleteAccountingPhase(File accountingDirectory, File finishedDir)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, CREATE_WORKINGDIR_STATE, false));
		_accountingDir = accountingDirectory;
		_finishedDir = finishedDir;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.CreatingActivity);

		history.createTraceWriter("Moving accounting directory").format("Moving accounting directory:  %s", _accountingDir).close();

		_logger.info(String.format("Moving accounting dir \"%s to Accounting/finished, i.e., \"%s\".", _accountingDir, _finishedDir ));
		try {
			// First check that they both exist
			if (_accountingDir.exists()) {
				_accountingDir.renameTo(new File(_finishedDir.getAbsolutePath()));
				if (OperatingSystemType.isWindows()) {
					_finishedDir.setWritable(true, false);
				}
				else {
					FileSystemUtils.chmod(_finishedDir.getAbsolutePath(), FileSystemUtils.MODE_USER_READ | FileSystemUtils.MODE_USER_WRITE
							| FileSystemUtils.MODE_USER_EXECUTE);

				}			
			}
			else throw new Throwable("Accounting Dir not there");

		} catch (Throwable cause) {
			history.createErrorWriter(cause, "Unable to move accounting directory.")
			.format("Unable to move accounting directory %s.", _accountingDir).close();
			throw cause;
		}


	}
}
