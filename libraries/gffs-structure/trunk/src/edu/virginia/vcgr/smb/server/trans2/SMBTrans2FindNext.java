package edu.virginia.vcgr.smb.server.trans2;

import java.io.IOException;
import java.util.List;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBSearchState;
import edu.virginia.vcgr.smb.server.SMBTrans2Command;
import edu.virginia.vcgr.smb.server.SMBTransactionInfo;
import edu.virginia.vcgr.smb.server.SMBTree;
import edu.virginia.vcgr.smb.server.cmd.SMBTransaction2;
import edu.virginia.vcgr.smb.server.find.SMBFind;

public class SMBTrans2FindNext implements SMBTrans2Command
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBTransactionInfo info, SMBBuffer acc) throws IOException, SMBException
	{
		SMBBuffer params = info.getParams();
		SMBBuffer data = info.getData();

		int SID = params.getUShort();
		int searchCount = params.getUShort();
		int infoLevel = params.getUShort();
		// Doesn't work correctly
		/* int resumeKey = */params.getInt();
		int flags = params.getUShort();
		String fileName = params.getString(h.isUnicode()); // A pattern

		List<String> xattr;
		if (data.remaining() > 0) {
			xattr = data.getSMBGEAList();
		} else {
			xattr = null;
		}

		SMBTree tree = c.verifyTID(h.tid);

		boolean resume = (flags & SMBFind.RETURN_RESUME_KEYS) != 0;
		boolean closeEOS = (flags & SMBFind.CLOSE_AT_EOS) != 0;
		boolean closeNow = (flags & SMBFind.CLOSE_AFTER_REQUEST) != 0;

		SMBSearchState search = tree.verifySID(SID);

		// TODO: this is probably wrong, although it does work for my machine
		if ((flags & SMBFind.CONTINUE_FROM_LAST) != 0) {
			// Continue from previous location; no-op
		}/*
		 * else if (search.getResume()) { // Continue from the resume key search.reset(resumeKey); }
		 */else {
			search.reset(fileName);
		}

		search.setResume(resume);

		SMBBuffer dataOut = SMBBuffer.allocateBuffer(info.getMaxDataCount());
		int wrote = SMBFind.encode(search, searchCount, resume, infoLevel, xattr, h.isUnicode(), dataOut);
		dataOut.flip();

		boolean eos = !search.hasNext();
		boolean close = closeNow || (eos && closeEOS);

		if (close) {
			tree.releaseSID(SID);
		}

		SMBBuffer setup = SMBBuffer.allocateBuffer(info.getMaxSetupCount() << 1);
		setup.flip();

		SMBBuffer paramOut = SMBBuffer.allocateBuffer(info.getMaxParamCount());
		paramOut.putShort((short) wrote);
		paramOut.putShort((short) ((eos) ? 1 : 0));
		paramOut.putShort((short) 0);
		// TODO: implement this
		paramOut.putShort((short) 0);
		paramOut.flip();

		SMBTransaction2.reply(c, h, info, acc, setup, paramOut, dataOut);
	}
}
