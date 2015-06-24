package edu.virginia.vcgr.smb.server;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;

public class SMBIOCache
{
	// 16 MB
	private static final int XFER_LOG_SIZE = 24;
	private static final int XFER_SIZE = (1 << XFER_LOG_SIZE);

	// 30 seconds
	private static final int TIMEOUT = 30;

	private class Waiter extends Thread
	{
		public void run()
		{
			try {
				Thread.sleep(TIMEOUT * 1000);
			} catch (InterruptedException e) {

			}

			synchronized (SMBIOCache.this.cached) {
				SMBIOCache.this.waiting = false;
			}

			try {
				SMBIOCache.this.doFlushSync();
			} catch (SMBException e) {

			}
		}
	}

	private class Writer extends Thread
	{
		private byte[] data;
		private long off;

		public Writer(byte[] data, long off)
		{
			this.data = data;
			this.off = off;
		}

		public void run()
		{
			try {
				SMBIOCache.this.transferer.write(off, XFER_SIZE, XFER_SIZE, data);
			} catch (RemoteException e) {

			}
		}
	}

	private RandomByteIOTransferer transferer;
	private ByteBuffer cached = ByteBuffer.allocate(XFER_SIZE);
	private long curBlock = (-1 >> XFER_LOG_SIZE);
	// The number of bytes available for reading from this block
	private int readAvail = 0;
	private boolean dirty = false;
	private boolean waiting = false;

	private ByteBuffer slice(int pos, int len)
	{
		ByteBuffer ret = cached.slice();
		ret.position(pos);
		if (pos + len > readAvail)
			ret.limit(readAvail);
		else
			ret.limit(len + pos);
		return ret;
	}

	public SMBIOCache(RandomByteIOTransferer transferer)
	{
		this.transferer = transferer;
	}

	private void doFlushSync() throws SMBException
	{
		byte[] data;
		long off;

		synchronized (cached) {
			if (!dirty)
				return;

			data = new byte[readAvail];
			cached.slice().get(data);
			off = curBlock << XFER_LOG_SIZE;

			dirty = false;
		}

		try {
			this.transferer.write(off, XFER_SIZE, XFER_SIZE, data);
		} catch (RemoteException e) {
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}

	private void doFlushAsync()
	{
		byte[] data;
		long off;

		synchronized (cached) {
			if (!dirty)
				return;

			data = new byte[readAvail];
			cached.slice().get(data);
			off = curBlock << XFER_LOG_SIZE;

			dirty = false;
		}

		Writer w = new Writer(data, off);
		w.start();
	}

	private void doLoadBlock(long block) throws SMBException
	{
		synchronized (cached) {
			// data must be flushed now
			doFlushAsync();

			curBlock = block;
		}

		long off = block << XFER_LOG_SIZE;
		try {
			byte[] data = this.transferer.read(off, XFER_SIZE, 1, XFER_SIZE);
			cached.slice().put(data);
			readAvail = data.length;
		} catch (RemoteException e) {
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}

	public void write(ByteBuffer data, long off) throws SMBException
	{
		int size = data.remaining();
		long startBlock = off >> XFER_LOG_SIZE;
		long endBlock = (off + size - 1) >> XFER_LOG_SIZE;
		long startOff = off - (startBlock << XFER_LOG_SIZE);
		long endOff = off + size - (endBlock << XFER_LOG_SIZE);

		while (true) {
			if (startBlock == curBlock && endBlock == curBlock) {
				// Fits completely
				ByteBuffer place = cached.slice();
				place.position((int) startOff);
				synchronized (cached) {
					place.put(data);
					dirty = true;
					// Maybe we extended the file
					if (startOff + size > readAvail)
						readAvail = (int) (startOff + size);
				}

				break;
			} else if (startBlock == curBlock) {
				// Spans forward boundary
				long boundary = endBlock << XFER_LOG_SIZE;
				long firstSize = boundary - startOff;

				ByteBuffer first = cached.slice();
				first.position((int) startOff);
				first.limit((int) firstSize);
				synchronized (cached) {
					first.put(data);
					dirty = true;
					// Entire block is readable
					readAvail = XFER_SIZE;
				}

				doLoadBlock(endBlock);
				startBlock++;
				startOff = 0;
			} else if (endBlock == curBlock) {
				// Spans backward boundary
				long boundary = endBlock << XFER_LOG_SIZE;
				long secondSize = boundary - startOff;

				ByteBuffer tmp = data.slice();
				tmp.position((int) secondSize);
				ByteBuffer first = cached.slice();
				synchronized (cached) {
					first.put(tmp);
					dirty = true;
					// Maybe we extended the file
					if (endOff > readAvail)
						readAvail = (int) endOff;
				}

				doLoadBlock(startBlock);
				endBlock--;
				endOff = XFER_SIZE;
			} else {
				// Find correct location
				doLoadBlock(startBlock);
			}
		}

		synchronized (cached) {
			if (!waiting) {
				waiting = true;
				Waiter w = new Waiter();
				w.start();
			}
		}
	}

	public int read(ByteBuffer data, long off) throws SMBException
	{
		int size = data.remaining();
		long startBlock = off >> XFER_LOG_SIZE;
		long endBlock = (off + size - 1) >> XFER_LOG_SIZE;
		long startOff = off - (startBlock << XFER_LOG_SIZE);
		long endOff = off + size - (endBlock << XFER_LOG_SIZE);

		while (true) {
			if (startBlock == curBlock && endBlock == curBlock) {
				// Fits completely
				synchronized (cached) {
					ByteBuffer place = slice((int) startOff, size);
					data.put(place);
				}

				break;
			} else if (startBlock == curBlock) {
				// Spans forward boundary
				long boundary = endBlock << XFER_LOG_SIZE;
				long firstSize = boundary - startOff;

				synchronized (cached) {
					ByteBuffer first = slice((int) startOff, (int) firstSize);
					data.put(first);
				}

				doLoadBlock(endBlock);
				startBlock++;
				startOff = 0;
			} else if (endBlock == curBlock) {
				// Spans backward boundary
				long boundary = endBlock << XFER_LOG_SIZE;
				long firstSize = endOff - boundary;
				long secondSize = boundary - startOff;

				ByteBuffer tmp = data.slice();
				tmp.position((int) secondSize);
				synchronized (cached) {
					ByteBuffer first = slice((int) startOff, (int) firstSize);
					tmp.put(first);
				}

				doLoadBlock(startBlock);
				endBlock--;
				endOff = XFER_SIZE;
			} else {
				// Find correct location
				doLoadBlock(startBlock);
			}
		}

		return size;
	}

	public void truncate(long size) throws SMBException
	{
		try {
			transferer.truncAppend(size, new byte[0]);
			doLoadBlock(curBlock);
		} catch (RemoteException e) {
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}

	public void close() throws SMBException
	{
		doFlushSync();
	}
}
