package edu.virginia.vcgr.smb.server.cmd;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.smb.server.SMBAndX;
import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBCommand;
import edu.virginia.vcgr.smb.server.SMBConnection;
import edu.virginia.vcgr.smb.server.SMBDialect;
import edu.virginia.vcgr.smb.server.SMBException;
import edu.virginia.vcgr.smb.server.SMBHeader;

public class SMBSessionSetupAndX implements SMBCommand
{
	static private Log _logger = LogFactory.getLog(SMBSessionSetupAndX.class);

	@Override
	public void execute(SMBConnection c, SMBHeader h, SMBBuffer params, SMBBuffer data, SMBBuffer message, SMBBuffer acc) throws IOException,
		SMBException
	{
		SMBDialect dialect = c.getDialect();

		SMBAndX chain = SMBAndX.decode(params);
		int maxBufferSize = params.getUShort();
		
		if (_logger.isDebugEnabled())
			_logger.debug("seeing max buffer size in session setup of " + maxBufferSize);
		
		// The number of simultaneous requests
		/* int maxMpxCount = */params.getUShort();
		// The connection number; zero means reset ?
		int vcNumber = params.getUShort();
		int sessionKey = params.getInt();
		int passOEMLen = params.getUShort();

		String username = "?", domain = "?", osName = "?", smbClient = "?";

		if (username == null || domain == null || smbClient == null || osName == null || maxBufferSize == 0 || vcNumber == 0
			|| sessionKey == 0) {
			// silence unused var warning.
		}

		if (dialect.atLeast(SMBDialect.NTLM)) {
			int passSMBLen = params.getUShort();
			params.getInt();
			int capabilities = params.getInt();

			SMBBuffer passwordOEM = data.getBuffer(passOEMLen);
			SMBBuffer passwordSMB = data.getBuffer(passSMBLen);
			username = data.getString(h.isUnicode());
			domain = data.getString(h.isUnicode());
			osName = data.getString(h.isUnicode());
			smbClient = data.getString(h.isUnicode());

			if (capabilities == 0 || passwordOEM == null || passwordSMB == null) {
				// do nothing but silence unused var warnings.
			}
		} else if (dialect.atLeast(SMBDialect.LM20)) {
			int encryptLen = params.getUShort();
			int encryptOff = params.getUShort();

			SMBBuffer password = data.getBuffer(passOEMLen);
			username = data.getString(h.isUnicode());

			SMBBuffer encrypt = message.getBuffer(encryptOff, encryptLen);

			if (encrypt == null || password == null) {
				// do nothing but silence unused var warnings.
			}

		} else {
			params.getShort();
			params.getShort();

			SMBBuffer password = data.getBuffer(passOEMLen);
			username = data.getString(h.isUnicode());

			if (password == null) {
				// do nothing but silence unused var warnings.
			}
		}

		if (username == null || domain == null) {
			// do nothing but silence unused var warnings.
		}
		// System.out.println(username);
		// System.out.println(domain);
		// System.out.println(osName);
		// System.out.println(smbClient);

		acc.startParameterBlock();
		int from = SMBAndX.reserve(acc);
		acc.putShort((short) 0);
		acc.finishParameterBlock();

		acc.startDataBlock();
		if (dialect.atLeast(SMBDialect.NTLM)) {
			acc.putString("GridOS", h.isUnicode());
			acc.putString("Genesis Lan Manager", h.isUnicode());
			acc.putString("GridDomain", h.isUnicode());
		}
		acc.finishDataBlock();

		SMBAndX.encode(acc, from, chain.getCommand());
		c.doAndX(h, chain, message, acc);
	}
}
