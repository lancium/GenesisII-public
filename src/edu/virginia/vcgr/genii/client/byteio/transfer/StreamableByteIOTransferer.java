package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;

public interface StreamableByteIOTransferer
{
	public int getPreferredReadSize();
	public int getPreferredWriteSize();
	
	public byte[] seekRead(SeekOrigin origin, long offset, long numBytes)
		throws RemoteException;
	public void seekWrite(SeekOrigin orgin, long offset, byte []data)
		throws RemoteException;
}