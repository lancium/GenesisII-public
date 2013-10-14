package test.org.morgan.mnaming;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.morgan.mnaming.InitialMNamingContext;
import org.morgan.mnaming.MNamingException;

public class TestInitialMNamingContext
{
	@Before
	public void setUp() throws Exception
	{
		InitialMNamingContext ctxt = new InitialMNamingContext();
		ctxt.bind("mem:one", "One");
		ctxt.bind("mem:7", new Integer(7));
	}

	@After
	public void tearDown() throws Exception
	{
		InitialMNamingContext ctxt = new InitialMNamingContext();
		ctxt.clearAll();
	}

	@Test
	public void testLookup() throws Exception
	{
		InitialMNamingContext ctxt = new InitialMNamingContext();

		Assert.assertEquals("One", ctxt.lookup("mem:one"));
		Assert.assertEquals(new Integer(7), ctxt.lookup("mem:7"));
		Assert.assertNull(ctxt.lookup("mem:foo"));
	}

	@Test
	public void testGet() throws Exception
	{
		InitialMNamingContext ctxt = new InitialMNamingContext();

		Assert.assertEquals("One", ctxt.get("mem:one"));
		Assert.assertEquals(new Integer(7), ctxt.get("mem:7"));
	}

	@Test
	public void testCastedLookup() throws Exception
	{
		InitialMNamingContext ctxt = new InitialMNamingContext();

		Assert.assertEquals("One", ctxt.lookup(String.class, "mem:one"));
		Assert.assertEquals(new Integer(7), ctxt.lookup(Integer.class, "mem:7"));
	}

	@Test
	public void testCastedGet() throws Exception
	{
		InitialMNamingContext ctxt = new InitialMNamingContext();

		Assert.assertEquals("One", ctxt.get(String.class, "mem:one"));
		Assert.assertEquals(new Integer(7), ctxt.get(Integer.class, "mem:7"));
	}

	@Test(expected = MNamingException.class)
	public void testFailedGet() throws Exception
	{
		InitialMNamingContext ctxt = new InitialMNamingContext();

		ctxt.get("mem:foo");
	}
}