package edu.virginia.vcgr.genii.container.sysinfo.macosx;

import java.io.IOException;
import java.util.List;

import edu.virginia.vcgr.genii.client.utils.exec.ExecutionEngine;

class MacOSXSysCtlRunner
{
	static final private String CPU_FREQUENCY_KEY = "hw.cpufrequency";
	static final private String MEMSIZE_KEY = "hw.memsize";

	private long _cpuSpeed;
	private long _physicalMemory;

	private MacOSXSysCtlRunner(long cpuSpeed, long physicalMemory)
	{
		_cpuSpeed = cpuSpeed;
		_physicalMemory = physicalMemory;
	}

	long cpuSpeed()
	{
		return _cpuSpeed;
	}

	long physicalMemory()
	{
		return _physicalMemory;
	}

	static MacOSXSysCtlRunner run() throws IOException
	{
		List<String> results = ExecutionEngine.simpleMultilineExecute("sysctl", "-n", CPU_FREQUENCY_KEY, MEMSIZE_KEY);

		return new MacOSXSysCtlRunner(Long.parseLong(results.get(0)), Long.parseLong(results.get(1)));
	}
}