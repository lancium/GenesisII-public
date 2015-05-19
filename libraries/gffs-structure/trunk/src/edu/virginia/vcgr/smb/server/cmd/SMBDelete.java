package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;
import java.util.Collection;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBDelete implements SMBCommand {
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws IOException, SMBException {
		int searchAttr = params.getUShort();
		
		String path = data.getSMBString(h.isUnicode());
		
		// Can't delete readonly files (according to the spec), so mask it out
		searchAttr &= ~SMBFileAttributes.READONLY;
		searchAttr &= ~SMBFileAttributes.VOLUME;
		searchAttr &= ~SMBFileAttributes.DIRECTORY;
		searchAttr |= SMBFileAttributes.ARCHIVE;
		searchAttr &= ~SMBFileAttributes.SEARCH;
		
		SMBTree tree = c.verifyTID(h.tid);
		Collection<RNSPath> remove = tree.listContents(path, new SMBFileAttributes(searchAttr), h.isCaseSensitive());
		
		if (remove.isEmpty())
			throw new SMBException(NTStatus.NO_SUCH_FILE);
		
		for (RNSPath todo: remove) {
			SMBTree.rm(todo);
		}
		
		acc.emptyParamBlock();
		acc.emptyDataBlock();
		
		c.sendSuccess(h, acc);
	}
}
