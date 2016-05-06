package edu.virginia.vcgr.genii.client.byteio.parallelByteIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;

public class FastRead implements Runnable
{
	static Log _logger = LogFactory.getLog(FastRead.class);

	private long startOffset; // starting offset in the file for this thread
	private RandomByteIOTransferer transferer; // Transferer associated with this thread
	private int block_read_size; // # of bytes per RPC for this thread
	private int threadId; // Among the threads created what is my id
	int bufferOffset; // where the bytes read should start being stored in the FillerAndChecker.
	private FillerAndChecker fac = null; // manages the buffers for ongoing parallel reads.

	/**
	 * this function reads a piece of a file beginning in the file at "startOffset" of length "blkReadSize". the data received will be stored
	 * into "fac" at a position starting at the "bufferOffset". The buffer offset is needed since the file's position and the buffer size for
	 * a read are somewhat unrelated.
	 */
	public FastRead(RandomByteIOTransferer transferer, long startOffset, int blkReadSize, FillerAndChecker fac, int threadId,
		int bufferOffset)
	{
		this.startOffset = startOffset;
		this.transferer = transferer;
		this.block_read_size = blkReadSize;
		this.threadId = threadId;
		this.fac = fac;
		this.bufferOffset = bufferOffset;

		if (_logger.isDebugEnabled())
			_logger
				.debug("FastRead thread " + threadId + " will read a chunk at offset " + startOffset + " with read size " + block_read_size);
	}

	@Override
	public void run()
	{
		byte[] temp_buffer = null;
		try {
			if (_logger.isDebugEnabled())
				_logger.debug("thread " + threadId + " about to read at offset " + startOffset + " with block size " + block_read_size);
			temp_buffer = transferer.read(startOffset, block_read_size, 1, 0);
			if (_logger.isDebugEnabled())
				_logger.debug("copying data from offset " + startOffset + " into buffer at index " + bufferOffset);
			fac.copyFetch(temp_buffer, bufferOffset);
		} catch (Throwable t) {
			_logger.error("caught exception in run; recording as failure", t);
			fac.setErrorFlag(t);
		}

		fac.completed();
	}
}
