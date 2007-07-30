package edu.virginia.vcgr.genii.client.byteio.xfer;

import java.io.Closeable;
import java.io.IOException;
import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;

public interface ISByteIOTransferer extends Closeable
{
	public byte[] seekRead(SeekOrigin origin, long offset, long numBytes)
		throws RemoteException;
	public void seekWrite(SeekOrigin origin, long offset, byte []data)
		throws RemoteException;
	
	public boolean endOfStream() throws RemoteException, IOException;
	public long position() throws RemoteException, IOException;
}