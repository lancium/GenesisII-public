package edu.virginia.vcgr.genii.client.byteio.parallelByteIO;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FillerAndChecker
{
	static Log _logger = LogFactory.getLog(FillerAndChecker.class);

	private CountDownLatch cdl; // Latch waiting for all threads to finish
	private byte[] data; // contains contents of read
	private int lastFilledBufferIndex = -1;
	private boolean errorFlag = false; // flag denoting if any thread failed in its read
	private Throwable threadFailCause; // cause of thread-failure

	public FillerAndChecker(CountDownLatch cdl, int length)
	{
		data = new byte[length];
		this.cdl = cdl;
	}

	public void await() throws InterruptedException
	{
		cdl.await();
	}

	// Each thread copies the sub-fetch into the global buffer

	public void copyFetch(byte[] temp_buffer, long offset) throws IOException
	{
		if ((temp_buffer == null) || (temp_buffer.length == 0)) {
			//hmmm: clean out logging.
			_logger.debug("ignoring empty buffer");
			return;
		}
		//int index = threadID * subBufferSize;
		int index = (int)offset;  // hmmm, better not be past 2gig.
		
		if (index >= data.length) {
			String msg = "computed index is past main buffer end: index is " + index + " but buffer is only " + data.length + " bytes";
			_logger.error(msg);
			throw new IOException(msg);
		} else if (index + temp_buffer.length > data.length) {
			String msg = "chunk will overwrite main buffer end: index is " + index + " and temp buffer is " + temp_buffer.length + " bytes but buffer is only " + data.length + " bytes";
			_logger.error(msg);
			throw new IOException(msg);			
		}

		//hmmm: denoise this.
		_logger.debug("copying buffer of " + temp_buffer.length + " bytes into index " + index + " of parent buffer");

		synchronized (data) {
			System.arraycopy(temp_buffer, 0, data, index, temp_buffer.length);
		}
		setLastFilledBufferIndex(index + temp_buffer.length - 1);
	}

	/*
	 * Method to aid in checking if fetch was satisfied completely, partially or not satisfied at all
	 */

	synchronized private void setLastFilledBufferIndex(int index)
	{
		if (index > lastFilledBufferIndex)
			lastFilledBufferIndex = index;
	}

	public void completed()
	{
		cdl.countDown();
	}

	public void setErrorFlag(Throwable cause)
	{
		errorFlag = true; // read has failed
		setThreadFailCause(cause); // the reason why the read failed.
	}

	public boolean isErrorFlag()
	{
		return errorFlag;
	}

	public void setThreadFailCause(Throwable threadFailCause)
	{
		this.threadFailCause = threadFailCause;
	}

	public Throwable getThreadFailCause()
	{
		return threadFailCause;
	}

	public int getLastFilledBufferIndex()
	{
		return lastFilledBufferIndex;
	}

	public byte[] getData()
	{
		return data;
	}

}
