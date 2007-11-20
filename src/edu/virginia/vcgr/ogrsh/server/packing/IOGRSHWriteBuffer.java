package edu.virginia.vcgr.ogrsh.server.packing;

import java.io.IOException;

public interface IOGRSHWriteBuffer
{
	public void writeRaw(byte []data, int offset, int length) throws IOException;
	public void writeObject(Object object) throws IOException;
}