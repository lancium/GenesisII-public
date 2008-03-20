package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.appmgr.ApplicationManager;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class PrepareApplicationPhase extends AbstractExecutionPhase
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private final String PREPARE_STATE = "preparing-application";
	
	private String _applicationName;
	
	public PrepareApplicationPhase(String applicationName)
	{
		super(
			new ActivityState(
				ActivityStateEnumeration.Running,
				PREPARE_STATE, false));
		
		_applicationName = applicationName;
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		ApplicationManager.prepareApplication(
			context.getCurrentWorkingDirectory(), _applicationName);
	}
}