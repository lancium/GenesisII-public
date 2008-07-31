package edu.virginia.vcgr.genii.client.byteio.buffer;

import java.io.IOException;

public interface AppendResolver
{
	public void append(byte []data, int start, int length)
		throws IOException;
}