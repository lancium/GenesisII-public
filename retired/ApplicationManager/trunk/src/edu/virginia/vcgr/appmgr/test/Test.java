package edu.virginia.vcgr.appmgr.test;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.ProcessorArchitecture;

public class Test
{
	static public void main(String[] args) throws Throwable
	{
		System.out.format("[%s] %s v %s\n", ProcessorArchitecture.getCurrent(), OperatingSystemType.getCurrent(),
			OperatingSystemType.getCurrentVersion());
	}
}
