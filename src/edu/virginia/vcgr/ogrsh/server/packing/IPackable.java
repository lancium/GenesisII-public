package edu.virginia.vcgr.ogrsh.server.packing;

import java.io.IOException;

public interface IPackable
{
	public void pack(IOGRSHWriteBuffer buffer) throws IOException;
	public void unpack(IOGRSHReadBuffer buffer) throws IOException;
}