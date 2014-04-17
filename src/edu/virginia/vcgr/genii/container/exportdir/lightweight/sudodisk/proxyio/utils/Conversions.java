package edu.virginia.vcgr.genii.container.exportdir.lightweight.sudodisk.proxyio.utils;

import java.nio.ByteBuffer;

/**
 * Helper methods for getting a long and int from 4 or 8 bytes respectively which have come from an
 * endpoint in network byte order (big endian)
 * 
 * @author avinash
 * 
 */
public class Conversions
{

	public static long getLongFromBytes(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8)
	{

		ByteBuffer bb = ByteBuffer.wrap(new byte[] { b1, b2, b3, b4, b5, b6, b7, b8 });
		long retVal = bb.getLong();

		return retVal;
	}

	public static int getIntFromBytes(byte b1, byte b2, byte b3, byte b4)
	{

		// Data is coming in NBO which is BigEndian!

		ByteBuffer bb = ByteBuffer.wrap(new byte[] { b1, b2, b3, b4 });
		int retVal = bb.getInt();

		return retVal;
	}
}
