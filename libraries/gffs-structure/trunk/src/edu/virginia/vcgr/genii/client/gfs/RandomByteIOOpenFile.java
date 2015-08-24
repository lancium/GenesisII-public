package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.ClientProperties;
import edu.virginia.vcgr.genii.client.byteio.buffer.AppendResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.BasicFileOperator;
import edu.virginia.vcgr.genii.client.byteio.buffer.ByteIOBufferLeaser;
import edu.virginia.vcgr.genii.client.byteio.buffer.ReadResolver;
import edu.virginia.vcgr.genii.client.byteio.buffer.WriteResolver;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;

class RandomByteIOOpenFile extends OperatorBasedOpenFile
{
	static private Log _logger = LogFactory.getLog(RandomByteIOOpenFile.class);

	final static Semaphore WS = new Semaphore(getConfigs().max_writers);
	final static Semaphore RS = new Semaphore(getConfigs().max_readers);

	/**
	 * does a deep copy of one byte buffer into another.
	 */
	private static ByteBuffer deepCopy(ByteBuffer orig)
	{
		int pos = orig.position(), lim = orig.limit();
		try {
			orig.position(0).limit(orig.capacity()); // set range to entire buffer.
			ByteBuffer toReturn = deepCopyVisible(orig); // deep copy range.
			toReturn.position(pos).limit(lim); // set range to original.
			return toReturn;
		} finally {
			// do in finally in case something goes wrong; then we don't bork the original.
			orig.position(pos).limit(lim); // restore original
		}
	}

	private static ByteBuffer deepCopyVisible(ByteBuffer orig)
	{
		int pos = orig.position();
		try {
			ByteBuffer toReturn;
			// try to maintain implementation to keep performance
			if (orig.isDirect())
				toReturn = ByteBuffer.allocateDirect(orig.remaining());
			else
				toReturn = ByteBuffer.allocate(orig.remaining());

			toReturn.put(orig);
			toReturn.order(orig.order());

			return (ByteBuffer) toReturn.position(0);
		} finally {
			orig.position(pos);
		}
	}

	/**
	 * There is one of these for each read ahead buffer; in the limit there is just one for the current buffer
	 * 
	 * @author coder
	 * 
	 */
	static class AsynchReadBuffer implements Runnable
	{
		RandomByteIOTransferer _RBT;
		ArrayBlockingQueue<RandomByteIOTransferer> _tq;
		long _offset;
		int _size;
		Semaphore blocker;
		boolean ready;
		long _read;
		byte buf[];
		int ioerror = 0;

		public AsynchReadBuffer(ArrayBlockingQueue<RandomByteIOTransferer> tq, long offset, int size)
		{
			_tq = tq;
			_offset = offset;
			_size = size;
			blocker = new Semaphore(0);
			ready = false;
			_read = 0;
			buf = null;
		}

		@Override
		public void run()
		{
			try {
				int tries = 0;

				try {
					_RBT = _tq.take();
				} catch (InterruptedException e1) {
					_logger.error("Failed to get a transferer in RandomByteIOFile:read", e1);
					ioerror = 110;
					return;
				}
				try {
					while (tries < getConfigs().read_retries && !ready) {

						tries += 1;
						try {
							buf = _RBT.read(_offset, _size, 1, 0);
							_size = buf.length;
						}

						catch (RemoteException re) {
							_logger.error("Timed out during a read on host " + re.getCause().getMessage(), re);
							ioerror = 110; // Unix timeout error
							continue;
						}
						ready = true;
					}
				} finally {
					try {
						_tq.put(_RBT);
					} catch (InterruptedException e) {
						_logger.error("Failed to put a transferer in RandomByteIOFile:read", e);
					}
				}
			} finally {
				blocker.release();
				RS.release();
			}
		}

		public boolean isReady()
		{
			return ready;
		}

		public int ioError()
		{
			return ioerror;
		}

		public void waitfor()
		{
			blocker.acquireUninterruptibly();
			;
		}

	}

