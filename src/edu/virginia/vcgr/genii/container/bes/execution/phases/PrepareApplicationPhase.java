package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelativePath;
import edu.virginia.vcgr.genii.container.appmgr.ApplicationManager;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class PrepareApplicationPhase extends AbstractExecutionPhase implements Serializable
{
	static final long serialVersionUID = 0L;

	static private final String PREPARE_STATE = "preparing-application";

	private FilesystemManager _fsManager;
	private FilesystemRelativePath _executable;

	public PrepareApplicationPhase(FilesystemManager fsManager, FilesystemRelativePath executable)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, PREPARE_STATE, false));

		_fsManager = fsManager;
		_executable = executable;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		File executable = null;

		if (_executable.getFileSystemName() != null) {
			executable = _fsManager.lookup(_executable);
		} else {
			executable = new File(context.getCurrentWorkingDirectory().getWorkingDirectory(), _executable.getString());
			if (!executable.exists())
				executable = new File(_executable.getString());
		}

		ApplicationManager.prepareApplication(executable);
	}
}