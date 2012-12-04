/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.sysinfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.morgan.util.io.StreamUtils;

public class ProcFilesystemProvider implements ISystemInfoProvider
{
	static private final String _CPU_INFO_FILE = "/proc/cpuinfo";
	static private final String _MEM_INFO_FILE = "/proc/meminfo";
	
	static private Pattern _CPU_SPEED_PATTERN = Pattern.compile(
		"^cpu MHz\\s*:\\s*(\\d+(?:.\\d+)?)$");
	
	static private Pattern _MEM_TOTAL_PATTERN = Pattern.compile(
		"^MemTotal:\\s*(\\d+)\\s*kB\\s*$");
	static private Pattern _MEM_FREE_PATTERN = Pattern.compile(
		"^MemFree:\\s*(\\d+)\\s*kB\\s*$");
	static private Pattern _SWAP_TOTAL_PATTERN = Pattern.compile(
		"^SwapTotal:\\s*(\\d+)\\s*kB\\s*$");
	static private Pattern _SWAP_FREE_PATTERN = Pattern.compile(
		"^SwapFree:\\s*(\\d+)\\s*kB\\s*$");
	
	public long getIndividualCPUSpeed()
	{
		double d = Double.parseDouble(
			getGroup(_CPU_INFO_FILE, _CPU_SPEED_PATTERN, 1));
		return (long)(d * 1000000L);
	}

	public long getPhysicalMemory()
	{
		return Long.parseLong(getGroup(_MEM_INFO_FILE, _MEM_TOTAL_PATTERN, 1))
			* 1024;
	}

	public long getPhysicalMemoryAvailable()
	{
		return Long.parseLong(getGroup(_MEM_INFO_FILE, _MEM_FREE_PATTERN, 1))
			* 1024;
	}

	public long getVirtualMemory()
	{
		return Long.parseLong(getGroup(_MEM_INFO_FILE, _SWAP_TOTAL_PATTERN, 1))
			* 1024;
	}

	public long getVirtualMemoryAvailable()
	{
		return Long.parseLong(getGroup(_MEM_INFO_FILE, _SWAP_FREE_PATTERN, 1))
			* 1024;
	}

	static private String getGroup(String file, Pattern p, int group)
	{
		BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new FileReader(file));
			String line;
			String toReturn = null;
			
			while ( (line = reader.readLine()) != null)
			{
				Matcher matcher = p.matcher(line);
				if (matcher.matches()) {
					toReturn = matcher.group(group);
					break;
				}
			}
			StreamUtils.close(reader);
			if (toReturn != null) return toReturn; 
			throw new RuntimeException("Couldn't find attribute");
		}
		catch (IOException ioe)
		{
			throw new RuntimeException("Couldn't find attribute:  " + ioe);
		}
		finally
		{
			StreamUtils.close(reader);
		}
	}
	
	// This method is currently un-implemented for Linux.
	public boolean getUserLoggedIn()
	{
		throw new RuntimeException(
			"User Logged-in method not implemented for Linux.");
	}
	
	// This method is currently un-implemented for Linux.
	public boolean getScreenSaverActive()
	{
		throw new RuntimeException(
			"ScreenSaver Active method not implemented for Linux.");
	}
}