	/*
	 * Made the method synchonized so that only once call can happen at a time, and so that we can use a static variable as a temporary. The
	 * compiler will not let us allocate a local variable without an instance of RandomByteIOOpenFile existing. ARGH
	 */
	synchronized static private BasicFileOperator createOperator(EndpointReferenceType target) throws ResourceException,
		GenesisIISecurityException, RemoteException, IOException
	{

		TypeInformation typeInfo = new TypeInformation(target);
		long size = typeInfo.getByteIOSize();
		if (_logger.isDebugEnabled())
			_logger.debug("Creating random byteIO file of size " + size + " on host " + target.getAddress().get_value().getHost());

		RandomByteIOTransferer rbit[] = new RandomByteIOTransferer[getConfigs().read_buffers];
		ArrayBlockingQueue<RandomByteIOTransferer> tq = new ArrayBlockingQueue<RandomByteIOTransferer>(getConfigs().transferers);
		for (int i = 0; i < getConfigs().read_buffers; ++i) {
			rbit[i] = RandomByteIOTransfererFactory.createRandomByteIOTransferer(ClientUtils.createProxy(RandomByteIOPortType.class, target));
			tq.add(rbit[i]);
		}
		writersReaders synch = new writersReaders();
		;
		return new BasicFileOperator(ByteIOBufferLeaser.leaser(rbit[0].getTransferProtocol()), new ReadResolverImpl(tq, size, synch),
			new WriteResolverImpl(tq, synch), new AppendResolverImpl(tq), false);
	}

	RandomByteIOOpenFile(String[] path, EndpointReferenceType target, boolean canRead, boolean canWrite, boolean isAppend)
		throws ResourceException, GenesisIISecurityException, RemoteException, IOException
	{
		super(path, createOperator(target), canRead, canWrite, isAppend);

	}

	static private class ReadResolverImpl implements ReadResolver
	{

		// ASG's update for pipeline read

		private ArrayBlockingQueue<RandomByteIOTransferer> _tq;
		private long curP;
		long _size;
		AsynchReadBuffer bufs[];
		/*
		 * Has the file been read sequentially. Assume yes to start. Once it goes to false we stop doing read ahead. What is the point?
		 */
		boolean isSequential;
		writersReaders _synch;
		boolean firstRead;
		int _bufSize;

		public ReadResolverImpl(ArrayBlockingQueue<RandomByteIOTransferer> tq, long size, writersReaders synch)
		{
			_tq = tq;
			curP = 0;
			_synch = synch;
			isSequential = true; // Assume until we know otherwise that they are
									// doing sequential reads
			_size = size;
			bufs = new AsynchReadBuffer[getConfigs().read_buffers];
			_bufSize = getConfigs().long_buffer_size;
			firstRead = true;
		}

		private boolean chkBuffers(long fileOffset)
		{
			int count = 0;
			boolean found = false;
			/*
			 * The post condition is that fileOffset is in the range buf[0].offset .. buf[0].offset+buf[0]._size curP=fileOffset
			 * 
			 * Also, if the data was not in any buffer, we go to non-sequential mode, i.e., no read ahead. If fileoffset is in one of the
			 * buffers, we make that buf buf[0], and restart re-ahead.
			 */
			while (bufs[0] != null && isSequential) {
				// Note that we do NOT have to wait for the IO to complete to know if it will be in the buffer.
				if (fileOffset >= bufs[0]._offset && fileOffset < bufs[0]._offset + bufs[0]._size) {
					found = true;
					break;
				}
				/*
				 * It is not in bufs[0]. So, we are going to shift the array down. Note that by setting bufs[0]=0 we mostly make it garbage
				 * for the collector to reap. In principle it should not be cleaned up until the thread associated with the buffer terminates
				 * - which will only happen when the IO completes.
				 */
				count++;
				bufs[0] = null;
				for (int i = 0; i < getConfigs().read_buffers - 1; i++) {
					bufs[i] = bufs[i + 1]; // Shift them down, thank god for garbage collection.
				}
				bufs[getConfigs().read_buffers - 1] = null;
			}
			/*
			 * Ok, at this point either all the bufs are 0, or we found it. If we did not find it we are going into nonSequential, non-read
			 * ahead
			 */
			curP = fileOffset;
			if (!found) {
				isSequential = false;
				// Only start reading if we are not at eof
				_bufSize = getConfigs().short_buffer_size;
				if (fileOffset < _size)
					startload(0, fileOffset);
				return false;
			}
			/*
			 * We found it, now we need to make sure the prefetch buffers are fine re-Start the pre-fetch pipeline if count > 0 . if count==0
			 * then the data was in buf[0]
			 */

			if (count > 0) {
				// We were not in the first buffer, we need load
				for (int i = getConfigs().read_buffers - count; i < getConfigs().read_buffers; i++) {
					// Now we want to make sure the next read is less than file
					// size
					if (bufs[i - 1] != null && (fileOffset < bufs[i - 1]._offset + _bufSize)) {
						startload(i, bufs[i - 1]._offset + _bufSize);
					} else
						// We are past the end of file, stop
						break;
				}
			}
			return true;
		}

