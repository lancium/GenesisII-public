package edu.virginia.vcgr.secrun;

import java.util.Properties;

public interface SecureRunnable
{
	public void run(String runHook, Properties runProperties) throws Throwable;
}