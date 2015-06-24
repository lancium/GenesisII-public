package edu.virginia.vcgr.smb.server.query;

import edu.virginia.vcgr.smb.server.SMBBuffer;
import edu.virginia.vcgr.smb.server.SMBFile;

public class SMBQueryFileCompressionInfo
{
	public static void encode(SMBFile fd, SMBBuffer buffer)
	{
		long fileSize = fd.getSize();

		buffer.putLong(fileSize);
		// Not compressed
		buffer.putShort((short) 0);
		// ?
		buffer.put((byte) 0);
		// ?
		buffer.put((byte) 0);
		// ?
		buffer.put((byte) 0);
		// reserved
		buffer.put((byte) 0);
		buffer.put((byte) 0);
		buffer.put((byte) 0);
	}
}
