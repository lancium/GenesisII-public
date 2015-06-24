package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;

public class SMBEcho implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		int echoCount = params.getUShort();

		for (int i = 1; i <= echoCount; i++) {
			acc.startParameterBlock();
			acc.putShort((short) i);
			acc.finishParameterBlock();

			acc.startDataBlock();
			acc.put(data.slice());
			acc.finishDataBlock();

			c.sendSuccess(h, acc);

			acc.resetPacket();
		}
	}
}
