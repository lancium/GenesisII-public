package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBTransactionInfo;

public class SMBTransaction2 implements SMBCommand {

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params,
			SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
			SMBException {
		int totalParamCount = params.getShort() & 0xffff;
		int totalDataCount = params.getShort() & 0xffff;
		int maxParamCount = params.getShort() & 0xffff;
		int maxDataCount = params.getShort() & 0xffff;
		int maxSetupCount = params.getShort() & 0xffff;
		int flags = params.getShort() & 0xffff;
		int timeout = params.getInt();
		params.getShort();
		int paramCount = params.getShort() & 0xffff;
		int paramOffset = params.getShort() & 0xffff;
		int dataCount = params.getShort() & 0xffff;
		int dataOffset = params.getShort() & 0xffff;
		int setupCount = params.getShort() & 0xffff;// Words apparently
		SMBBuffer setup = params.getBuffer(setupCount << 1);
		
		if (flags == 0 || timeout == 0) {
			//silence unused var warning.
		}

		
		SMBBuffer transParam = message.getBuffer(paramOffset, paramCount);
		SMBBuffer transData = message.getBuffer(dataOffset, dataCount);
		
		if (paramCount < totalParamCount || dataCount < totalDataCount) {
			/* Need more data */
			SMBTransactionInfo trans = new SMBTransactionInfo(totalParamCount, totalDataCount, maxParamCount, maxDataCount, setup, maxSetupCount);
			trans.recv(transParam, 0, totalParamCount, transData, 0, totalDataCount);
			c.setCurrentTransaction(trans);
			
			// Interim response
			c.sendSuccess(h, acc);
		} else {
			/* Off we go */
			SMBTransactionInfo trans = new SMBTransactionInfo(transParam, transData, maxParamCount, maxDataCount, setup, maxSetupCount);
			
			c.doTrans2(h, trans, acc);
		}
	}

	public static void reply(SMBConnection c, SMBHeader h, SMBTransactionInfo trans, SMBBuffer acc, SMBBuffer setup, SMBBuffer params, SMBBuffer data) throws IOException, SMBException {
		int totalParameterCount = params.remaining();
		int totalDataCount = data.remaining();
		
		// Need to return at least one response
		do {
			acc.startParameterBlock();
			acc.putShort((short)totalParameterCount);
			acc.putShort((short)totalDataCount);
			acc.putShort((short)0);
			int paramCountLoc = acc.skip(2);
			int paramOffsetLoc = acc.skip(2);
			acc.putShort((short)params.position());
			int dataCountLoc = acc.skip(2);
			int dataOffsetLoc = acc.skip(2);
			acc.putShort((short)data.position());
			acc.putShort((short)(setup.remaining() >> 1));
			acc.put(setup.slice());
			acc.finishParameterBlock();
			
			acc.startDataBlock();
			acc.align(4);
			int paramOffset = acc.position();
			int paramCount = acc.putMax(params);
			acc.align(4);
			int dataOffset = acc.position();
			int dataCount = acc.putMax(data);
			acc.finishDataBlock();
			
			acc.putShort(paramCountLoc, (short)paramCount);
			acc.putShort(paramOffsetLoc, (short)paramOffset);
			acc.putShort(dataCountLoc, (short)dataCount);
			acc.putShort(dataOffsetLoc, (short)dataOffset);
			
			c.sendSuccess(h, acc);
			
			// Reuse the buffer to send additional packets
			acc.resetPacket();
		} while (params.remaining() > 0 || data.remaining() > 0);
	}
}
