package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileSystemHelper;
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

	// hmmm: ARGH, this code is nearly identical to SMBTreeConnectAndX. abstract the shared code!
	// hmmm: ARGH ARGH ARGH, probably all of these AndX are similar copies of functionality.

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

		// SMB may want to mount internal path
		String internal = "/";

		if (path.endsWith("IPC$")) {
			// this is a special path for interprocess communication. we don't actually know what to serve here.
			_logger.debug("** seeing IPC$ path");
			// hmmm: for now leave internal there at root.

			// hmmm: trying different approach: tell it we can't handle this. does not make it better.
			// throw new SMBException(NTStatus.NOT_IMPLEMENTED);
			// hmmm: trying not found instead.
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);

		} else {

			// We only listen to \\stuff\grid[\path]
			String[] chunks = path.split("\\\\", 5);// Thanks regex
			if (!chunks[0].isEmpty() || !chunks[1].isEmpty() || !chunks[3].equalsIgnoreCase("grid")) {
				throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
			}

			if (chunks.length == 5)
				internal = FileSystemHelper.sanitizeFilename("/" + chunks[4]);
		}

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
