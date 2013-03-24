package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class TeeRedirectionSink implements StreamRedirectionSink
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(TeeRedirectionSink.class);

	private StreamRedirectionSink[] _sinks;

	public TeeRedirectionSink(StreamRedirectionSink... sinks)
	{
		_sinks = sinks;
	}

	@Override
	public OutputStream openSink(ExecutionContext context) throws IOException
	{
		TeeOutputStream ret = new TeeOutputStream();
		for (StreamRedirectionSink sink : _sinks) {
			ret.addTarget(sink.openSink(context));
		}

		return ret;
	}

	static private class TeeOutputStream extends OutputStream
	{
		private Collection<OutputStream> _targets = new LinkedList<OutputStream>();

		public void addTarget(OutputStream target)
		{
			_targets.add(target);
		}

		@Override
		public void close() throws IOException
		{
			Iterator<OutputStream> streamIter = _targets.iterator();
			while (streamIter.hasNext()) {
				OutputStream out = streamIter.next();
				try {
					out.close();
				} catch (IOException ioe) {
					_logger.warn("Removing tee'd redirection stream " + "because it threw an exception.", ioe);
					streamIter.remove();
				}
			}
		}

		@Override
		public void flush() throws IOException
		{
			Iterator<OutputStream> streamIter = _targets.iterator();
			while (streamIter.hasNext()) {
				OutputStream out = streamIter.next();
				try {
					out.flush();
				} catch (IOException ioe) {
					_logger.warn("Removing tee'd redirection stream " + "because it threw an exception.", ioe);
					streamIter.remove();
				}
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException
		{
			Iterator<OutputStream> streamIter = _targets.iterator();
			while (streamIter.hasNext()) {
				OutputStream out = streamIter.next();
				try {
					out.write(b, off, len);
				} catch (IOException ioe) {
					_logger.warn("Removing tee'd redirection stream " + "because it threw an exception.", ioe);
					streamIter.remove();
				}
			}
		}

		@Override
		public void write(byte[] b) throws IOException
		{
			Iterator<OutputStream> streamIter = _targets.iterator();
			while (streamIter.hasNext()) {
				OutputStream out = streamIter.next();
				try {
					out.write(b);
				} catch (IOException ioe) {
					_logger.warn("Removing tee'd redirection stream " + "because it threw an exception.", ioe);
					streamIter.remove();
				}
			}
		}

		@Override
		public void write(int b) throws IOException
		{
			Iterator<OutputStream> streamIter = _targets.iterator();
			while (streamIter.hasNext()) {
				OutputStream out = streamIter.next();
				try {
					out.write(b);
				} catch (IOException ioe) {
					_logger.warn("Removing tee'd redirection stream " + "because it threw an exception.", ioe);
					streamIter.remove();
				}
			}
		}
	}
}