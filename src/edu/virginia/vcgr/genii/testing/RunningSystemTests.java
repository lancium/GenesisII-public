package edu.virginia.vcgr.genii.testing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(org.junit.runners.Suite.class)
@SuiteClasses(
	{
		TestA.class, 
		TestB.class
	}
)
public class RunningSystemTests
{
	@BeforeClass
	static public void startSystem()
	{
		System.out.println("Starting a brand new system.");
	}
	
	@AfterClass
	static public void after()
	{
		System.out.println("Cleaning up system.");
	}
}