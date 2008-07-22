package edu.virginia.vcgr.genii.client.utils.exec;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.morgan.util.io.StreamUtils;

public class StreamGatherer implements Closeable
{
	static private final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	static private final long DEFAULT_TIMEOUT = 1000L * 4;
	
	private Thread _streamThread;
	private ByteArrayOutputStream _sink;
	private InputStream _source;
	private IOException _ioe = null;
	
	public StreamGatherer(InputStream source)
	{
		_source = source;
		_sink = new ByteArrayOutputStream();
		
		_streamThread = new Thread(new GatherWorker());
		_streamThread.setName("Gather Worker");
		_streamThread.setDaemon(true);
		_streamThread.start();
	}
	
	public byte[] getData() throws IOException
	{
		return getData(DEFAULT_TIMEOUT);
	}
	
	public byte[] getData(long timeoutMS) throws IOException
	{
		if (_ioe != null)
			throw _ioe;
		
		long stopTime = System.currentTimeMillis() + timeoutMS;
		long interval;
		while (true)
		{
			interval = stopTime - System.currentTimeMillis();
			if (interval < 0)
				throw new IOException(
					"Timed out waiting for thread to finish.");
			
			try
			{
				_streamThread.join(interval);
				if (!_streamThread.isAlive())
					break;
			}
			catch (InterruptedException ie)
			{
				Thread.interrupted();
			}
		}
		
		return _sink.toByteArray();
	}
	
	protected void finalize()
	{
		close();
	}
	
	@Override
	synchronized public void close()
	{
		if (_source != null)
			StreamUtils.close(_source);
		_source = null;
	}
	
	private class GatherWorker implements Runnable
	{
		@Override
		public void run()
		{
			byte []data = new byte[DEFAULT_BUFFER_SIZE];
			int read;
	
			try
			{
				while ( (read = _source.read(data)) > 0)
				{
					_sink.write(data, 0, read);
				}
			}
			catch (IOException ioe)
			{
				_ioe = ioe;
			}
			finally
			{
				StreamUtils.close(_sink);
				close();
			}
		}
	}
}