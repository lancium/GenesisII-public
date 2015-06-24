package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.FileTime;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBQueryFileAllInfo
{
	public static void encode(SMBFile fd, SMBBuffer buffer) throws SMBException
	{
		long fileSize = fd.getSize();
		long create = fd.getCreateTime();
		long access = fd.getAccessTime();
		long write = fd.getWriteTime();
		long change = write;
		long allocSize = fileSize;
		String fileName = fd.getPath().getName();
		int length = fileName.length();

		FileTime.fromMillis(create).encode(buffer);
		FileTime.fromMillis(access).encode(buffer);
		FileTime.fromMillis(write).encode(buffer);
		FileTime.fromMillis(change).encode(buffer);
		buffer.putInt(fd.getExtAttr());
		buffer.putInt(0);
		buffer.putLong(allocSize);
		buffer.putLong(fileSize);
		// No hardlinks
		buffer.putInt(1);
		buffer.put((byte) (fd.getDeletePending() ? 1 : 0));
		if (fd.getPath().isRNS())
			buffer.put((byte) 1);
		else
			buffer.put((byte) 0);
		buffer.putShort((short) 0);
		// No EAs
		buffer.putInt(0);
		buffer.putInt(length * 2);
		for (int i = 0; i < length; i++)
			buffer.putChar(fileName.charAt(i));
	}
}
