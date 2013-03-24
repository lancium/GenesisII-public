package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;

/**
 * While the exact protocols for transferring bytes in ByteIO is extensible and therefore infinite
 * in cardinality, the various mechanisms all fall under two distinct categories -- random byteio
 * transfers, and streamable byte io transfers. This interface represents the ability to transfer
 * bytes for the streamable byteio case. Actually, it's an almost verbatim representation of the
 * interface supported by the ByteIO specification. It wouldn't have been strictly necessary to
 * codify this (a higher level abstraction might have been more appropriate for programmers) except
 * for the fact that for certain cases (i.e., to utilize some of the more obscure, web services
 * related provisions such as combined seek and read/write access), it was desirable in some cases
 * to have access to this protocol at this level.
 * 
 * @author mmm2a
 * 
 */
public interface StreamableByteIOTransferer extends ByteIOTransferer
{
	public void seekRead(SeekOrigin origin, long offset, ByteBuffer destination) throws RemoteException;

	public void seekWrite(SeekOrigin origin, long offset, ByteBuffer source) throws RemoteException;

	/**
	 * This method first seeks (if indicated) to a specific place in the target ByteIO's stream, and
	 * then reads a block of data from it.
	 * 
	 * @param origin
	 *            The seek origin (one of beginning, current, or end).
	 * @param offset
	 *            The offset for the seek operation (can be 0).
	 * @param numBytes
	 *            The number of bytes to read.
	 * 
	 * @return The block of data (if any) read from the remote byte io.
	 * 
	 * @throws RemoteException
	 */
	public byte[] seekRead(SeekOrigin origin, long offset, long numBytes) throws RemoteException;

	/**
	 * This method first seeks (if indicated) to a specific place in the target ByteIO's stream, and
	 * then writes a block of data to it.
	 * 
	 * @param origin
	 *            The seek origin (one of beginning, current, or end).
	 * @param offset
	 *            The offset for the seek operation (can be 0).
	 * @param data
	 *            The block of data to write.
	 * 
	 * @throws RemoteException
	 */
	public void seekWrite(SeekOrigin orgin, long offset, byte[] data) throws RemoteException;
}