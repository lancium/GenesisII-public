package edu.virginia.vcgr.smb.server.trans2;

import java.io.IOException;
import java.util.List;

import edu.virginia.vcgr.smb.server.NTStatus;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBFileAttributes;
import edu.virginia.vcgr.smb.server.SMBHeader;
import edu.virginia.vcgr.smb.server.SMBSearchState;
import edu.virginia.vcgr.smb.server.SMBTrans2Command;
import edu.virginia.vcgr.smb.server.SMBTransactionInfo;
import edu.virginia.vcgr.smb.server.SMBTree;
import edu.virginia.vcgr.smb.server.cmd.SMBTransaction2;
import edu.virginia.vcgr.smb.server.find.SMBFind;

public class SMBTrans2FindFirst implements SMBTrans2Command
{
	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBTransactionInfo info, SMBBuffer acc) throws IOException, SMBException
	{
		SMBBuffer params = info.getParams();
		SMBBuffer data = info.getData();

		int searchAttrs = params.getUShort();
		int searchCount = params.getUShort();
		int flags = params.getUShort();
		int infoLevel = params.getUShort();
		params.getInt();
		String filePath = params.getString(h.isUnicode()); // A pattern

		List<String> xattr;
		if (data.remaining() > 0) {
			xattr = data.getSMBGEAList();
		} else {
			xattr = null;
		}

		// handle

		searchAttrs |= SMBFileAttributes.READONLY;
		searchAttrs |= SMBFileAttributes.ARCHIVE;

		SMBTree tree = c.verifyTID(h.tid);

		boolean resume = (flags & SMBFind.RETURN_RESUME_KEYS) != 0;

		SMBSearchState search = tree.search(filePath, new SMBFileAttributes(searchAttrs), h.isCaseSensitive());
		search.setResume(resume);

		SMBBuffer dataOut = SMBBuffer.allocateBuffer(info.getMaxDataCount());
		int wrote = SMBFind.encode(search, searchCount, resume, infoLevel, xattr, h.isUnicode(), dataOut);
		dataOut.flip();

		if (wrote == 0) {
			throw new SMBException(NTStatus.NO_SUCH_FILE);
		}

		boolean eos = !search.hasNext();
		boolean close = (flags & SMBFind.CLOSE_AFTER_REQUEST) != 0 || (eos && (flags & SMBFind.CLOSE_AT_EOS) != 0);

		int SID = 0;
		if (!close) {
			SID = tree.allocateSID(search);
		}

		SMBBuffer setup = SMBBuffer.allocateBuffer(info.getMaxSetupCount() << 1);
		setup.flip();

		SMBBuffer paramOut = SMBBuffer.allocateBuffer(info.getMaxParamCount());
		paramOut.putShort((short) SID);
		paramOut.putShort((short) wrote);
		paramOut.putShort((short) ((eos) ? 1 : 0));
		paramOut.putShort((short) 0);

		// future: fill this in if needed:
		// if (close) {
		// paramOut.putShort((short)0);
		// } else {
		// paramOut.putShort((short)123);
		// }

		// This is strange; the [MS-CIFS] says it's needed if search can be resumed; others say this should be included if no resumeKey
		paramOut.putShort((short) 0);
		paramOut.flip();

		SMBTransaction2.reply(c, h, info, acc, setup, paramOut, dataOut);
	}

}
