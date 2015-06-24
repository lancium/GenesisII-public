package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBAndX;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;

public class SMBLogoffAndX implements SMBCommand
{

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		SMBAndX chain = SMBAndX.decode(acc);

		// TODO: check UID and close

		acc.startParameterBlock();
		int andx = SMBAndX.reserve(acc);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		SMBAndX.encode(acc, andx, chain.getCommand());

		c.doAndX(h, chain, message, acc);
	}

}
