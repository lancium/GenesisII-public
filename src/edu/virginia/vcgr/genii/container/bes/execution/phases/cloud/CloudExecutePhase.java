package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public class CloudExecutePhase  implements ExecutionPhase, Serializable{

	static final long serialVersionUID = 0L;
	
	private String _activityID;
	private String _besid;
	private String _workingDir;
	private String _runScript;

	public CloudExecutePhase(String activityID, String besid,
			String workingDir, String runScript){
		_activityID = activityID;
		_besid = besid;
		_workingDir = workingDir;
		_runScript = runScript;
	}

	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"executing", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID  = tManage.aquireResource(_activityID);

		//Build Command
		String command = _workingDir + _runScript;
		tManage.sendCommand(resourceID, command, System.out, System.err);

	}

}
