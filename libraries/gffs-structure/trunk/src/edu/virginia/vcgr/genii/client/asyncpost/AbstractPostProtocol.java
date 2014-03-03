package edu.virginia.vcgr.genii.client.asyncpost;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractPostProtocol implements PostProtocol
{
	static private Log _logger = LogFactory.getLog(AbstractPostProtocol.class);

	private String[] _handledProtocols;

	protected abstract void doPost(URI target, byte[] content) throws Throwable;

	protected AbstractPostProtocol(String[] handledProtocols)
	{
		_handledProtocols = handledProtocols;
	}

	@Override
	final public String[] handledProtocols()
	{
		return _handledProtocols;
	}

	@Override
	final public OutputStream postStream(URI target)
	{
		return new StoringOutputStream(target);
	}

	private class StoringOutputStream extends OutputStream
	{
		private ByteArrayOutputStream _baos = new ByteArrayOutputStream();
		private URI _target;

		private StoringOutputStream(URI target)
		{
			_target = target;
		}

		@Override
		public void close() throws IOException
		{
			_baos.close();
			Thread th = new Thread(new PostWorker(_target, _baos.toByteArray()), "Asynchronous Post Thread");
			th.setDaemon(true);
			th.start();
		}

		@Override
		public void flush() throws IOException
		{
			_baos.flush();
		}

		@Override
		public void write(int b) throws IOException
		{
			_baos.write(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			_baos.write(b, off, len);
		}

		@Override
		public void write(byte[] b) throws IOException
		{
			_baos.write(b);
		}
	}

	private class PostWorker implements Runnable
	{
		private URI _target;
		private byte[] _contents;

		private PostWorker(URI target, byte[] contents)
		{
			_target = target;
			_contents = contents;
		}

		@Override
		public void run()
		{
			try {
				doPost(_target, _contents);
			} catch (Throwable cause) {
				_logger.warn("Unable to post to remote target.", cause);
			}
		}
	}
}