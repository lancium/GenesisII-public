package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;
import java.util.Date;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;
import edu.virginia.vcgr.smb.server.UTime;

public class SMBQueryInformation implements SMBCommand
{

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws IOException, SMBException
	{
		SMBTree tree = c.verifyTID(h.tid);

		String filePath = data.getSMBString(h.isUnicode());

		// handle

		RNSPath file = tree.lookup(filePath, h.isCaseSensitive());
		TypeInformation type = SMBTree.stat(file);

		Date write = new Date();
		long size = 0;

		if (type.isByteIO()) {
			size = type.getByteIOSize();
			write = type.getByteIOModificationTime();
		}

		UTime writeTime = UTime.fromMillis(write.getTime());

		// out

		acc.startParameterBlock();
		acc.putShort((short) SMBFileAttributes.fromTypeInfo(type));
		writeTime.encode(acc);
		acc.putInt((int) size);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		c.sendSuccess(h, acc);
	}
}
