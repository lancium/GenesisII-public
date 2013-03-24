package edu.virginia.vcgr.genii.client.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public class PerformanceLogger implements Closeable
{
	static private Log _logger = LogFactory.getLog(PerformanceLogger.class);

	private PrintStream _ps = null;
	private long _startTime = -1L;
	private Long _stopTime = -1L;

	@Override
	protected void finalize()
	{
		close();
	}

	public PerformanceLogger(File file)
	{
		try {
			_ps = new PrintStream(new FileOutputStream(file, true));
		} catch (Throwable cause) {
			_logger.warn("Unable to open print stream.", cause);
		}
	}

	final public void start()
	{
		_startTime = System.currentTimeMillis();
	}

	final public void stop()
	{
		_stopTime = System.currentTimeMillis();
	}

	public void log()
	{
		_ps.format("Ellapsed time is %d milliseconds.\n", (_stopTime - _startTime));
	}

	@Override
	synchronized public void close()
	{
		StreamUtils.close(_ps);
		_ps = null;
	}
}