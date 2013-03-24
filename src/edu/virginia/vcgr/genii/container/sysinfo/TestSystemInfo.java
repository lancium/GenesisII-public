package edu.virginia.vcgr.genii.container.sysinfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;

/**
 * A simple tester that allows one to run just the SystemInfoProvider support on a targeted
 * platform.
 * 
 * @author Chris Koeritz
 * @copyright Copyright (c) 2012-$now By University of Virginia
 * @license This file is free software; you can modify and redistribute it under the terms of the
 *          Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
public class TestSystemInfo
{
	static private Log logger = LogFactory.getLog(TestSystemInfo.class);

	public TestSystemInfo()
	{
		System.loadLibrary("VcgrContainerLib");
	}

	public void runTest()
	{
		System.out.println("Testing the SystemInfoProvider for " + OperatingSystemType.getCurrent().toString());
		System.out.println("  getting cpu speed...");
		long cpuspeed = 0;
		try {
			cpuspeed = SystemInfoUtils.getIndividualCPUSpeed();
		} catch (Throwable cause) {
			logger.error("Exception occurred", cause);
		}
		System.out.println("  getting physical memory...");
		long physmem = 0;
		try {
			physmem = SystemInfoUtils.getPhysicalMemory();
		} catch (Throwable cause) {
			logger.error("Exception occurred", cause);
		}
		System.out.println("  getting physical memory available...");
		long physmemavail = 0;
		try {
			physmemavail = SystemInfoUtils.getPhysicalMemoryAvailable();
		} catch (Throwable cause) {
			logger.error("Exception occurred", cause);
		}
		System.out.println("  getting virtual memory ...");
		long virtmem = 0;
		try {
			virtmem = SystemInfoUtils.getVirtualMemory();
		} catch (Throwable cause) {
			logger.error("Exception occurred", cause);
		}
		System.out.println("  getting virtual memory available...");
		long virtmemavail = 0;
		try {
			virtmemavail = SystemInfoUtils.getVirtualMemoryAvailable();
		} catch (Throwable cause) {
			logger.error("Exception occurred", cause);
		}
		System.out.println("  getting user logged in state...");
		boolean loggedin = false;
		try {
			loggedin = SystemInfoUtils.getUserLoggedIn();
		} catch (Throwable cause) {
			logger.error("Exception occurred", cause);
		}
		System.out.println("  getting screen saver activity state...");
		boolean screensaving = false;
		try {
			screensaving = SystemInfoUtils.getScreenSaverActive();
		} catch (Throwable cause) {
			logger.error("Exception occurred", cause);
		}
		System.out.println("\nReport for system:");
		System.out.println(String.format("  cpu speed=%d  user logged in=%b  screensaver active=%b", cpuspeed, loggedin,
			screensaving));
		System.out.println(String.format("  total physical memory=%d & available=%d", physmem, physmemavail));
		System.out.println(String.format("  total virtual memory=%d & available=%d", virtmem, virtmemavail));
	}

	public static void main(String s[])
	{
		TestSystemInfo tsi = new TestSystemInfo();
		tsi.runTest();
	}
}
