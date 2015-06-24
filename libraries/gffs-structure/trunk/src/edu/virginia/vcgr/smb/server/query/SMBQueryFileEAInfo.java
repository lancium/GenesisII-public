package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBQueryFileEAInfo
{
	public static void encode(SMBFile fd, SMBBuffer buffer)
	{
		// No EAs
		buffer.putInt(0);
	}
}
