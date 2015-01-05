package edu.virginia.vcgr.genii.network;

import java.util.zip.CRC32;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ByteVerifications
{
	static private Log _logger = LogFactory.getLog(ByteVerifications.class);

	public static long getChecksum(byte[] bytes)
	{
		try {
			CRC32 c = new CRC32();
			c.update(bytes, 0, bytes.length);
			return c.getValue();
		} catch (Exception e) {
			_logger.error("problem encoding checksum for byte array.");
			return -1;
		}
	}
}
