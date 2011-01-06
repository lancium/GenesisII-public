package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public class CloudStageInPhase  implements ExecutionPhase, Serializable{

	static final long serialVersionUID = 0L;
	
	private String _activityID;
	private String _besid;
	private String _workingDir;
	private String _genDir;
	private String _genState;
	private String _jobFile;

	public CloudStageInPhase(String activityID, String besid,
			String workingDir, String genDir,
			String genState, String jobFile){
		_activityID = activityID;
		_besid = besid;
		_workingDir = workingDir;
		_genDir = genDir;
		_genState = genState;
		_jobFile = jobFile;
	}

	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"staging-in", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID  = tManage.aquireResource(_activityID);

		//Build Command
		String command = 
			"export GENII_USER_DIR=" + _workingDir + _genState +
			"; " + _genDir + "grid";
		command +=  " stageData --direction=\"in\" --type=\"binary\" ";
		command += _workingDir + " local:" + _workingDir + _jobFile;
		
		tManage.sendCommand(resourceID, command, System.out, System.err);

	}

}
