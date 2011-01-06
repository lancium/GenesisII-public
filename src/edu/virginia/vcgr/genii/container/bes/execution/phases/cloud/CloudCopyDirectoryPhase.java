package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public class CloudCopyDirectoryPhase implements ExecutionPhase, Serializable{

	static final long serialVersionUID = 0L;

	private String _localDir, _remoteDir, _activityID, _besid;

	public CloudCopyDirectoryPhase(String localDir, String remoteDir,
			String activityID, String besid){
		_localDir = localDir;
		_remoteDir = remoteDir;
		_activityID = activityID;
		_besid = besid;
	}

	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"push-working-dir", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		File lDir = new File(_localDir);
		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID = tManage.aquireResource(_activityID);

		//first create directories on remote system
		tManage.sendCommand(resourceID,
				"mkdir " + _remoteDir , System.out, System.err); 

		copyDirectory(tManage, new File(_remoteDir), lDir,
				resourceID, System.out, System.err); 
		
	}
	
	private void copyDirectory(CloudManager tManage, File root, File dir,
			String resourceID, OutputStream out,
			OutputStream err) throws Exception{
		File[] fileList = dir.listFiles();
	
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isDirectory()){
				//first create directory then copy it
				tManage.sendCommand(resourceID,
						"mkdir " + _remoteDir + fileList[i].getName(), out, err);
				copyDirectory(tManage, new File(
						root.getAbsolutePath() + "/" + fileList[i].getName()),
						fileList[i], resourceID, out, err);
			}
			else
				tManage.sendFileTo(resourceID, fileList[i].getPath(),
						root.getAbsolutePath() + "/" + fileList[i].getName());	
		}
	}



}
