package edu.virginia.vcgr.appmgr.patch;

import java.util.Properties;

public interface PatchRunnable
{
	public void run(Properties runProperties) throws Throwable;
}