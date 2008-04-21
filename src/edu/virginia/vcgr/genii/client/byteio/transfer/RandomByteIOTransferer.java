package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.rmi.RemoteException;

/**
 * While the exact protocols for transferring bytes in ByteIO is
 * extensible and therefore infinite in cardinality, the various
 * mechanisms all fall under two distinct categories -- random byteio
 * transfers, and streamable byte io transfers.  This interface
 * represents the ability to transfer bytes for the random byteio
 * case.   Actually, it's an almost verbatim representation of the
 * interface supported by the ByteIO specification.  It wouldn't
 * have been strictly necessary to codify this (a higher level
 * abstraction might have been more appropriate for programmers) except
 * for the fact that for certain cases (i.e., to utilize some of the
 * more obscure, web services related provisions such as scatter/gather
 * and trunc with append), it was desirable in some cases to have
 * access to this protocol at this level.
 *  
 * @author mmm2a
 *
 */
public interface RandomByteIOTransferer extends ByteIOTransferer
{
	/**
	 * Read data from a remote Random ByteIO starting at a known
	 * offset, and with the possibilty of a gather operation of
	 * so many blocks at so large a block size and so long of a
	 * stride.
	 * 
	 * @param startOffset The offset in the remote byte io to start
	 * reading at.
	 * @param bytesPerBlock The nubmer of bytes in each block to read.
	 * @param numBlocks The number of blocks total to read.
	 * @param stride The distance between blocks (beginnings of blocks
	 * that is).
	 * @return The bytes that were read from the remote byte io.
	 * 
	 * @throws RemoteException
	 */
	public byte[] read(long startOffset, int bytesPerBlock,
		int numBlocks, long stride) throws RemoteException;
	
	/**
	 * Similar to the read method, this performs a classic scatter operation
	 * on a remote byteIO.  Bytes are written in blocks to a target random
	 * byteio at a given stride and offset.
	 * @param startOffset The start offset in the remote byteio at which to
	 * begin writing.
	 * @param bytesPerBlock The number of bytes per block to write.
	 * @param stride THe stride between blocks (beginnings of blocks) to 
	 * write.
	 * @param data The total data (representing the concatenated blocks that
	 * are being sent out).
	 * 
	 * @throws RemoteException
	 */
	public void write(long startOffset, int bytesPerBlock,
		long stride, byte []data) throws RemoteException;
	
	/**
	 * Append a block of data to a remote ByteIO resource.
	 * 
	 * @param data The block of data to append.
	 * 
	 * @throws RemoteException
	 */
	public void append(byte []data) throws RemoteException;
	
	/**
	 * A convenience operation that gives a user the ability to truncate a
	 * remote byteio to a given length and then append the data.  Because
	 * trunc and append often follow each other in standard use cases, they 
	 * are combined in the ByteIO case for message efficiency.  Note that
	 * degenerate cases occur where truncate can happen without an append.
	 * 
	 * @param offset The offset to which the byteio should be truncated.
	 * @param data The data to append (if any) at the end of the truncated 
	 * byteio.
	 * 
	 * @throws RemoteException
	 */
	public void truncAppend(long offset, byte []data) throws RemoteException;
}