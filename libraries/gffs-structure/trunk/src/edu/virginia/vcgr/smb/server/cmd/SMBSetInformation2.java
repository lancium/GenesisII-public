package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBDate;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTime;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBSetInformation2 implements SMBCommand
{

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		int FID = params.getUShort();
		SMBDate createDate = SMBDate.decode(acc);
		SMBTime createTime = SMBTime.decode(acc);
		SMBDate accessDate = SMBDate.decode(acc);
		SMBTime accessTime = SMBTime.decode(acc);
		SMBDate writeDate = SMBDate.decode(acc);
		SMBTime writeTime = SMBTime.decode(acc);

		// handle

		SMBTree tree = c.verifyTID(h.tid);
		SMBFile file = tree.verifyFID(FID);

		long create = createDate.toMillis(createTime);
		long access = accessDate.toMillis(accessTime);
		long write = writeDate.toMillis(writeTime);

		file.setCreateTime(create);
		file.setAccessTime(access);
		file.setWriteTime(write);

		// out

		acc.emptyParamBlock();
		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}

}
