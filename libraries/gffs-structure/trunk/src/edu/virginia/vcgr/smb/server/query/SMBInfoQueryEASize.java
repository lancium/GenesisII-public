package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBDate;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBTime;

public class SMBInfoQueryEASize
{
	public static void encode(SMBFile fd, SMBBuffer buffer)
	{
		long create = fd.getCreateTime();
		long access = fd.getAccessTime();
		long write = fd.getWriteTime();
		long fileSize = fd.getSize();
		long allocSize = fileSize;
		int attr = fd.getAttr();

		SMBDate.fromMillis(create).encode(buffer);
		SMBTime.fromMillis(create).encode(buffer);
		SMBDate.fromMillis(access).encode(buffer);
		SMBTime.fromMillis(access).encode(buffer);
		SMBDate.fromMillis(write).encode(buffer);
		SMBTime.fromMillis(write).encode(buffer);
		buffer.putInt((int) fileSize);
		buffer.putInt((int) allocSize);
		buffer.putShort((short) attr);
		// No EAs
		buffer.putInt(0);
	}
}
