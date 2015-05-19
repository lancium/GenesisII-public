package edu.virginia.vcgr.smb.server.set;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBSetFileAllocationInfo {
	public static void encode(SMBFile fd, SMBBuffer dataIn) {
		/*long allocSize = */dataIn.getLong();
	}
}
