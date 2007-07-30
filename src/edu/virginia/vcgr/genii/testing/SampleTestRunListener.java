package edu.virginia.vcgr.genii.testing;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class SampleTestRunListener extends RunListener
{
	public void testRunFinished(Result result)
	{
		System.err.println("Test run Finished.");
	}

	public void testRunStarted(Description description)
	{
		System.err.println("Test run started.");
	}
}