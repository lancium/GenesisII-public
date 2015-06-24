package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBQueryFileStandardInfo
{
	public static void encode(SMBFile fd, SMBBuffer buffer)
	{
		long fileSize = fd.getSize();
		long allocSize = fileSize;

		buffer.putLong(allocSize);
		buffer.putLong(fileSize);
		// No hardlinks
		buffer.putInt(1);
		buffer.put((byte) (fd.getDeletePending() ? 1 : 0));
		if (fd.getPath().isRNS())
			buffer.put((byte) 1);
		else
			buffer.put((byte) 0);
	}
}
