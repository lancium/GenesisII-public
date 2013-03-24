package edu.virginia.vcgr.genii.client.byteio.parallelByteIO;

import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;

public class FastRead implements Runnable
{

	private long startOffset; // starting offset in the file for this thread

	private RandomByteIOTransferer transferer; // Transferer associated with this thread

	private int block_read_size; // # of bytes per RPC for this thread

	private int threadId; // Among the threads created what is my id

	private int subBufferSize;
	// Each thread will fill parent stream's byte-buffer at index (threadId - 1)*subBufferSize

	private FillerAndChecker fac = null;

	public FastRead(RandomByteIOTransferer transferer, long startOffset, int blkReadSize, FillerAndChecker fac, int threadId,
		int subBufferSize)
	{
		this.startOffset = startOffset;
		this.transferer = transferer;
		block_read_size = blkReadSize;
		this.threadId = threadId;
		this.fac = fac;
		this.subBufferSize = subBufferSize;
	}

	@Override
	public void run()
	{
		try {
			byte[] temp_buffer = transferer.read(startOffset, block_read_size, 1, 0);
			fac.copyFetch(temp_buffer, threadId, subBufferSize);
		}

		catch (RemoteException re) {
			fac.setErrorFlag(re);
		}

		fac.completed();
	}
}
