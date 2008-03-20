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

public class SystemInfoUtils
{
	static private ISystemInfoProvider _provider;
	static
	{
		SupportedOperatingSystems os = SupportedOperatingSystems.current();

		if (os.equals(SupportedOperatingSystems.LINUX))
			_provider = new ProcFilesystemProvider();
		else if (os.equals(SupportedOperatingSystems.WINDOWS))
			_provider = new WindowsProvider();
		else
			throw new RuntimeException(
				"Don't know an ISystemInfoProvider for OS type \"" +
				os + "\".");
	}
	
	static private ISystemInfoProvider getProvider()
	{
		return _provider;
	}
	
	static public long getIndividualCPUSpeed()
	{
		return getProvider().getIndividualCPUSpeed();
	}
	
	static public long getPhysicalMemory()
	{
		return getProvider().getPhysicalMemory();
	}
	
	static public long getPhysicalMemoryAvailable()
	{
		return getProvider().getPhysicalMemoryAvailable();
	}
	
	static public long getVirtualMemory()
	{
		return getProvider().getVirtualMemory();
	}
	
	static public long getVirtualMemoryAvailable()
	{
		return getProvider().getVirtualMemoryAvailable();
	}

	static public boolean getUserLoggedIn()
	{
		return getProvider().getUserLoggedIn();
	}
	
	static public boolean getScreenSaverActive()
	{
		return getProvider().getScreenSaverActive();
	}
}
