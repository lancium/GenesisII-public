package edu.virginia.vcgr.smb.server.queryfs;

import edu.virginia.vcgr.smb.server.FileTime;
import edu.virginia.vcgr.smb.server.SMBBuffer;

public class SMBQueryFsVolumeInfo {
	public static void encode(SMBBuffer buffer) {
		int length = SMBQueryFs.VOLUME_LABEL.length();
		
		FileTime.fromMillis(0).encode(buffer);
		buffer.putInt(SMBQueryFs.SERIAL_NUMBER);
		buffer.putInt(2 * length);
		buffer.putShort((short)0);
		for (int i = 0; i < length; i++)
			buffer.putChar(SMBQueryFs.VOLUME_LABEL.charAt(i));
	}
}
