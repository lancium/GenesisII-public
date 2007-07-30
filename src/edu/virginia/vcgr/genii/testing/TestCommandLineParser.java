package edu.virginia.vcgr.genii.testing;

import org.junit.Assert;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.cmd.CommandLineFormer;
import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class TestCommandLineParser
{
	@Test
	public void testCommandLineParser() throws ToolException
	{
		String []cLine = CommandLineFormer.formCommandLine(
			"a bb ccc \"a b c\"   a\\ b\tfoo");
		
		Assert.assertEquals(6, cLine.length);
		Assert.assertEquals("a", cLine[0]);
		Assert.assertEquals("bb", cLine[1]);
		Assert.assertEquals("ccc", cLine[2]);
		Assert.assertEquals("a b c", cLine[3]);
		Assert.assertEquals("a b", cLine[4]);
		Assert.assertEquals("foo", cLine[5]);
	}
}
