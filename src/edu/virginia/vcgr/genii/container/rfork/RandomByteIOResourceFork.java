package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface RandomByteIOResourceFork extends ByteIOResourceFork
{
	public void read(long offset, ByteBuffer dest) throws IOException;

	public void write(long offset, ByteBuffer source) throws IOException;

	public void truncAppend(long offset, ByteBuffer source) throws IOException;
}