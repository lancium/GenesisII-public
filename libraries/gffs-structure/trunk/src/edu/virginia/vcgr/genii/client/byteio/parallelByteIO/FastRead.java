package edu.virginia.vcgr.genii.client.byteio.parallelByteIO;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.transfer.AbstractByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;

public class FastRead implements Runnable
{
	static Log _logger = LogFactory.getLog(FastRead.class);

	private long startOffset; // starting offset in the file for this thread

	private RandomByteIOTransferer transferer; // Transferer associated with this thread

	private int block_read_size; // # of bytes per RPC for this thread

	private int threadId; // Among the threads created what is my id

	int bufferOffset; // where the bytes read should start being stored in the FillerAndChecker.

	private FillerAndChecker fac = null;

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

		int failureCount = 0;
		int MAX_FAILURES = 6; // maximum mulligans for axis attachments.

		try {
			/*
			 * hmmm: kludge engine below is to avoid trying to read an overly large chunk, since axis seems to choke badly on large
			 * attachments.
			 */
			int remainingLength = block_read_size;
			long currOffset = startOffset;
			while (remainingLength > 0) {
				
				int currentRead = 
					remainingLength;
					//hmmm: trying full length again above.
				//	Math.min(ByteIOConstants.MAXIMUM_READ_ATTEMPTED, remainingLength);
					//hmmm: the above is for chunking.
					
				if (_logger.isDebugEnabled())
					_logger.debug("thread " + threadId + " about to read at offset " + currOffset + " with block size " + currentRead);
				try {
					temp_buffer = transferer.read(currOffset, currentRead, 1, 0);
				} catch (RemoteException e) {
					if (e.getMessage().contains("Stream closed")) {
						// hmmm: make sure this assertion is true and that this is a harmless exception!!!!
						// ... so far, it has been harmless and the reads have succeeded even when we've seen this.
						_logger.debug("ignoring stream closed, which can happen after reading to eof.");
						break;
					} else if (e.getMessage().contains(AbstractByteIOTransferer.NO_ATTACHMENTS_FAILURE)
						|| e.getMessage().contains(EPRUtils.EXTRACTION_FAILURE)
						/* latest case, a null pointer exception from attachments... */
						||e.getMessage().contains("NullPointerException") ) {
						failureCount++;
						if (failureCount < MAX_FAILURES) {
							_logger.debug("caught axis attachment problem; retrying read at attempt #" + failureCount);
							// skip the copy of broken data and go try again.
							continue;
						} else {
							_logger.debug("axis attachment problem used up all attempts at #" + failureCount + " so bailing for real.", e);
							throw e;
						}
					} else {
						throw e;
					}
				}
				fac.copyFetch(temp_buffer, (currOffset - startOffset) + bufferOffset);
				if (temp_buffer.length >= currentRead) {
					// got the whole read.
					remainingLength -= currentRead;
					currOffset += currentRead;
				} else {
					remainingLength = -1; // bail out.
				}
			}
		} catch (Throwable t) {
			_logger.error("caught exception in run; recording as failure", t);
			fac.setErrorFlag(t);
		}

		fac.completed();
	}
}
