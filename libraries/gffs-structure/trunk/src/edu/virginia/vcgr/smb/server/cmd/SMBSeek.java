package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBSeek implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		int FID = params.getUShort();
		int mode = params.getUShort();
		int reqOffset = params.getInt();

		SMBTree tree = c.verifyTID(h.tid);
		SMBFile file = tree.verifyFID(FID);

		long offset;

		if (mode == 0) {
			if (reqOffset < 0)
				throw new SMBException(NTStatus.OS2_NEGATIVE_SEEK);

			offset = reqOffset;
		} else if (mode == 1) {
			if (reqOffset < 0)
				offset = 0;
			else
				offset = reqOffset;
		} else if (mode == 2) {
			long size = file.getSize();
			offset = size + reqOffset;
			if (offset < 0)
				offset = 0;
			else if (offset > 0xffffffff)
				throw new SMBException(NTStatus.END_OF_FILE);
		} else {
			throw new SMBException(NTStatus.INVALID_PARAMETER);
		}

		acc.startParameterBlock();
		acc.putInt((int) offset);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
