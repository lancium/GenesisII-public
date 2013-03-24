package edu.virginia.vcgr.genii.client.cmd;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.configuration.GridEnvironment;
import edu.virginia.vcgr.genii.client.osgi.OSGiSupport;

public class TestCommandLineParser
{
	@SuppressWarnings("unused")
	static private class startupKludge extends ApplicationBase
	{
		public startupKludge()
		{
			if (!OSGiSupport.setUpFramework()) {
				System.exit(1);
			}
			GridEnvironment.loadGridEnvironment();
			prepareClientApplication();
			// this code is not sufficient. we are getting nasty error message from deep
			// in the application setup.
		}
	}

	// @SuppressWarnings("unused")
	// static private startupKludge kludgey = new startupKludge();

	@Test
	public void testCanEvenConstruct()
	{
		CommandLineFormer cmdf = new CommandLineFormer();
		System.out.println("command line former: " + cmdf.toString());
	}

	// @Test
	public void notAtestCommandLineParser() throws ToolException, FileNotFoundException, IOException
	{
		String[] cLine = CommandLineFormer.formCommandLine("a bb ccc abc   abfoo");

		Assert.assertEquals(5, cLine.length);
		Assert.assertEquals("a", cLine[0]);
		Assert.assertEquals("bb", cLine[1]);
		Assert.assertEquals("ccc", cLine[2]);
		Assert.assertEquals("abc", cLine[3]);
		Assert.assertEquals("abfoo", cLine[4]);
	}
}
