package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;

public class CreateWorkingDirectoryPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(CreateWorkingDirectoryPhase.class);
	
	private File _workingDirectory;
	
	static private final String CREATE_WORKINGDIR_STATE = "create-workingdir";
	
	public CreateWorkingDirectoryPhase(BESWorkingDirectory workingDirectory)
	{
		super(new ActivityState(
			ActivityStateEnumeration.Running,
			CREATE_WORKINGDIR_STATE, false));
		_workingDirectory = workingDirectory.getWorkingDirectory();
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		_logger.info(String.format("Creating job working directory \"%s\".", _workingDirectory));
		new GuaranteedDirectory(_workingDirectory);
	}
}