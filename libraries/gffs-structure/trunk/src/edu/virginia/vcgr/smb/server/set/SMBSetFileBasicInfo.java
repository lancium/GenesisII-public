package edu.virginia.vcgr.smb.server.set;

import edu.virginia.vcgr.smb.server.FileTime;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBSetFileBasicInfo {
	public static void encode(SMBFile fd, SMBBuffer dataIn) {
		FileTime createTime = FileTime.decode(dataIn);
		FileTime accessTime = FileTime.decode(dataIn);
		FileTime writeTime = FileTime.decode(dataIn);
		/*FileTime changeTime = */FileTime.decode(dataIn);
		int fileAttr = dataIn.getInt();
		dataIn.getInt();
		
		if (!createTime.isZero() && !createTime.isMax())
			fd.setCreateTime(createTime.toMillis());
		if (!accessTime.isZero() && !accessTime.isMax())
			fd.setAccessTime(accessTime.toMillis());
		if (!writeTime.isZero() && !writeTime.isMax())
			fd.setWriteTime(writeTime.toMillis());
		fd.setExtAttr(fileAttr);
	}
}
