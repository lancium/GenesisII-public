package edu.virginia.vcgr.genii.client.byteio.cache;

import java.io.IOException;

/**
 * This interface denotes the ability of some entity to resolve a
 * request to write a set of bytes to some sink (ostensibly, a file, though
 * that is not required).  This interface is used by the ByteIO cache to
 * "flush" out the bytes of a buffer to some "original" sink.
 * 
 * @author mmm2a
 */
public interface WriteResolver
{
	/**
	 * Write an array of bytes to some destination.
	 * 
	 * @param fileOffset The offset within the target to which to
	 * begin writing.
	 * @param source The source buffer of bytes from which to flush.
	 * @param sourceOffset The offset within the source buffer at which
	 * to begin reading bytes.
	 * @param length The number of bytes to flush.
	 * @throws IOException
	 */
	public void write(long fileOffset, byte []source, 
		int sourceOffset, int length) throws IOException;
	
	/**
	 * Truncate the target file to some known length.
	 * 
	 * @param offset The length to wich the target file should be truncated.
	 * @throws IOException
	 */
	public void truncate(long offset) throws IOException;
}
