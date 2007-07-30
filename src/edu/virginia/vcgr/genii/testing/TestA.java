package edu.virginia.vcgr.genii.testing;


import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestA
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.err.println("TestA Before Class...");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		System.err.println("TestA After Class...");
	}
	
	@Test
	public void test()
	{
	}
}