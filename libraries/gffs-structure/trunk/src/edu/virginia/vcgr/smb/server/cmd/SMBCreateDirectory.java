package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBCreateDirectory implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws IOException, SMBException
	{
		String path = data.getSMBString(h.isUnicode());

		SMBTree tree = c.verifyTID(h.tid);
		RNSPath dir = tree.lookup(path, h.isCaseSensitive());
		SMBTree.open(dir, SMBFileAttributes.DIRECTORY, true, true, false);

		acc.emptyParamBlock();
		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
