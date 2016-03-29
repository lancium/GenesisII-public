/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.client.sysinfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.OperatingSystemType.OperatingSystemTypes;

public class SystemInfoUtils
{
	static private Log _logger = LogFactory.getLog(SystemInfoUtils.class);

	static private ISystemInfoProvider _provider;

	static {
		OperatingSystemTypes osType = OperatingSystemType.getCurrent();

		if (osType == OperatingSystemTypes.LINUX)
			_provider = new ProcFilesystemProvider();
		else if (OperatingSystemType.isWindows())
			_provider = new WindowsProvider();
		else if (osType == OperatingSystemTypes.MACOS)
			_provider = new MacOSXProvider();
		else
			throw new RuntimeException("Don't know an ISystemInfoProvider for OS type \"" + osType + "\".");
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
		try {
			return getProvider().getUserLoggedIn();
		} catch (RuntimeException t) {
			_logger.warn("Problem calling windows provider for active user", t);
			throw t;
		}
	}

	static public boolean getScreenSaverActive()
	{
		return getProvider().getScreenSaverActive();
	}
}
