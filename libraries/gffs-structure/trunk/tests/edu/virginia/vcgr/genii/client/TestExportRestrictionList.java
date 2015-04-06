package edu.virginia.vcgr.genii.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.ExportControl.ModeAllowance;
import edu.virginia.vcgr.genii.client.ExportControlsList.TestingFlag;

public class TestExportRestrictionList
{
	static private Log _logger = LogFactory.getLog(TestExportRestriction.class);

	public TestExportRestrictionList()
	{
	}

	@Test
	public void testParsingEntireFilePermissive()
	{
		// @formatter:off
		String exampleLines =
			"\n\n# this is a comment\n   #this is also a comment\n  \t \t   \n# yo another comment\n" + "\t/ \t\t \t * rw\n"
				+ " /etc \t \t  \t* \t\n" + " \t /etc  joe  rw  \n" + "  /etc/samba\t\ttony\t\\\n\t\trw\n"
				+ "/etc/samba\t  joe   \t\t\n\n\n" + "   /etc/samba/fezziwig\t\t\tjoe\t  \t\t  rw\n" + " \t\t\t/home * r\n"
				+ " /home/joe  * \t\t\n" + " /home/joe  joe rw \t\t\n";
		// @formatter:on

		InputStream in = null;
		try {
			in = IOUtils.toInputStream(exampleLines, "UTF-8");
		} catch (IOException e) {
			_logger.debug("caught exception while setting up input stream from string");
			Assert.assertFalse(true);
		}

		ExportControlsList erl = new ExportControlsList(new TestingFlag());

		boolean readOkay = erl.readRestrictionsFromStream(in);
		Assert.assertEquals(true, readOkay);
		Assert.assertEquals(9, erl.getAllRestrictions().size());

		Assert.assertEquals(false, erl.checkCreationOkay("/etc/fstab", "tony", ModeAllowance.getReadOnlyMode()));

		Assert.assertEquals(true, erl.checkCreationOkay("/etc/fstab", "joe", ModeAllowance.getReadOnlyMode()));

		Assert.assertEquals(true, erl.checkCreationOkay("/etco/crampus", "tony", ModeAllowance.getReadWriteMode()));

		Assert.assertEquals(true, erl.checkCreationOkay("/etc/samba/smb.conf", "tony", ModeAllowance.getReadWriteMode()));
		Assert.assertEquals(true, erl.checkCreationOkay("/etc/samba/smb.conf", "tony", ModeAllowance.getReadOnlyMode()));

		Assert.assertEquals(false, erl.checkCreationOkay("/etc/samba/smb.conf", "joe", ModeAllowance.getReadOnlyMode()));
		Assert.assertEquals(false, erl.checkCreationOkay("/etc/samba", "joe", ModeAllowance.getReadWriteMode()));

		Assert.assertEquals(true, erl.checkCreationOkay("/etc/samba/fezziwig/smb.conf", "joe", ModeAllowance.getReadOnlyMode()));
		Assert.assertEquals(true, erl.checkCreationOkay("/etc/samba/fezziwig", "joe", ModeAllowance.getReadWriteMode()));
		Assert.assertEquals(true, erl.checkCreationOkay("/etc/samba/fezziwig/smb.conf", "tony", ModeAllowance.getReadWriteMode()));

		Assert.assertEquals(false, erl.checkCreationOkay("/home/doughnut", "tony", ModeAllowance.getReadWriteMode()));

		Assert.assertEquals(true, erl.checkCreationOkay("/home/joe", "joe", ModeAllowance.getReadWriteMode()));
		Assert.assertEquals(false, erl.checkCreationOkay("/home/joe", "tony", ModeAllowance.getReadWriteMode()));
		Assert.assertEquals(false, erl.checkCreationOkay("/home/joe", "tony", ModeAllowance.getReadOnlyMode()));

		Assert.assertEquals(true, erl.checkCreationOkay("/home/tony", "tony", ModeAllowance.getReadOnlyMode()));
		Assert.assertEquals(true, erl.checkCreationOkay("/home/tony", "joe", ModeAllowance.getReadOnlyMode()));
	}

	@Test
	public void testParsingEntireFileRestrictive()
	{
		// @formatter:off
		String exampleLines =
			"\n\n# this file is more restrictive.\n" + "   /  \t\t  *    \n" + "  \t   /home/joe  joe  rw   \n"
				+ "    /home/tony \t\t\t  tony  n  \t\t\n " + "    /home/tony/realtony \t\t\t  tony  rw  \t\t\n ";
		// @formatter:on

		InputStream in = null;
		try {
			in = IOUtils.toInputStream(exampleLines, "UTF-8");
		} catch (IOException e) {
			_logger.debug("caught exception while setting up input stream from string");
			Assert.assertFalse(true);
		}

		ExportControlsList erl = new ExportControlsList(new TestingFlag());

		boolean readOkay = erl.readRestrictionsFromStream(in);
		Assert.assertEquals(true, readOkay);
		Assert.assertEquals(4, erl.getAllRestrictions().size());

		Assert.assertEquals(false, erl.checkCreationOkay("/etc/fstab", "tony", ModeAllowance.getReadOnlyMode()));
		Assert.assertEquals(false, erl.checkCreationOkay("/", "joe", ModeAllowance.getReadOnlyMode()));
		Assert.assertEquals(false, erl.checkCreationOkay("/home/tony", "joe", ModeAllowance.getReadOnlyMode()));
		Assert.assertEquals(false, erl.checkCreationOkay("/home/tony", "tony", ModeAllowance.getReadOnlyMode()));

		Assert.assertEquals(true, erl.checkCreationOkay("/home/tony/realtony", "tony", ModeAllowance.getReadOnlyMode()));
		Assert.assertEquals(true, erl.checkCreationOkay("/home/tony/realtony", "tony", ModeAllowance.getReadWriteMode()));
		Assert.assertEquals(false, erl.checkCreationOkay("/home/tony/realtony", "joe", ModeAllowance.getReadOnlyMode()));

	}
}
