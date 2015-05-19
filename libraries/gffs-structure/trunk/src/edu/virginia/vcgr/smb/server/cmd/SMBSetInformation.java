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

public class SMBSetInformation implements SMBCommand {
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws IOException, SMBException {
		int fileAttr = params.getUShort();
		UTime writeTime = UTime.decode(acc);
		params.getShort();
		params.getShort();
		params.getShort();
		params.getShort();
		params.getShort();
		
		String fileName = data.getSMBString(h.isUnicode());
		
		// handle
		
		SMBTree tree = c.verifyTID(h.tid);
		RNSPath path = tree.lookup(fileName, h.isCaseSensitive());
		SMBFile fd = SMBTree.open(path, 0, false, false, false);
		fd.setAttr(fileAttr);
		if (!writeTime.isZero())
			fd.setWriteTime(writeTime.toMillis());
		
		acc.emptyParamBlock();
		acc.emptyDataBlock();
		
		c.sendSuccess(h, acc);
	}
}
