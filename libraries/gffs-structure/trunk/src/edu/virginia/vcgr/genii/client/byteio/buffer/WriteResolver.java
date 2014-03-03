package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * This interface denotes the ability of some entity to resolve a request to write a set of bytes to
 * some sink (ostensibly, a file, though that is not required). This interface is used by the ByteIO
 * cache to "flush" out the bytes of a buffer to some "original" sink.
 * 
 * @author mmm2a
 */
public interface WriteResolver
{
	/**
	 * Write a bunch of bytes out to a target ByteIO.
	 * 
	 * @param fileOffset
	 *            The offset at which to begin writing bytes out.
	 * @param source
	 *            The source of the bytes to write.
	 * 
	 * @throws IOException
	 */
	public void write(long fileOffset, ByteBuffer source) throws IOException;

	/**
	 * Truncate the target file to some known length.
	 * 
	 * @param offset
	 *            The length to wich the target file should be truncated.
	 * @throws IOException
	 */
	public void truncate(long offset) throws IOException;
}
