package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StreamableByteIOFactoryResourceFork extends ByteIOResourceFork
{
	public void snapshotState(OutputStream sink) throws IOException;

	public void modifyState(InputStream source) throws IOException;
}