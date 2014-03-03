package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;
import java.nio.ByteBuffer;

import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;

public interface StreamableByteIOResourceFork extends ByteIOResourceFork
{
	public long getPosition();

	public boolean getSeekable();

	public boolean getEndOfStream();

	public boolean getDestroyOnClose();

	public void seekRead(SeekOrigin origin, long seekOffset, ByteBuffer destination) throws IOException;

	public void seekWrite(SeekOrigin origin, long seekOffset, ByteBuffer source) throws IOException;
}