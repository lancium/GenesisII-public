package edu.virginia.vcgr.genii.container.exportdir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.ExportProperties;

public class GffsExportConfiguration
{
	static private Log _logger = LogFactory.getLog(GffsExportConfiguration.class);

	/**
	 * consumes a line of text from a grid-mapfile and breaks it down into the DN specified and the
	 * list of users associated with the DN.
	 */
	public static String parseGridMapLine(String line, GridMapUserList addTo)
	{
		// wipe out prior contents in the list.
		addTo.clear();

		if ((line == null) || (line.length() < 2)) {
			// this line's not big enough to work on.
			return null;
		}

		String complaint = "line format in grid-mapfile is erroneous; ";
		// seek the quoted bit first.
		if (!line.substring(0, 1).equals("\"")) {
			_logger.error(complaint + "does not start with quote.  ignoring:" + line);
			return null;
		}

		int posnSecondQuote = line.lastIndexOf('"');
		if (posnSecondQuote < 2) {
			_logger.error(complaint + "could not find valid closing quote.  ignoring:" + line);
			return null;
		}

		String DN = line.substring(1, posnSecondQuote);
		// _logger.debug("found a DN in grid-mapfile line of: " + DN);

		// verify that there's a space between the quote and users.
		posnSecondQuote++; // skip the quote we found.
		if (!line.substring(posnSecondQuote, posnSecondQuote + 1).equals(" ")) {
			_logger.error(complaint + "separator was missing between DN and user list.  ignoring:" + line);
			return null;
		}
		// skip all spaces.
		while (line.substring(posnSecondQuote, posnSecondQuote + 1).equals(" ")) {
			posnSecondQuote++;
		}
		// then take the rest of the line (after skipping the second quote and a space) and separate
		// by commas.
		String remainder = line.substring(posnSecondQuote);
		if (remainder.length() == 0) {
			_logger.error(complaint + "found no users listed for DN.  ignoring: " + line);
			return null;
		}

		// _logger.debug("found user list in grid-mapfile line as: " + remainder);
		GridMapUserList users = new GridMapUserList(remainder.split(","));
		if (_logger.isDebugEnabled()) {
			String printout = new String();
			for (String user : users) {
				printout = printout.concat("\n" + user);
			}
			// _logger.debug("broken out users list is:" + printout);
		}

		addTo.addAll(users);

		// _logger.debug("at end of parse, users list has " + addTo.size() + " elements: " + addTo);
		return DN;
	}

	/*
	 * hmmm: add a cache! this really should not read the file each and every time. so cache the
	 * values without time-out, and we won't have to read the file every time. however, if the
	 * grid map file changes, we should throw out all cached entries.
	 */


	
	/**
	 * reads the grid-mapfile to locate a particular DN.
	 * 
	 * note that the DN currently has to match exactly.
	 */
	public static GridMapUserList mapDistinguishedName(String DN)
	{
		File mapFile = ExportProperties.getExportProperties().openGridMapFile();
		if (mapFile == null) {
			_logger.error("could not open the grid-mapfile; is it configured properly in export.properties?");
			return null;
		}

		GridMapUserList usersFound = new GridMapUserList();
		try {
			BufferedReader br = new BufferedReader(new FileReader(mapFile));
			String line;
			while ((line = br.readLine()) != null) {
				// disassemble the line and add it to our map.
				String foundDN = parseGridMapLine(line, usersFound);
				if (foundDN == null) {
					// ignore parsing errors, which we complain about inside the parse function.
					continue;
				}
				if (foundDN.equals(DN)) {
					if (_logger.isDebugEnabled())
						_logger.debug("found the sought DN '" + DN + "' as this userlist: " + usersFound);
					break;
				}
			}
			br.close();
		} catch (Throwable t) {
			_logger.error("failed to read the grid-mapfile", t);
			return null;
		}
		return usersFound;
	}

	private static void dumpInfo(String info, String dn, GridMapUserList users)
	{
		_logger.info(info + ": DN= " + dn + ", users list: ");
		for (int i = 0; i < users.size(); i++) {
			_logger.info("#" + i + ": " + users.get(i));
		}

	}

	static public void main(String[] args) throws Throwable
	{
		{
			// tests a basic line from the grid-mapfile.
			String example = "\"/C=US/O=NPACI/OU=SDSC/CN=Nancy Wilkins-Diehr/UID=wilkinsn\" wilkinsn";
			GridMapUserList usersSeen = new GridMapUserList();
			String dnfound = parseGridMapLine(example, usersSeen);
			dumpInfo("first test", dnfound, usersSeen);
		}
		{
			// tests multiple users listed (one to many, which we don't need but want to not barf
			// on).
			String example = "\"/C=US/O=NPACI/OU=SDSC/CN=Feng Wang/UID=ux454763\" ux454763,jortnips";
			GridMapUserList usersSeen = new GridMapUserList();
			String dnfound = parseGridMapLine(example, usersSeen);
			dumpInfo("second test (has two users)", dnfound, usersSeen);
		}
		{
			// tests pernicious extra spaces.
			String example = "\"/C=US/O=NPACI/OU=SDSC/CN=Christopher T. Jordan/UID=ctjordan\"    ctjordan";
			GridMapUserList usersSeen = new GridMapUserList();
			String dnfound = parseGridMapLine(example, usersSeen);
			dumpInfo("third test", dnfound, usersSeen);
		}
	}
}
