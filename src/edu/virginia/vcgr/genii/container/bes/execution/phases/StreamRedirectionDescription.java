package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class StreamRedirectionDescription
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(
		StreamRedirectionDescription.class);
	
	private StreamRedirectionSource _stdin;
	private StreamRedirectionSink _stdout;
	private StreamRedirectionSink _stderr;
	
	public StreamRedirectionDescription(StreamRedirectionSource stdinSource,
		StreamRedirectionSink stdoutSink, StreamRedirectionSink stderrSink)
	{
		_stdin = stdinSource;
		_stdout = stdoutSink;
		_stderr = stderrSink;
	}
	
	public void enact(ExecutionContext context, OutputStream stdin, 
		InputStream stdout, InputStream stderr)
	{
		redirect(context, stdin, _stdin);
		redirect(context, _stdout, stdout);
		redirect(context, _stderr, stderr);
	}
	
	static private void redirect(ExecutionContext context,
		OutputStream sink, StreamRedirectionSource source)
	{
		if (source != null)
		{
			try
			{
				Thread thread = new Thread(
					new StreamRedirector(source.openSource(context), sink),
					"BES Stream Redirector Thread");
				thread.setDaemon(false);
				thread.start();
				
				return;
			}
			catch (Throwable cause)
			{
				_logger.error("Unable to redirect stream.", cause);
			}
		}
		
		StreamUtils.close(sink);
	}
	
	static private void redirect(ExecutionContext context,
		StreamRedirectionSink sink, InputStream source)
	{
		try
		{
			Thread thread = new Thread(
				new StreamRedirector(source, 
					sink == null ? null : sink.openSink(context)),
				"BES Stream Redirector Thread");
			thread.setDaemon(false);
			thread.start();
				
			return;
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to redirect stream.", cause);
		}
		
		StreamUtils.close(source);
	}
	
	static private class StreamRedirector implements Runnable
	{
		private InputStream _in;
		private OutputStream _out;
		
		public StreamRedirector(InputStream in, OutputStream out)
		{
			_in = in;
			_out = out;
		}
		
		public void run()
		{
			try
			{
				StreamUtils.copyStream(_in, _out, true);
			}
			catch (IOException ioe)
			{
				_logger.error("Error redirecting process tream.");
			}
			finally
			{
				StreamUtils.close(_in);
				StreamUtils.close(_out);
			}
		}
	}
}