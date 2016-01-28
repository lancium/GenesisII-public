package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;
import edu.virginia.vcgr.smb.server.UTime;

public class SMBCreate implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws IOException, SMBException
	{
		int fileAttr = params.getUShort();
		// Can't do anything with the creation time
		UTime creationTime = UTime.decode(acc);

		String path = data.getSMBString(h.isUnicode());

		// handle

		SMBTree tree = c.verifyTID(h.tid);
		RNSPath file = tree.lookup(path, h.isCaseSensitive());
		SMBFile fd = SMBTree.open(file, fileAttr, true, false, true);
		int FID = tree.allocateFID(fd);

		fd.setAttr(fileAttr);
		fd.setCreateTime(creationTime.toMillis());

		// out

		acc.startParameterBlock();
		acc.putShort((short) FID);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
