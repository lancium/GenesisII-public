package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBTreeConnect implements SMBCommand
{
	static private Log _logger = LogFactory.getLog(SMBTreeConnect.class);

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws SMBException, IOException
	{
		String path = data.getSMBString(false);
		String password = data.getSMBString(false);
		String service = data.getSMBString(false);

		if (password != null && service != null) {
			// do nothing except silence unused var warning.
			// this location could contain authentication code in the future.
		}

		// We only listen to \\stuff\grid[\path]
		String[] chunks = path.split("\\\\", 5);// Thanks regex
		if (!chunks[0].isEmpty() || !chunks[1].isEmpty() || !chunks[3].equalsIgnoreCase("grid")) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		}

		// SMB may want to mount internal path
		String internal = "/";
		if (chunks.length == 5)
			internal = "/" + chunks[4].replace("\\", "/");

		// Find internal path
		RNSPath current = RNSPath.getCurrent();

		if (_logger.isDebugEnabled())
			_logger.debug("SMBTreeConnect looking up path: " + internal);

		RNSPath rnsPath = current.lookup(internal);
		if (rnsPath == null || !rnsPath.exists()) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		}

		// Allocate new TID
		int TID = c.allocateTID(new SMBTree(rnsPath));

		acc.startParameterBlock();
		acc.putShort((short) 0xffff);
		acc.putShort((short) TID);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
