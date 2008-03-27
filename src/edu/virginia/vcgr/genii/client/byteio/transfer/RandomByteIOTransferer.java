package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.rmi.RemoteException;

public interface RandomByteIOTransferer extends ByteIOTransferer
{
	public byte[] read(long startOffset, int bytesPerBlock,
		int numBlocks, long stride) throws RemoteException;
	public void write(long startOffset, int bytesPerBlock,
		long stride, byte []data) throws RemoteException;
	public void append(byte []data) throws RemoteException;
	public void truncAppend(long offset, byte []data) throws RemoteException;
}