		private void startload(int bufid, long offset)
		{
			// Start loading the data up. Do not make this call if the old
			// buffer is not ready.
			// First make sure that any old reads have been completed.
			if (bufs[bufid] != null && !bufs[bufid].isReady()) {
				bufs[bufid].waitfor();
			}
			bufs[bufid] = new AsynchReadBuffer(_tq, offset, _bufSize);
			// AsynchReadBuffer(RandomByteIOTransferer RBT, long offset, int
			// size)
			RS.acquireUninterruptibly();
			Thread th = new Thread(bufs[bufid]);
			th.start();
			// System.err.println("Starting a thread to read a block at offset = "+offset);
		}

		@Override
		synchronized public void read(long fileOffset, ByteBuffer destination) throws IOException
		{
			/*
			 * This method is synchronized to ensure that two different readers to the same file do not enter with different file offsets and
			 * corrupt the read ahead buffer management code. In particular bufs, the instance variable read ahead buffer list.
			 */
			// The caller wants to read starting at file offset, length bytes

			int length = destination.remaining();
			int destOffset = 0;

			// System.err.println("fileOffset = " + fileOffset + " curP = " +
			// curP + " amount to read = "+length);
			_synch.startRead();// Don't read until all of the writes have
								// completed
			// Read ahead, assume we are doing sequential reads
			if (firstRead) {
				long offset = fileOffset;
				int curBuf = 0;
				while (offset < _size && curBuf < getConfigs().read_buffers) {
					startload(curBuf, offset);
					curBuf++;
					offset += _bufSize;
				}
				firstRead = false;
			}
			// We need to find out if the file offset is in the range of any of
			// the buffers we have, or that we have started loading.
			// If not, sadly we must dump a buffers, start loading
			if (!chkBuffers(fileOffset)) {
				// System.err.println("fileOffset = " + fileOffset +
				// " was not in the buffer");
			}
			// This next check is just a final check, the constructor should
			// have done this
			if (bufs[0] == null) {
				// Need to load up
				if (fileOffset < _size)
					startload(0, fileOffset);

			}
			if (bufs[0] != null && !bufs[0].isReady())
				bufs[0].waitfor();
			// Now we have something in bufs[0]. the buffer starts at bufs[0]._offset and has bufs[0].buf.length.

			/*
			 * Let's check if this a "normal" sequential read. is (fileOffset>=curP or fileOffst>= b0.offset) and fileOffset < _size
			 * chkBuffers called above ensures that fileOffset is in the current buffer
			 */
			if ((fileOffset >= curP || fileOffset >= bufs[0]._offset) && fileOffset < _size) {
				boolean full = false; // Have we got all the bytes?
				int remaining = length; // How many more bytes to get from the file.
				long workingOffset = fileOffset; // what is the current file offset we are reading.
				while (!full && bufs[0] != null && workingOffset < _size) {
					if (bufs[0] != null && !bufs[0].isReady())
						bufs[0].waitfor();
					if (bufs[0].ioError() != 0) {
						throw new IOException("Read failed after " + getConfigs().read_retries + " attempts");
					}
					if (workingOffset >= bufs[0]._offset && workingOffset < bufs[0]._offset + bufs[0].buf.length) {
						// The start is within the range
						int buf_offset = (int) (workingOffset - bufs[0]._offset);
						// buf_offset -- Where are we copying from in the buffer?

						int copybytes = remaining; // How many bytes are we copying, we know the upper bound is "remaining".
						// Make sure we only take what is available in the buf.
						if (copybytes > bufs[0].buf.length - buf_offset)
							copybytes = bufs[0].buf.length - buf_offset;
						// We now have the bytes to copy, the offset in the buffer.
						// System.err.println("copybytes is "+ copybytes);
						System.arraycopy(bufs[0].buf, buf_offset, destination.array(), destOffset, copybytes);
						// Need to update the position function of the byte buffer because arraycopy does not update it.
						destination.position(destOffset + copybytes);
						/*
						 * Note though that bufs[]._size may be greater than the data actually available, bufs[].buf.length if we passed end
						 * of file We want to put into destination update all of the counters and pointers by the number of bytes copied.
						 */
						destOffset += copybytes;
						remaining -= copybytes;
						workingOffset += copybytes;
						curP += copybytes;
						// System.err.println("got past the array copy");
						if (workingOffset >= _size)
							return; // We've read all of the bytes, lets go
						full = remaining == 0;
						boolean consumed = buf_offset + copybytes == bufs[0].buf.length; // The buffer has been used.
						if (consumed) {
							// System.err.println("CONSUMED - NEED TO LOAD");
							// Now start loading if there is more to read.
							chkBuffers(workingOffset);
							// System.err.println("CONSUMED - on the way out");
						}
					} else {
						// We will need to get more data, there are no bytes in bufs[0].
						_logger.error("Need to get more data, yet have no way to do so - LOGIC ERROR");
					}
				}
				// System.err.println("on the way out - bye bye");
				if (!full)
					throw new IOException("Could not read all of the bytes");
			} else {
				// Not able to deal with this, it is out of bounds or too far backwards.
				return;
			}
		}
	}

