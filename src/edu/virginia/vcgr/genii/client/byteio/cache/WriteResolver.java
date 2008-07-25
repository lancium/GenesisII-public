package edu.virginia.vcgr.genii.client.byteio.cache;

import java.io.IOException;

public interface WriteResolver
{
	public void write(long fileOffset, byte []source, 
		int sourceOffset, int length) throws IOException;
	
	public void truncate(long offset) throws IOException;
}
