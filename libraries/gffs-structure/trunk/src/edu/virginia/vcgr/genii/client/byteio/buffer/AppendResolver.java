package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface AppendResolver
{
	/**
	 * Append a bunch of bytes to the end of a ByteIO.
	 * 
	 * @param source
	 *            The source of the bytes to append.
	 * 
	 * @throws IOException
	 */
	public void append(ByteBuffer source) throws IOException;
}