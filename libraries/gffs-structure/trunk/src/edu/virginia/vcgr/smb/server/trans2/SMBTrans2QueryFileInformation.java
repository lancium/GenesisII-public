package edu.virginia.vcgr.smb.server.trans2;

import java.io.IOException;
import java.util.List;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFile;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTrans2Command;
import edu.virginia.vcgr.smb.server.SMBTransactionInfo;
import edu.virginia.vcgr.smb.server.SMBTree;
import edu.virginia.vcgr.smb.server.cmd.SMBTransaction2;
import edu.virginia.vcgr.smb.server.query.SMBQuery;

public class SMBTrans2QueryFileInformation implements SMBTrans2Command
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBTransactionInfo info, SMBBuffer acc) throws IOException, SMBException
	{
		SMBBuffer params = info.getParams();
		SMBBuffer data = info.getData();

		int FID = params.getUShort();
		int level = params.getUShort();

		List<String> xattr;
		if (data.remaining() > 0) {
			xattr = data.getSMBGEAList();
		} else {
			xattr = null;
		}

		if (xattr == null) {
			// silence unused var warning.
		}

		SMBTree tree = c.verifyTID(h.tid);
		SMBFile fd = tree.verifyFID(FID);

		SMBBuffer setup = SMBBuffer.allocateBuffer(info.getMaxSetupCount() << 1);
		setup.flip();

		SMBBuffer paramOut = SMBBuffer.allocateBuffer(info.getMaxParamCount());
		// EA error
		paramOut.putShort((short) 0);
		paramOut.flip();

		SMBBuffer dataOut = SMBBuffer.allocateBuffer(info.getMaxDataCount());
		SMBQuery.encode(fd, dataOut, h.isUnicode(), level);
		dataOut.flip();

		SMBTransaction2.reply(c, h, info, acc, setup, paramOut, dataOut);
	}
}
