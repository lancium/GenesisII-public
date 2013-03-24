package edu.virginia.vcgr.genii.client.mem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LowMemoryExitHandler implements LowMemoryHandler
{
	static private Log _logger = LogFactory.getLog(LowMemoryExitHandler.class);

	private int _exitCode;

	public LowMemoryExitHandler(int exitCode)
	{
		_exitCode = exitCode;
	}

	@Override
	public void lowMemoryWarning(long usedMemory, long maxMemory)
	{
		_logger.warn(String.format("Received a low memory notification -- exiting with code %d", _exitCode));
		System.exit(_exitCode);
	}
}