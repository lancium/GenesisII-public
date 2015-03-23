package edu.virginia.vcgr.genii.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.ExportControl;
import edu.virginia.vcgr.genii.client.ExportControl.ModeAllowance;

public class TestExportRestriction
{
	static private Log _logger = LogFactory.getLog(TestExportRestriction.class);

	public TestExportRestriction()
	{
	}

	@Test
	public void testConsolidatingAndComments()
	{
		String exampleLines =
			"\n\n# this is a comment\n   #this is also a comment\nreal line part 1\\\n continues here\\\n and stops after this.\n\n\n# yo another comment\n";

		InputStream in = null;
		try {
			in = IOUtils.toInputStream(exampleLines, "UTF-8");
		} catch (IOException e) {
			_logger.debug("caught exception while setting up input stream from string");
			Assert.assertFalse(true);
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		String result = ExportControl.readNextLine(reader);
		Assert.assertEquals("real line part 1 continues here and stops after this.", result);
	}

	@Test
	public void testPathAppropriatenessSimpleCompares()
	{
		ExportControl test1 = new ExportControl("/acklabeth/snorpfest", "joe", null);

		int approp = test1.isPathAppropriate("/acklabeth/buptail");
		Assert.assertEquals(approp, 0);

		approp = test1.isPathAppropriate("/acklabeth/snorpfest");
		Assert.assertEquals(approp, 3);
	}

	@Test
	public void testPathAppropriatenessOnRoot()
	{
		ExportControl test2 = new ExportControl("/", "joe", null);
		int approp = test2.isPathAppropriate("/acklabeth/buptail");
		Assert.assertEquals(approp, 1);

		approp = test2.isPathAppropriate("carbo");
		Assert.assertEquals(approp, 0);

		approp = test2.isPathAppropriate("/");
		Assert.assertEquals(approp, 1);
	}

	@Test
	public void testPathAppropriatenessWindowsPaths()
	{
		// windows cases here.
		ExportControl test3 = new ExportControl("C:/Program Files/GenesisII", "joe", null);
		int approp = test3.isPathAppropriate("C:\\Program Files");
		Assert.assertEquals(approp, 0);

		approp = test3.isPathAppropriate("C:\\");
		Assert.assertEquals(approp, 0);

		approp = test3.isPathAppropriate("C:\\Program Files/GenesisII\\lib");
		Assert.assertEquals(approp, 3);

		approp = test3.isPathAppropriate("c:\\Program Files/GenesisII\\lib");
		Assert.assertEquals(approp, 3);

		approp = test3.isPathAppropriate("A:\\Program Files/GenesisII\\lib");
		Assert.assertEquals(approp, 0);
	}

	@Test
	public void testParsingLineWithoutMode()
	{
		String line = " \t   /acklabeth/snorpfest \t\t joe \t\t \t   \t";
		ExportControl e = ExportControl.parseLine(line);
		Assert.assertNotNull(e);
		Assert.assertEquals(true, e.isUserAppropriate("joe"));
		Assert.assertEquals(false, e.isUserAppropriate("joey"));
		Assert.assertEquals(false, e.isUserAppropriate("*"));
		Assert.assertEquals(0, e.isPathAppropriate("/cramma"));
		Assert.assertEquals(3, e.isPathAppropriate("/acklabeth/snorpfest/caldera"));
		Assert.assertEquals(false, e.actionAllowed(ModeAllowance.READ_MODE));
		Assert.assertEquals(false, e.actionAllowed(ModeAllowance.WRITE_MODE));
		Assert.assertEquals(true, e.actionAllowed(ModeAllowance.DISALLOW_MODE));
	}

	@Test
	public void testParsingPermissiveLine()
	{
		String line = " \t   /acklabeth/snorpfest \t\t joe \t\t \t rw \t";
		ExportControl e = ExportControl.parseLine(line);
		Assert.assertNotNull(e);
		Assert.assertEquals(true, e.actionAllowed(ModeAllowance.READ_MODE));
		Assert.assertEquals(true, e.actionAllowed(ModeAllowance.WRITE_MODE));
		Assert.assertEquals(false, e.actionAllowed(ModeAllowance.DISALLOW_MODE));
	}

}
