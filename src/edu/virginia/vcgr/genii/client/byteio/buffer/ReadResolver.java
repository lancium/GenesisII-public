package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This interface denotes the ability of some entity to resolve a
 * request to read a set of bytes from some source (ostensibly, a file, though
 * that is not required).  This interface is used by the ByteIO cache to
 * "fill" in the bytes of a buffer from some "original" source.
 * 
 * @author mmm2a
 */
public interface ReadResolver
{
	/**
	 * Read a bunch of bytes from a target ByteIO.
	 * 
	 * @param fileOffset The offset at which to begin reading bytes.
	 * @param destination The destination buffer into which to place
	 * the bytes.  This method CAN return a short read.
	 * 
	 * @throws IOException
	 */
	public void read(long fileOffset,
		ByteBuffer destination) throws IOException;
}