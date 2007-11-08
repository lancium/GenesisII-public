package edu.virginia.vcgr.ogrsh.server.packing;

import java.io.IOException;

public interface IOGRSHWriteBuffer
{
	public void writeObject(Object object) throws IOException;
}