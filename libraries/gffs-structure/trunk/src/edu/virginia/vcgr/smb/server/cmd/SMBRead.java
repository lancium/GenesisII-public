package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBRead implements SMBCommand {
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws IOException, SMBException {
		int FID = params.getUShort();
		int reqCount = params.getUShort();
		long offset = params.getUInt();
		params.getShort();
		
		// handle
		
		SMBTree tree = c.verifyTID(h.tid);
		SMBFile fd = tree.verifyFID(FID);
		
		// out
		
		acc.startParameterBlock();
		int countOffset = acc.skip(2);
		acc.putShort((short)0);
		acc.putShort((short)0);
		acc.putShort((short)0);
		acc.putShort((short)0);
		acc.finishParameterBlock();
		
		acc.startDataBlock();
		int startBuffer = acc.startDataBuffer();
		int count = fd.read(acc, offset, reqCount);
		acc.finishDataBuffer(startBuffer);
		acc.finishDataBlock();
		
		acc.putShort(countOffset, (short)count);
		
		c.sendSuccess(h, acc);
	}
}
