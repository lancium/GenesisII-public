package edu.virginia.vcgr.genii.container.sysinfo.macosx;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.utils.exec.ExecutionEngine;

class VMStatRunner
{
	static private final Pattern FREE_PATTERN = Pattern.compile(
		"^.*Pages free:[^\\d]*(\\d+)[^\\d]*$");
	static private final Pattern PAGE_SIZE_PATTERN = Pattern.compile(
		"^.*page size of (\\d+) bytes.*$");
	
	private long _memoryFree;
	
	private VMStatRunner(long memoryFree)
	{
		_memoryFree = memoryFree;
	}
	
	long memoryFree()
	{
		return _memoryFree;
	}
	
	static VMStatRunner run() throws IOException
	{
		Matcher matcher;
		List<String> results = ExecutionEngine.simpleMultilineExecute(
			"vm_stat");
		
		long pageSize = -1l;
		long pagesFree = -1l;
		
		for (String line : results)
		{
			if (pageSize > 0 && pagesFree > 0)
				break;
			
			if (pageSize < 0)
			{
				matcher = PAGE_SIZE_PATTERN.matcher(line);
				if (matcher.matches())
				{
					pageSize = Long.parseLong(matcher.group(1));
					continue;
				}
			} else if (pagesFree < 0)
			{
				matcher = FREE_PATTERN.matcher(line);
				if (matcher.matches())
				{
					pagesFree = Long.parseLong(matcher.group(1));
				}
			}
		}
		
		return new VMStatRunner(pageSize * pagesFree);
	}
}