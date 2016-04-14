package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.smb.server.FileTime;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBDialect;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;

public class SMBNegotiate implements SMBCommand
{
	static private Log _logger = LogFactory.getLog(SMBNegotiate.class);

	public static final int CAP_RAW_MODE = 0x1;
	public static final int CAP_MPX_MODE = 0x2;
	public static final int CAP_UNICODE = 0x4;
	public static final int CAP_LARGE_FILES = 0x8;
	public static final int CAP_SMBS = 0x10;
	public static final int CAP_RPC_REMOTE_APIS = 0x20;
	public static final int CAP_STATUS32 = 0x40;
	public static final int CAP_LEVEL_II_OPLOCKS = 0x80;
	public static final int CAP_LOCK_AND_READ = 0x100;
	public static final int CAP_NT_FIND = 0x200;
	public static final int CAP_BULK_TRANSFER = 0x400;
	public static final int CAP_COMPRESSED_DATA = 0x800;
	public static final int CAP_DFS = 0x1000;
	public static final int CAP_QUADWORD_ALIGNED = 0x2000;
	public static final int CAP_LARGE_READX = 0x4000;

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc)
		throws IOException, SMBException
	{
		int core = -1; // core protocol
		int LM10 = -1; // extended 1.0 protocol
		int LM12 = -1;
		int LM20 = -1; // extended 2.0 protocol
		int LM21 = -1;
		int NTLM = -1; // NT Lan Manager; unique

		for (int idx = 0; data.remaining() > 0; idx++) {
			String dialect = data.getOEMString();

			if (_logger.isDebugEnabled())
				_logger.debug("dialect[" + idx + "]=" + dialect);

			if (dialect.equalsIgnoreCase("PCLAN1.0") || dialect.equalsIgnoreCase("PC NETWORK PROGRAM 1.0")) {
				core = idx;
			} else if (dialect.equalsIgnoreCase("LANMAN1.0") || dialect.equalsIgnoreCase("NT LANMAN1.0")) {
				LM10 = idx;
			} else if (dialect.equalsIgnoreCase("LANMAN1.2") || dialect.equalsIgnoreCase("LANMAN1.2X002")) {
				LM12 = idx;
			} else if (dialect.equalsIgnoreCase("LANMAN2.1") || dialect.equalsIgnoreCase("DOS LANMAN2.1")) {
				LM21 = idx;
			} else if (dialect.equalsIgnoreCase("LM1.2X002") || dialect.equalsIgnoreCase("SMB 2.002")
				|| dialect.equalsIgnoreCase("SMB 2.???")) {
				LM20 = idx;
			} else if (dialect.equalsIgnoreCase("LANMAN2.1")) {
				LM21 = idx;
			} else if (dialect.equalsIgnoreCase("NT LM 0.12")) {
				NTLM = idx;
			}

			// System.out.println(dialect);
		}

		// NTLM = -1;
		// LM21 = -1;
		// LM20 = -1;
		// LM12 = -1;
		// LM10 = -1;

		if (LM10 != -1 || LM12 != -1 || LM20 != -1 || LM21 != -1) {
			int select;
			if (LM21 != -1) {
				_logger.debug("smb choosing NTLM version 2.1 dialect");
				select = LM21;
				c.setDialect(SMBDialect.LM21);
			} else if (LM20 != -1) {
				_logger.debug("smb choosing NTLM version 2.0 dialect");
				select = LM20;
				c.setDialect(SMBDialect.LM20);
			} else if (LM12 != -1) {
				_logger.debug("smb choosing NTLM version 1.2 dialect");
				select = LM12;
				c.setDialect(SMBDialect.LM12);
			} else {
				_logger.debug("smb choosing NTLM version 1.0 dialect");
				select = LM10;
				c.setDialect(SMBDialect.LM10);
			}

			acc.startParameterBlock();
			acc.putShort((short) select);
			acc.putShort((short) 0);

			// Max buffer size
			acc.putShort((short) 0xffff);

			// Max Mpx Count; number of simultaneous requests
			acc.putShort((short) 0xffff);
			// future: adjusted to 50 to see if makes a difference. not helping.

			// Max VCs; number of setups per individual connection
			// future: maybe force 1
			acc.putShort((short) 0xffff);
			// future: upped this also to see if helps with windows hoseups. did not help

			// Support for write raw and read raw
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.putShort((short) 0);
			acc.finishParameterBlock();

			acc.emptyDataBlock();

			c.sendSuccess(h, acc);
		} else if (NTLM != -1) {
			_logger.debug("smb choosing NTLM dialect");

			long millis = new Date().getTime();

			c.setDialect(SMBDialect.NTLM);

			acc.startParameterBlock();
			acc.putShort((short) NTLM);

			// Share-level; no authentication
			acc.put((byte) 0);

			// Max Mpx Count; number of simultaneous requests
			acc.putShort((short) 0xffff);
			// hmmm: upgraded to 50 to see it that helps. nope.

			// Max VCs; number of setups per individual connection.
			// future: maybe force 1
			acc.putShort((short) 0xffff);
			// hmmm: upped this also to see if helps with windows hoseups. did not help.

			// Max buffer size
			acc.putInt(0x20000);
			// Max raw size
			acc.putInt(0x20000);
			// Session key
			acc.putInt(0);
			// Capabilities
			acc.putInt(CAP_UNICODE | CAP_LARGE_FILES | CAP_SMBS | CAP_STATUS32 | CAP_NT_FIND);
			// System Time
			FileTime.fromMillis(millis).encode(acc);
			// Time zone
			acc.putShort((short) TimeZone.getDefault().getOffset(millis));
			// Challenge length
			acc.put((byte) 0);
			acc.finishParameterBlock();

			acc.startDataBlock();
			acc.putString("GridWorkgroup", h.isUnicode());
			acc.finishDataBlock();

			c.sendSuccess(h, acc);
		} else if (core != -1) {
			_logger.debug("smb choosing 'core' dialect");

			c.setDialect(SMBDialect.CORE);

			acc.startParameterBlock();
			acc.putShort((short) core);
			acc.finishParameterBlock();

			acc.emptyDataBlock();

			c.sendSuccess(h, acc);
		} else {
			_logger.debug("smb failed to choose any offered dialect");

			acc.startParameterBlock();
			acc.putShort((short) -1);
			acc.finishParameterBlock();

			acc.emptyDataBlock();

			c.sendSuccess(h, acc);
		}
	}
}
