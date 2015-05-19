package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBDeleteDirectory implements SMBCommand {
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws IOException, SMBException {
		String path = data.getSMBString(h.isUnicode());
		
		SMBTree tree = c.verifyTID(h.tid);
		RNSPath dir = tree.lookup(path, h.isCaseSensitive());
		
		// It better be an empty directory
		try {
			TypeInformation info = new TypeInformation(dir.getEndpoint());
			
			if (!info.isRNS())
				throw new SMBException(NTStatus.NO_SUCH_FILE);
			
			if (!dir.listContents().isEmpty())
				throw new SMBException(NTStatus.DIRECTORY_NOT_EMPTY);
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		} catch (RNSException e) {
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
		
		SMBTree.rm(dir);
		
		acc.emptyParamBlock();
		acc.emptyDataBlock();
		
		c.sendSuccess(h, acc);
	}
}
