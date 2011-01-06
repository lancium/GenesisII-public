package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public class CloudReleaseResourcePhase implements ExecutionPhase, Serializable{
	
	static final long serialVersionUID = 0L;
	
	private String _activityID;
	private String _besid;
	
	public CloudReleaseResourcePhase(String activityID, String besid){
		_activityID = activityID;
		_besid = besid;
	}
	
	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"releasing-resource", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		CloudManager tManage = CloudMonitor.getManager(_besid);
		if (tManage != null)
			tManage.releaseResource(_activityID);
		
	}

}
