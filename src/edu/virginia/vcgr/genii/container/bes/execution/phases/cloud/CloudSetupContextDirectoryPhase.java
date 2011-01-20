package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.File;
import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.context.ClientContextResolver;
import edu.virginia.vcgr.genii.client.context.ContextFileSystem;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class CloudSetupContextDirectoryPhase implements ExecutionPhase, Serializable{

	static final long serialVersionUID = 0L;
	
	private String _localWorkingDirectory;
	
	
	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"setting-up-context", false);
	}
	
	public CloudSetupContextDirectoryPhase(String lWorkingDirectory){
		_localWorkingDirectory = lWorkingDirectory;
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		HistoryContext history = HistoryContextFactory.createContext(
				HistoryEventCategory.CloudStage);

		history.createTraceWriter("Aquiring Cloud Resources").close();
		
		File dir = new File(_localWorkingDirectory);
		dir.mkdirs();
		
		ContextFileSystem.store(
				new File(dir, ClientContextResolver.USER_CONTEXT_FILENAME), 
				new File(dir, ClientContextResolver.USER_TRANSIENT_FILENAME),
				context.getCallingContext());
		
		
	}

}
