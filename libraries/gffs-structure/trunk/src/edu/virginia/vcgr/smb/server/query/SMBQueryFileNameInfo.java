package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBQueryFileNameInfo
{
	public static void encode(SMBFile fd, SMBBuffer buffer)
	{
		String fileName = fd.getPath().getName();
		int length = fileName.length();

		buffer.putInt(length * 2);
		for (int i = 0; i < length; i++)
			buffer.putChar(fileName.charAt(i));
	}
}
