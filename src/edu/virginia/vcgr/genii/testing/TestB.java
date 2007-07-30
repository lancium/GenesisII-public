package edu.virginia.vcgr.genii.testing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestB
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.err.println("TestB Before Class...");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		System.err.println("TestB After Class...");
	}
	

	@Test
	public void test()
	{
	}
}