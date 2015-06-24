package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTransactionInfo;

public class SMBTransaction2Secondary implements SMBCommand
{

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		int totalParamCount = params.getShort() & 0xffff;
		int totalDataCount = params.getShort() & 0xffff;
		int paramCount = params.getShort() & 0xffff;
		int paramOffset = params.getShort() & 0xffff;
		int paramDisp = params.getShort() & 0xffff;
		int dataCount = params.getShort() & 0xffff;
		int dataOffset = params.getShort() & 0xffff;
		int dataDisp = params.getShort() & 0xffff;
		int fid = params.getShort() & 0xffff;

		if (fid == 0) {
			// silence unused var warning.
		}

		SMBBuffer transParam = message.getBuffer(paramOffset, paramCount);
		SMBBuffer transData = message.getBuffer(dataOffset, dataCount);

		SMBTransactionInfo trans = c.getCurrentTransaction();
		if (trans == null) {
			// We must not return anything
			return;
		}

		if (trans.recv(transParam, paramDisp, totalParamCount, transData, dataDisp, totalDataCount)) {
			/* Off we go */
			c.setCurrentTransaction(null);

			c.doTrans2(h, trans, acc);
		} else {
			/* Need more data; NOP */
		}
	}
}
