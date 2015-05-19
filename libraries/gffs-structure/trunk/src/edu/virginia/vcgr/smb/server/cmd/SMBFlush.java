package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBFlush implements SMBCommand {
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws IOException, SMBException {
		int FID = params.getUShort();
		
		// Nothing to do, just check the arguments
		SMBTree tree = c.verifyTID(h.tid);
		if (FID != 0xffff)
			tree.verifyFID(FID);
		
		acc.emptyParamBlock();
		acc.emptyDataBlock();
		
		c.sendSuccess(h, acc);
	}
}
