package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;
import edu.virginia.vcgr.smb.server.UTime;

public class SMBClose implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		int FID = params.getUShort();
		UTime writeTime = UTime.decode(acc);

		SMBTree tree = c.verifyTID(h.tid);
		SMBFile fd = tree.verifyFID(FID);
		tree.releaseFID(FID);

		fd.close();

		if (!writeTime.isZero() && !writeTime.isMax())
			fd.setWriteTime(writeTime.toMillis());

		acc.emptyParamBlock();
		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
