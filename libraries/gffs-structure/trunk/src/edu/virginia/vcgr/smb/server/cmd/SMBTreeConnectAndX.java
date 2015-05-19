package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBAndX;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBTreeConnectAndX implements SMBCommand {
	public final static int SUPPORT_SEARCH_BITS = 0x0001;
	public final static int SHARE_IS_IN_DFS = 0x0002;

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc)
			throws IOException, SMBException {
		SMBAndX chain = SMBAndX.decode(params);
		int flags = params.getUShort();
		int passwordLen = params.getUShort();

		SMBBuffer password = data.getBuffer(passwordLen);
		String path = data.getString(h.isUnicode());
		String service = data.getString(false);
		
		if (flags== 0 || password == null || service == null) {
			//silence unused var warning.
		}
		
		// We only listen to \\stuff\grid[\path]
		String [] chunks = path.split("\\\\", 5);// Thanks regex
		if (chunks.length < 4 || !chunks[0].isEmpty() || !chunks[1].isEmpty() || !chunks[3].equalsIgnoreCase("grid")) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		}
		
		// SMB may want to mount internal path
		String internal = "/";
		if (chunks.length == 5)
			internal = "/" + chunks[4].replace("\\", "/");
		
		// Find internal path
		RNSPath current = RNSPath.getCurrent();
		RNSPath rnsPath = current.lookup(internal);
		if (rnsPath == null || !rnsPath.exists()) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		}
		
		// Allocate new TID
		int TID = c.allocateTID(new SMBTree(rnsPath));
		h.tid = (short)TID;
		
		acc.startParameterBlock();
		int from = SMBAndX.reserve(acc);
		acc.putShort((short)0);
		acc.finishParameterBlock();
		
		acc.startDataBlock();
		acc.putString("A:", false);
		acc.putString("NTFS", h.isUnicode());
		acc.finishDataBlock();
		
		SMBAndX.encode(acc, from, chain.getCommand());
		c.doAndX(h, chain, message, acc);
	}

}
