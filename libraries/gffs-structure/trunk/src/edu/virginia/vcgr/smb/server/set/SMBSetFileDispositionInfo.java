package edu.virginia.vcgr.smb.server.set;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBSetFileDispositionInfo {
	public static void encode(SMBFile fd, SMBBuffer dataIn) {
		int pending = dataIn.get();
		
		fd.setDeletePending(pending != 0);
	}
}
