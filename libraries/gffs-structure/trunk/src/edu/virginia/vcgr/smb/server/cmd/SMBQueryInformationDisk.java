package edu.virginia.vcgr.smb.server.cmd;
import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;


public class SMBQueryInformationDisk implements SMBCommand {
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException, SMBException {
		c.verifyTID(h.tid);
		
		acc.startParameterBlock();
		acc.putShort((short)0xffff);
		acc.putShort((short)0xffff);
		acc.putShort((short)0xffff);
		acc.putShort((short)0xffff);
		acc.putShort((short)0);
		acc.finishParameterBlock();
		
		acc.emptyDataBlock();
		
		c.sendSuccess(h, acc);
	}
}
