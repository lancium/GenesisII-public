package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.LinkedList;

import org.ggf.rbyteio.RandomByteIOPortType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ByteIOPerformanceTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dbyteioperf";
	static final private FileResource _USAGE = new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/usage/ubyteioperf");
	static private Log _logger = LogFactory.getLog(ByteIOPerformanceTool.class);

	static final private FileResource _MANPAGE = new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/man/byteioperf");

	static private class WorkRequest
	{
		private long _startByte;
		private boolean _completed = false;

		public void waitForCompletion() throws InterruptedException
		{
			synchronized (this) {
				while (!_completed)
					wait();
			}
		}

		public void completed()
		{
			synchronized (this) {
				_completed = true;
				notifyAll();
			}
		}
	}

	private Long _lastByte = null;
	private LinkedList<WorkRequest> _queue = new LinkedList<WorkRequest>();

	private class Worker implements Runnable
	{
		private ByteBuffer _block;
		private RandomByteIOTransferer _source;

		private Worker(RandomByteIOTransferer source, int blockSize) throws ResourceException, GenesisIISecurityException,
			RemoteException, IOException
		{
			_block = ByteBuffer.allocate(blockSize);
			_source = source;
		}

		@Override
		public void run()
		{
			try {
				WorkRequest wr = null;
				while (_lastByte == null) {
					wr = null;

					synchronized (_queue) {
						if (_queue.isEmpty()) {
							_queue.wait();
							continue;
						} else
							wr = _queue.removeFirst();
					}

					if (_lastByte != null)
						break;

					_block.rewind();
					_source.read(wr._startByte, _block);

					if (_block.remaining() > 0) {
						_block.flip();
						_lastByte = new Long(wr._startByte + _block.remaining());
					}

					wr.completed();
				}

				synchronized (_queue) {
					for (WorkRequest request : _queue) {
						request.completed();
					}

					_queue.clear();
				}
			} catch (Throwable cause) {
				_logger.info("exception occurred in run", cause);
			}
		}
	}

	private long readFile(int numThreads, int blockSize) throws InterruptedException
	{
		LinkedList<WorkRequest> requestList = new LinkedList<WorkRequest>();
		long nextRequest = 0;

		while (_lastByte == null) {
			while (requestList.size() < numThreads) {
				WorkRequest request = new WorkRequest();
				request._startByte = nextRequest;
				nextRequest += blockSize;
				requestList.addLast(request);
				synchronized (_queue) {
					_queue.addLast(request);
					_queue.notify();
				}
			}

			WorkRequest request = requestList.removeFirst();
			request.waitForCompletion();
		}

		return _lastByte;
	}

	public ByteIOPerformanceTool()
	{
		super(new FileResource(_DESCRIPTION), _USAGE, true, ToolCategory.INTERNAL);
		addManPage(_MANPAGE);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath source = lookup(new GeniiPath(getArgument(0)));

		boolean testRPC = false;

		if (testRPC) {
			RPCTest(source.getEndpoint());
			return 0;
		}

		int blockSize = Integer.parseInt(getArgument(1));
		int numThreads = Integer.parseInt(getArgument(2));
		long startTime;
		long stopTime;
		long bytesTransferred;

		for (int lcv = 0; lcv < numThreads; lcv++) {
			RandomByteIOTransferer sourceT = RandomByteIOTransfererFactory.createRandomByteIOTransferer(ClientUtils
				.createProxy(RandomByteIOPortType.class, source.getEndpoint()));
			Thread th = new Thread(new Worker(sourceT, blockSize));
			th.setDaemon(true);
			th.start();
		}

		startTime = System.currentTimeMillis();
		bytesTransferred = readFile(numThreads, blockSize);
		stopTime = System.currentTimeMillis();

		stdout.format("Transfered %d bytes in %d milliseconds\n", bytesTransferred, (stopTime - startTime));

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 3)
			throw new InvalidToolUsageException();
	}

	private boolean RPCTest(EndpointReferenceType target) throws RemoteException
	{
		stdout.println("Running 100 RPCs.");
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, target);
		long start = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
			common.ping("Hello, World!");
		long stop = System.currentTimeMillis();

		stdout.format("It took %d ms to run them.  That's %.2f ms/rpc.\n", (stop - start), (stop - start) / 100.0);

		return true;
	}
}
