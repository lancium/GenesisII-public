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

public class SMBWriteAndX implements SMBCommand
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		SMBAndX chain = SMBAndX.decode(params);
		int FID = params.getUShort();
		long offset = params.getUInt();
		/* Timeout; does not apply to files */params.getUInt();
		short writeMode = params.getShort();
		params.getShort();
		params.getShort();
		int dataLength = params.getUShort();
		int dataOffset = params.getUShort();
		if (params.remaining() > 0)
			offset |= (params.getUInt() << 32);

		if (writeMode == 0) {
			// silence unused var warning.
		}

		SMBBuffer dataWrite = message.getBuffer(dataOffset, dataLength);

		// handling

		SMBTree tree = c.verifyTID(h.tid);
		SMBFile file = tree.verifyFID(FID);

		file.write(dataWrite, offset);

		// out

		acc.startParameterBlock();
		int from = SMBAndX.reserve(acc);
		acc.putShort((short) dataLength);
		acc.putShort((short) 0xffff);
		acc.putInt(0);
		acc.finishParameterBlock();

		acc.emptyDataBlock();

		SMBAndX.encode(acc, from, chain.getCommand());
		c.doAndX(h, chain, message, acc);
	}
}
