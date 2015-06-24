package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBAndX;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTree;

public class SMBReadAndX implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		SMBAndX chain = SMBAndX.decode(params);
		int FID = params.getUShort();
		long offset = params.getUInt();
		int maxCount = params.getUShort();
		/* int minCount = */params.getUShort();
		// Timeout is only for pipes
		/* long timeout = */params.getUInt();
		params.getShort();
		if (params.remaining() > 0)
			offset |= (params.getUInt() << 32);

		SMBTree tree = c.verifyTID(h.tid);
		SMBFile file = tree.verifyFID(FID);

		acc.startParameterBlock();
		int from = SMBAndX.reserve(acc);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		int lengthLoc = acc.skip(2);
		int offsetLoc = acc.skip(2);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.putShort((short) 0);
		acc.finishParameterBlock();

		acc.startDataBlock();
		int dataOffset = acc.position();
		int read = file.read(acc, offset, maxCount);
		acc.finishDataBlock();

		acc.putShort(offsetLoc, (short) dataOffset);
		acc.putShort(lengthLoc, (short) read);

		SMBAndX.encode(acc, from, chain.getCommand());
		c.doAndX(h, chain, message, acc);
	}
}
