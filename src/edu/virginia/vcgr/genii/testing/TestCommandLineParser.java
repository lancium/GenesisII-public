package edu.virginia.vcgr.genii.testing;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.cmd.CommandLineFormer;
import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class TestCommandLineParser
{
	@Test
	public void testCommandLineParser() throws ToolException, FileNotFoundException, IOException
	{
		String []cLine = CommandLineFormer.formCommandLine(
			"a bb ccc abc   abfoo");
		
		Assert.assertEquals(5, cLine.length);
		Assert.assertEquals("a", cLine[0]);
		Assert.assertEquals("bb", cLine[1]);
		Assert.assertEquals("ccc", cLine[2]);
		Assert.assertEquals("abc", cLine[3]);
		Assert.assertEquals("abfoo", cLine[4]);
	}
}
