package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ClientContextResolver;
import edu.virginia.vcgr.genii.client.context.ContextFileSystem;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class SetupContextDirectoryPhase extends AbstractExecutionPhase
{
	static final long serialVersionUID = 0L;
	
	private String _contextDirectoryName;
	
	public SetupContextDirectoryPhase(String contextDirectoryName)
	{
		super(new ActivityState(
			ActivityStateEnumeration.Running, "context-directory-store", 
			false));
		
		_contextDirectoryName = contextDirectoryName;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		File dir = new File(context.getCurrentWorkingDirectory(),
			_contextDirectoryName);
		dir.mkdirs();
		ContextFileSystem.store(
			new File(dir, ClientContextResolver.USER_CONTEXT_FILENAME), 
			new File(dir, ClientContextResolver.USER_TRANSIENT_FILENAME),
			context.getCallingContext());
	}
}