	static class AsynchWriteBuffer implements Runnable
	{
		RandomByteIOTransferer _RBT;
		ArrayBlockingQueue<RandomByteIOTransferer> _tq;
		long _offset;
		boolean done;
		long _read;
		ByteBuffer buf;
		writersReaders _synch;
		int ioerror;

		public AsynchWriteBuffer(ArrayBlockingQueue<RandomByteIOTransferer> tq, long offset, ByteBuffer source, writersReaders synch)
		{
			_tq = tq;
			_offset = offset;
			done = false;
			_read = 0;
			buf = source;
			_synch = synch;
		}

		@Override
		public void run()
		{
			int tries = 0;
			try {
				try {
					_RBT = _tq.take();
				} catch (InterruptedException e1) {
					_logger.error("Failed to get a transferer in RandomByteIOFile:write", e1);
					ioerror = 110;
					return;
				}
				try {

					while (tries < getConfigs().write_retries && !done) {
						tries += 1;
						try {
							// System.err.println("Starting a write to offset " +
							// _offset);
							_RBT.write(_offset, buf);
							// System.err.println("Completed a write to offset " +
							// _offset);

						} catch (RemoteException re) {
							_logger.error("Timed out during a write on host ", re);
							continue;
						}
						done = true;

					}
				} finally {
					try {
						_tq.put(_RBT);
					} catch (InterruptedException e) {
						_logger.error("Failed to put a transferer in RandomByteIOFile:write", e);
						ioerror = 110;
						return;
					}
				}

			} finally {
				_synch.endWrite();
				WS.release();
			}
		}
	}

	static private class WriteResolverImpl implements WriteResolver
	{
		ArrayBlockingQueue<RandomByteIOTransferer> _tq;
		writersReaders _synch;

		private WriteResolverImpl(ArrayBlockingQueue<RandomByteIOTransferer> tq, writersReaders synch)
		{
			_tq = tq;
			_synch = synch;
		}

		@Override
		public void truncate(long offset) throws IOException
		{
			try {
				RandomByteIOTransferer _transferer = _tq.take();
				_transferer.truncAppend(offset, new byte[0]);
				_tq.put(_transferer);
			} catch (InterruptedException e) {
				_logger.error("interrupted exception caught in truncate", e);
			}
		}

		@Override
		public void write(long fileOffset, ByteBuffer source)
		{
			if (_logger.isDebugEnabled())
				_logger.debug("Write to offset " + fileOffset + ", p " + source.limit());
			_synch.startWrite();

			// hmmm: is this code not used?
			// _transferer.write(fileOffset, source);

			AsynchWriteBuffer B;
			// Note the deepcopy of source.
			B = new AsynchWriteBuffer(_tq, fileOffset, deepCopy(source), _synch);

			WS.acquireUninterruptibly();

			Thread th = new Thread(B);
			th.start();

		}

		@Override
		public void drain()
		{
			if (_logger.isDebugEnabled())
				_logger.debug("Draining to close the file, writers = " + _synch.numWriters);

			_synch.waitForWritersToComplete(); // This has the effect of blocking the thread until all writes complete.

		}
	}

	static private class AppendResolverImpl implements AppendResolver
	{
		ArrayBlockingQueue<RandomByteIOTransferer> _tq;

		private AppendResolverImpl(ArrayBlockingQueue<RandomByteIOTransferer> tq)
		{
			_tq = tq;
		}

		@Override
		public void append(ByteBuffer source) throws IOException
		{
			// System.err.println("Appending, bytes " + source.limit());
			try {

				RandomByteIOTransferer _transferer = _tq.take();
				_transferer.append(source);
				_tq.put(_transferer);
			} catch (InterruptedException e) {
				_logger.warn("caught interrupted exception in append", e);
			}

		}
	}

	// do not use this member variable unless you are getConfigs() below.
	private static ByteIOConfigurationPack _privateConfigPack = null;

	/**
	 * management of the configuration package for this class. only getConfigs() should ever be called to access the configuration, which will
	 * ensure there is only one instance of the configuration package per process.
	 */
	public static ByteIOConfigurationPack getConfigs()
	{
		synchronized (RandomByteIOOpenFile.class) {
			if (_privateConfigPack != null)
				return _privateConfigPack;
			try {
				ClientProperties cp = ClientProperties.getClientProperties();
				_privateConfigPack = new ByteIOConfigurationPack(cp);
			} catch (Throwable t) {
				_logger.error("failure to establish static byte io configuration pack", t);
			}
			return _privateConfigPack;
		}
	}

}