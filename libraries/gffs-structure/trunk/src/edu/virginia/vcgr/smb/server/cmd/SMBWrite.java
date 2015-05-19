package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBWrite implements SMBCommand {
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws IOException, SMBException {
		int FID = params.getUShort();
		int reqCount = params.getUShort();
		long offset = params.getUInt();
		params.getShort();
		
		SMBBuffer dataWrite = data.getDataBuffer();
		
		// handle
		
		SMBTree tree = c.verifyTID(h.tid);
		SMBFile fd = tree.verifyFID(FID);
		
		fd.write(dataWrite, offset);
		
		// out
		
		acc.startParameterBlock();
		acc.putShort((short)reqCount);
		acc.finishParameterBlock();
		
		acc.emptyDataBlock();
		
		c.sendSuccess(h, acc);
	}
}
