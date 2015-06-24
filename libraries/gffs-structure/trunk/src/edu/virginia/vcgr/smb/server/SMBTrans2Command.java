package edu.virginia.vcgr.smb.server;

import java.io.IOException;

public interface SMBTrans2Command
{
	public void execute(SMBConnection c, SMBHeader h, SMBTransactionInfo info, SMBBuffer acc) throws IOException, SMBException;
}
