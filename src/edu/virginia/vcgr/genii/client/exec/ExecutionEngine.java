package edu.virginia.vcgr.genii.client.exec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.io.TeeOutputStream;

public class ExecutionEngine
{
	static private Log _logger = LogFactory.getLog(ExecutionEngine.class);

	static private class StreamCopier extends Thread
	{
		private InputStream _input;
		private OutputStream _output;

		public StreamCopier(InputStream input, OutputStream output)
		{
			super("Stream Copier");
			setDaemon(true);

			_input = input;
			_output = output;
		}

		@Override
		public void run()
		{
			byte[] data = new byte[4 * 1024];
			int read;

			if (_input != null) {
				try {
					while ((read = _input.read(data)) > 0) {
						if (_output != null) {
							_output.write(data, 0, read);
							_output.flush();
						}
					}
				} catch (IOException ioe) {
					_logger.info("Unable to copy streams (IOException for input)", ioe);
				}
			}

			try {
				if (_output != null)
					_output.flush();
			} catch (IOException ioe) {
				_logger.info("Unable to copy streams (IOException for output)", ioe);
			}
		}
	}

	static final private Pattern SPACES = Pattern.compile("\\s+");

	static private void logCommandStart(ExecutionTask task, String[] cLine)
	{
		StringBuilder builder = null;
		for (String c : cLine) {
			if (SPACES.matcher(c).matches())
				c = String.format("\"%s\"", c);
			if (builder == null)
				builder = new StringBuilder(c);
			else {
				builder.append(" ");
				builder.append(c);
			}
		}

		_logger.info(String.format("Task(%s) -- Executing command:  %s", task, builder));
	}

	static public void execute(InputStream stdin, OutputStream stdout, OutputStream stderr, ExecutionTask task)
		throws ExecutionException, IOException
	{
		ByteArrayOutputStream outStorage = new ByteArrayOutputStream();
		ByteArrayOutputStream errStorage = new ByteArrayOutputStream();

		stdout = (stdout == null) ? outStorage : new TeeOutputStream(stdout, outStorage);
		stderr = (stderr == null) ? errStorage : new TeeOutputStream(stderr, errStorage);

		StreamCopier[] copiers = null;
		String[] cLine = task.getCommandLine();
		logCommandStart(task, cLine);
		ProcessBuilder builder = new ProcessBuilder(cLine);
		Process proc = builder.start();

		copiers = new StreamCopier[] { new StreamCopier(proc.getErrorStream(), stderr),
			new StreamCopier(proc.getInputStream(), stdout), new StreamCopier(stdin, proc.getOutputStream()) };
		for (StreamCopier copier : copiers)
			copier.start();

		int exitCode;
		while (true) {
			try {
				exitCode = proc.waitFor();
				proc = null;
				break;
			} catch (InterruptedException ie) {
			}
		}

		for (StreamCopier copier : copiers) {
			try {
				copier.join();
			} catch (InterruptedException ie) {
			}
		}

		byte[] data = outStorage.toByteArray();
		if (data != null && data.length > 0)
			_logger.info(String.format("Task(%s) -- Command produced output:  %s", task, new String(data)));

		data = errStorage.toByteArray();
		if (data != null & data.length > 0)
			_logger.warn(String.format("Task(%s) -- Command produced error:  %s", task, new String(data)));

		task.getResultsChecker().checkResults(exitCode);
	}
}
