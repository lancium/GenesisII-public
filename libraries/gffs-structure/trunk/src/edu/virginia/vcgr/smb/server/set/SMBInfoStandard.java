package edu.virginia.vcgr.smb.server.set;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBDate;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBTime;

public class SMBInfoStandard {
	public static void encode(SMBFile fd, SMBBuffer dataIn) {
		SMBDate createDate = SMBDate.decode(dataIn);
		SMBTime createTime = SMBTime.decode(dataIn);
		SMBDate accessDate = SMBDate.decode(dataIn);
		SMBTime accessTime = SMBTime.decode(dataIn);
		SMBDate writeDate = SMBDate.decode(dataIn);
		SMBTime writeTime = SMBTime.decode(dataIn);
		dataIn.skip(10);
		
		long create = createDate.toMillis(createTime);
		long access = accessDate.toMillis(accessTime);
		long write = writeDate.toMillis(writeTime);
		
		fd.setCreateTime(create);
		fd.setAccessTime(access);
		fd.setWriteTime(write);
	}
}
