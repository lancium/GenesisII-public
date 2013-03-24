package edu.virginia.vcgr.genii.container.sysinfo.macosx;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MacOSXSysInfoAccumulator
{
	static private Log _logger = LogFactory.getLog(MacOSXSysInfoAccumulator.class);

	static final private long UPDATE_CYCLE = 1000L * 8;

	static private Long _individualCPUSpeed = null;
	static private Long _physicalMemory = null;
	static final private long _virtualMemory = 1024 * 1024 * 1024 * 8;
	static final private long _virtualMemoryAvailable = _virtualMemory;

	static private MacOSXSysInfoAccumulator _accumulator = null;

	synchronized static public MacOSXSysInfoAccumulator accumulator()
	{
		if (_accumulator == null || (_accumulator._lastUpdated + UPDATE_CYCLE < System.currentTimeMillis()))
			_accumulator = new MacOSXSysInfoAccumulator();

		return _accumulator;
	}

	static public long individualCPUSpeed()
	{
		if (_individualCPUSpeed == null)
			accumulator();
		return _individualCPUSpeed;
	}

	static public long physicalMemory()
	{
		if (_physicalMemory == null)
			accumulator();
		return _physicalMemory;
	}

	static public long virtualMemory()
	{
		return _virtualMemory;
	}

	static public long virtualMemoryAvailable()
	{
		return _virtualMemoryAvailable;
	}

	static public boolean screenSaverActivte()
	{
		return false;
	}

	static public boolean userLoggedIn()
	{
		try {
			return WhoRunner.run().loggedIn().size() > 0;
		} catch (IOException ioe) {
			_logger.warn("Unable to determine who is logged in currently.", ioe);
			return false;
		}
	}

	static public long physicalMemoryAvailable()
	{
		return accumulator()._physicalMemoryAvailable;
	}

	private long _lastUpdated;
	private long _physicalMemoryAvailable;

	private MacOSXSysInfoAccumulator()
	{
		try {
			MacOSXSysCtlRunner sysInfo = MacOSXSysCtlRunner.run();
			VMStatRunner vm = VMStatRunner.run();

			_individualCPUSpeed = new Long(sysInfo.cpuSpeed());
			_physicalMemory = new Long(sysInfo.physicalMemory());
			_physicalMemoryAvailable = vm.memoryFree();
		} catch (IOException ioe) {
			_logger.warn("Unable to determine memory information for host.", ioe);
			_individualCPUSpeed = new Long(0);
			_physicalMemory = new Long(0);
			_physicalMemoryAvailable = new Long(0);
		}

		_lastUpdated = System.currentTimeMillis();
	}
}