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

import edu.virginia.vcgr.genii.system.jni.JNIContainerBaseClass;

public class WindowsProvider extends JNIContainerBaseClass implements ISystemInfoProvider
{
	public native long getIndividualCPUSpeed();

	public native long getPhysicalMemory();

	public native long getPhysicalMemoryAvailable();

	public native long getVirtualMemory();

	public native long getVirtualMemoryAvailable();

	public native boolean getUserLoggedIn();

	public native boolean getScreenSaverActive();
}
