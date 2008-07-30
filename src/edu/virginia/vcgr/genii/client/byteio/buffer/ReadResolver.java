package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.IOException;

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
	 * Read an array of bytes from some source.
	 * 
	 * @param fileOffset The offset within the target source from which to
	 * begin reading.
	 * @param destination The destination buffer into which the source bytes
	 * should be placed.
	 * @param destinationOffset The offset within the destination buffer at
	 * which to begin placing bytes.
	 * @param length The number of bytes to read.
	 * @return The number of bytes actually read.
	 * @throws IOException
	 */
	public int read(long fileOffset, byte []destination, 
		int destinationOffset, int length) throws IOException;
}