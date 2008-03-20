package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class CleanupPhase extends AbstractExecutionPhase 
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static final private String CLEANUP_STAGE = "cleanup";
	
	private String _fileToCleanup;
	
	public CleanupPhase(String fileToCleanup)
	{
		super(
			new ActivityState(
				ActivityStateEnumeration.Running, CLEANUP_STAGE, false));
		
		_fileToCleanup = fileToCleanup;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		try
		{
			File file = new File(context.getCurrentWorkingDirectory(), 
				_fileToCleanup);
			file.delete();
		}
		catch (Throwable cause)
		{
			throw new ContinuableExecutionException(
				"A continuable exception has occurred while " +
					"running a BES activity.", cause);
		}
	}
}