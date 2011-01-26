package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.File;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;


public class CloudCopyDirectoryPhase extends AbstractCloudExecutionPhase
	implements Serializable{

	static final long serialVersionUID = 0L;

	private String _localDir;


	static private Log _logger = LogFactory.getLog(CloudCopyDirectoryPhase.class);
	
	public CloudCopyDirectoryPhase(String localDir, String remoteDir,
			String activityID, String besid){
		_localDir = localDir;
		_workingDir = remoteDir;
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

		
		HistoryContext history = HistoryContextFactory.createContext(
				HistoryEventCategory.CloudSetup);

		history.createInfoWriter("Setting up working directory").close();
		
		
		tryExecuteCommand(resourceID,
				"mkdir " + _workingDir , System.out, System.err, tManage); 

		copyDirectory(tManage, new File(_workingDir), lDir,
				resourceID, System.out, System.err); 

		history.createInfoWriter("Working directory set up").close();
	}
	
	private void copyDirectory(CloudManager tManage, File root, File dir,
			String resourceID, OutputStream out,
			OutputStream err) throws Exception{
		File[] fileList = dir.listFiles();
	
		for (int i=0; i<fileList.length; i++){
			if (fileList[i].isDirectory()){
				//first create directory then copy it
				tryExecuteCommand(resourceID,
						"mkdir " + _workingDir + fileList[i].getName(), out, err, tManage);
				copyDirectory(tManage, new File(
						root.getAbsolutePath() + "/" + fileList[i].getName()),
						fileList[i], resourceID, out, err);
			}
			else
				trySendFile(resourceID, fileList[i].getPath(),
						root.getAbsolutePath() + "/" + fileList[i].getName(), tManage);	
		}
	}

	@Override
	protected Log getLog() {
		return _logger;
	}

	@Override
	protected String getPhase() {
		return "Copy Directory";
	}
	



}
