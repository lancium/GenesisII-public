package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileSystemHelper;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBAndX;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBTreeConnectAndX implements SMBCommand
{
	static private Log _logger = LogFactory.getLog(SMBTreeConnectAndX.class);

	public final static int SUPPORT_SEARCH_BITS = 0x0001;
	public final static int SHARE_IS_IN_DFS = 0x0002;

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		SMBAndX chain = SMBAndX.decode(params);
		int flags = params.getUShort();
		int passwordLen = params.getUShort();

		SMBBuffer password = data.getBuffer(passwordLen);
		String path = data.getString(h.isUnicode());
		String service = data.getString(false);

		if (flags == 0 || password == null || service == null) {
			// silence unused var warning.
		}
		
		if (_logger.isDebugEnabled())
			_logger.debug("processing a path: " + path);

		// SMB may want to mount internal path
		String internal = "/";

		if (path.endsWith("IPC$")) {
			// this is a special path for interprocess communication. we don't actually know what to serve here.
			_logger.debug("** seeing IPC$ path");
			// hmmm: for now leave internal there at root.
			
			//hmmm: trying different approach: tell it we can't handle this.
			//throw new SMBException(NTStatus.NOT_IMPLEMENTED);

		} else {			
			// We only listen to \\stuff\grid[\path]
			String[] chunks = path.split("\\\\", 5);// Thanks regex
			if (chunks.length < 4 || !chunks[0].isEmpty() || !chunks[1].isEmpty() || !chunks[3].equalsIgnoreCase("grid")) {
				_logger.error("wacky parsing of path has failed.  bombing out.");
				throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
			}
	
			if (chunks.length == 5)
				internal = FileSystemHelper.sanitizeFilename("/" + chunks[4]);
		}

		// Find internal path
		RNSPath current = RNSPath.getCurrent();

		if (_logger.isDebugEnabled())
			_logger.debug("SMBTreeConnectAndX looking up path: " + internal);

		RNSPath rnsPath = current.lookup(internal);
		if (rnsPath == null || !rnsPath.exists()) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		}

		// Allocate new TID
		int TID = c.allocateTID(new SMBTree(rnsPath));
		h.tid = (short) TID;

		acc.startParameterBlock();
		int from = SMBAndX.reserve(acc);
		acc.putShort((short) 0);
		acc.finishParameterBlock();

		acc.startDataBlock();
		acc.putString("A:", false);
		acc.putString("NTFS", h.isUnicode());
		acc.finishDataBlock();

		SMBAndX.encode(acc, from, chain.getCommand());
		c.doAndX(h, chain, message, acc);
	}

}
