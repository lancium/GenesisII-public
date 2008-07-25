package edu.virginia.vcgr.genii.client.byteio.cache;

import java.io.IOException;

public interface ReadResolver
{
	public int read(long fileOffset, byte []destination, 
		int destinationOffset, int length) throws IOException;
}