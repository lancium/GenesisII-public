package edu.virginia.vcgr.genii.cloud.ec2;

import java.io.File;

import edu.virginia.vcgr.genii.cloud.CloudManager;

public class VMSetup
{

	public static void setupVM(String script, String archive, String remoteDirectory, String resourceID, CloudManager tManage)
		throws Exception
	{
		// send two files and execute script
		if (tManage != null) {

			// Fix after installer created (so path independent)
			// (not needed after create image)
			tManage.sendCommand(resourceID, "sudo chmod 777 /mnt", System.out, System.err);

			if (archive != null)
				tManage.sendFileTo(resourceID, archive, remoteDirectory);
			if (script != null) {
				tManage.sendFileTo(resourceID, script, remoteDirectory);
				File fScript = new File(script);
				String remoteScript = remoteDirectory + fScript.getName();
				// Set permissions on script
				String command = "chmod +x " + remoteScript;
				tManage.sendCommand(resourceID, command, System.out, System.err);
				// Execute script
				tManage.sendCommand(resourceID, remoteScript, System.out, System.err);
			}
		}
	}

}
