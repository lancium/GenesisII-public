package edu.virginia.vcgr.genii.cloud;

import java.io.OutputStream;

public interface ResourceController {

	public boolean sendFileTo(
			String localPath, String remotePath) throws Exception;
	public boolean recieveFileFrom(
			String localPath, String remotePath) throws Exception;
	public int sendCommand(
			String command, OutputStream out, OutputStream err) throws Exception;
	public void setAuthorizationFile(String path);
	
	
}
