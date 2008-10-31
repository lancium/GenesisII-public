package edu.virginia.vcgr.secrun.runnables.test;

import java.util.Properties;

import edu.virginia.vcgr.secrun.SecureRunnable;

public class TestSecRun implements SecureRunnable
{
	@Override
	public boolean run(Properties runProperties) throws Throwable
	{
		System.err.println("I am the TestSecRun runnable.");
		return true;
	}
}