package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBRename implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		int searchAttr = params.getUShort();

		String oldName = data.getSMBString(h.isUnicode());
		String newName = data.getSMBString(h.isUnicode());

		// handle

		SMBTree tree = c.verifyTID(h.tid);
		// TODO: this is wrong, the names are actually patterns, although there's no documentation on how it works
		RNSPath oldPath = tree.lookup(oldName, h.isCaseSensitive());
		RNSPath newPath = tree.lookup(newName, h.isCaseSensitive());

		searchAttr &= ~SMBFileAttributes.READONLY;
		searchAttr &= ~SMBFileAttributes.VOLUME;
		SMBFileAttributes search = new SMBFileAttributes(searchAttr);

		if (!search.matches(oldPath))
			throw new SMBException(NTStatus.NO_SUCH_FILE);

		tree.rename(oldPath, newPath);

		// out

		acc.emptyParamBlock();
		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
