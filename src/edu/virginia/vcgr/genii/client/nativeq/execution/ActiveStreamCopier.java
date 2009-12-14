package edu.virginia.vcgr.genii.client.nativeq.execution;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

class ActiveStreamCopier extends Thread
{
	static private Log _logger = LogFactory.getLog(ActiveStreamCopier.class);
	
	private InputStream _input;
	private ProcessStreamSink _sink;
	
	ActiveStreamCopier(InputStream input, ProcessStreamSink sink)
	{
		super("Active Stream Copier Thread");
		
		_input = input;
		_sink = sink;
		
		setDaemon(true);
		start();
	}
	
	@Override
	final public void run()
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(_input));
		String line;
		
		try
		{
			while ( (line = reader.readLine()) != null)
			{
				if (_sink != null)
					_sink.addOutputLine(line);
			}
		}
		catch (Throwable cause)
		{
			_logger.error(
				"Unable to read stream from fork/exec'd process.", cause);
		}
		
		StreamUtils.close(_input);
	}
}