package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.FileTime;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBQueryFileBasicInfo {
	public static void encode(SMBFile fd, SMBBuffer buffer) throws SMBException {
		long create = fd.getCreateTime();
		long access = fd.getAccessTime();
		long write = fd.getWriteTime();
		long change = write;
			
		FileTime.fromMillis(create).encode(buffer);
		FileTime.fromMillis(access).encode(buffer);
		FileTime.fromMillis(write).encode(buffer);
		FileTime.fromMillis(change).encode(buffer);
		buffer.putInt(fd.getExtAttr());
		buffer.putInt(0);
	}
}