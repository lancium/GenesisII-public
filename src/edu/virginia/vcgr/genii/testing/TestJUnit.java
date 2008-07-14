package edu.virginia.vcgr.genii.testing;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.junit.Assert;

public class TestJUnit
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
		System.err.println("SetUpBeforeClass called.");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		System.err.println("TearDownAfterClass called.");
	}

	@Before
	public void setUp() throws Exception
	{
		System.err.println("SetUp called.");
	}

	@After
	public void tearDown() throws Exception
	{
		System.err.println("TearDown called.");
	}

	@Test
	public void testMath()
	{
		Assert.assertEquals(7, 2 + 5);
	}
	
	@SuppressWarnings("null")
	@Test(expected=NullPointerException.class)
	public void testException()
	{
		String foo = null;
		
		foo.toString();
	}
}
