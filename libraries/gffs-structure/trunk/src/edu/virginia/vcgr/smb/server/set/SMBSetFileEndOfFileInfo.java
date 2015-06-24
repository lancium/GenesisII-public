package edu.virginia.vcgr.smb.server.set;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBSetFileEndOfFileInfo
{
	public static void encode(SMBFile fd, SMBBuffer dataIn)
	{
		long fileSize = dataIn.getLong();
		fd.setSize(fileSize);
	}
}
