package edu.virginia.vcgr.test.regression.client.history;

import org.junit.Assert;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.history.SequenceNumber;

public class TestSequenceNumber
{
	@Test
	public void testSimpleSequence()
	{
		SequenceNumber a = new SequenceNumber();
		SequenceNumber b = a.next();
		SequenceNumber c = b.next();
		
		Assert.assertEquals("1", a.toString());
		Assert.assertEquals("2", b.toString());
		Assert.assertEquals("3", c.toString());
		
		Assert.assertTrue("a < b", a.compareTo(b) < 0);
		Assert.assertTrue("a < c", a.compareTo(c) < 0);
		Assert.assertTrue("b > a", b.compareTo(a) > 0);
		Assert.assertTrue("b < c", b.compareTo(c) < 0);
		Assert.assertTrue("c > a", c.compareTo(a) > 0);
		Assert.assertTrue("c > b", c.compareTo(b) > 0);
	}
	
	@Test
	public void testCompoundSequences()
	{
		SequenceNumber a = new SequenceNumber();
		SequenceNumber c = a.next();
		SequenceNumber b = new SequenceNumber();
		b = b.wrapWith(a);
		
		Assert.assertEquals("1", a.toString());
		Assert.assertEquals("1.1", b.toString());
		Assert.assertEquals("2", c.toString());
		
		Assert.assertTrue("a < b", a.compareTo(b) < 0);
		Assert.assertTrue("a < c", a.compareTo(c) < 0);
		Assert.assertTrue("b > a", b.compareTo(a) > 0);
		Assert.assertTrue("b < c", b.compareTo(c) < 0);
		Assert.assertTrue("c > a", c.compareTo(a) > 0);
		Assert.assertTrue("c > b", c.compareTo(b) > 0);
	}
	
	@Test
	public void testStringParsing()
	{
		SequenceNumber a = new SequenceNumber();	// 1
		String aRep = a.toString();
		SequenceNumber b = a.next();				// 2
		b = b.wrapWith(a);							// 1.2
		String bRep = b.toString();
		SequenceNumber c = b.next();				// 1.3
		c = c.wrapWith(a);							// 1.1.3
		String cRep = c.toString();
		
		Assert.assertEquals("1", aRep);
		Assert.assertEquals("1.2", bRep);
		Assert.assertEquals("1.1.3", cRep);
		
		SequenceNumber aPrime = SequenceNumber.valueOf(aRep);
		SequenceNumber bPrime = SequenceNumber.valueOf(bRep);
		SequenceNumber cPrime = SequenceNumber.valueOf(cRep);
		
		Assert.assertEquals(a, aPrime);
		Assert.assertEquals(b, bPrime);
		Assert.assertEquals(c, cPrime);
	}
}