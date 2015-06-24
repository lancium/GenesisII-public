package edu.virginia.vcgr.smb.server.queryfs;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;

public class SMBQueryFsDeviceInfo
{
	public static void encode(SMBBuffer output) throws SMBException
	{
		// Disk
		output.putInt(0x8);
		// Remote
		output.putInt(0x10);
	}
}
