package edu.virginia.vcgr.genii.container.exportdir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

public class TestGffsExportConfiguration
{
	static private Log _logger = LogFactory.getLog(TestGffsExportConfiguration.class);

	public TestGffsExportConfiguration()
	{
	}

	@Test
	public void testParseBasicGridMapLine()
	{
		// tests a basic line from the grid-mapfile.
		String example = "\"/C=US/O=NPACI/OU=SDSC/CN=Nancy Wilkins-Diehr/UID=wilkinsn\" wilkinsn";
		GridMapUserList usersSeen = new GridMapUserList();
		String dnfound = GffsExportConfiguration.parseGridMapLine(example, usersSeen);
		GffsExportConfiguration.dumpInfo(_logger, "first test", dnfound, usersSeen);
		Assert.assertEquals(1, usersSeen.size());
		Assert.assertEquals("wilkinsn", usersSeen.get(0));
		Assert.assertEquals("/C=US/O=NPACI/OU=SDSC/CN=Nancy Wilkins-Diehr/UID=wilkinsn", dnfound);
	}

	@Test
	public void testParseMultiUserListing()
	{
		// tests multiple users listed (one to many, which we don't need but want to not barf
		// on).
		String example = "\"/C=US/O=NPACI/OU=SDSC/CN=Feng Wang/UID=ux454763\" ux454763,jortnips";
		GridMapUserList usersSeen = new GridMapUserList();
		String dnfound = GffsExportConfiguration.parseGridMapLine(example, usersSeen);
		GffsExportConfiguration.dumpInfo(_logger, "second test (has two users)", dnfound, usersSeen);
		Assert.assertEquals(2, usersSeen.size());
		Assert.assertEquals("ux454763", usersSeen.get(0));
		Assert.assertEquals("jortnips", usersSeen.get(1));
		Assert.assertEquals("/C=US/O=NPACI/OU=SDSC/CN=Feng Wang/UID=ux454763", dnfound);
	}

	@Test
	public void testParseExtraSpaces()
	{
		// tests pernicious extra spaces.
		String example = "\"/C=US/O=NPACI/OU=SDSC/CN=Christopher T. Jordan/UID=ctjordan\"    ctjordan";
		GridMapUserList usersSeen = new GridMapUserList();
		String dnfound = GffsExportConfiguration.parseGridMapLine(example, usersSeen);
		GffsExportConfiguration.dumpInfo(_logger, "third test", dnfound, usersSeen);
		Assert.assertEquals(1, usersSeen.size());
		Assert.assertEquals("ctjordan", usersSeen.get(0));
		Assert.assertEquals("/C=US/O=NPACI/OU=SDSC/CN=Christopher T. Jordan/UID=ctjordan", dnfound);
	}

}
