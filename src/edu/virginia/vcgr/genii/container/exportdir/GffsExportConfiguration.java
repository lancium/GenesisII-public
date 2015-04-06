package edu.virginia.vcgr.genii.container.exportdir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileChangeTracker;
import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.ExportProperties;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

public class GffsExportConfiguration
{
	static private Log _logger = LogFactory.getLog(GffsExportConfiguration.class);

	// how long gridmap entries can be kept in the cache.
	public static final long GRIDMAP_CACHE_LIFETIME = 1000 * 60 * 30;

	// how many cached gridmap elements we keep around.
	public static final int MAX_GRIDMAP_CACHE_ELEMENTS = 200;

	// the cache for mapping between DN and grid map users.
	private static TimedOutLRUCache<String, GridMapUserList> _dnCache = new TimedOutLRUCache<String, GridMapUserList>(
		MAX_GRIDMAP_CACHE_ELEMENTS, GRIDMAP_CACHE_LIFETIME);

	// tracks last timestamp we saw on the gridmap file.
	private static FileChangeTracker _GridmapChangeTracker = null;

	/**
	 * consumes a line of text from a grid-mapfile and breaks it down into the DN specified and the list of users associated with the DN.
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

	/**
	 * returns true if the time stamp for the grid-mapfile has changed since the last time we checked.
	 */
	private static synchronized boolean gridmapFileChanged()
	{
		if (_GridmapChangeTracker == null) {
			_GridmapChangeTracker = new FileChangeTracker(new File(ExportProperties.getExportProperties().getGridMapFile()));
		}
		return _GridmapChangeTracker.hasFileChanged();
	}

	private static void cacheDnMapping(String dn, GridMapUserList users)
	{
		_dnCache.put(dn, users);
	}

	private static GridMapUserList lookupDnInCache(String dn)
	{
		if (gridmapFileChanged()) {
			// take care of flushing the cache out, since the file changed.
			if (_logger.isDebugEnabled())
				_logger.debug("flushing DN cache for exports due to change in grid-mapfile");
			_dnCache.clear();
			return null;
		}
		return _dnCache.get(dn);
	}

	/**
	 * reads the grid-mapfile to locate a particular DN.
	 * 
	 * note that the DN currently has to match exactly what is in the grid-mapfile.
	 */
	public static GridMapUserList mapDistinguishedName(String dn)
	{
		// lookup in cache before reading the grid-mapfile.
		GridMapUserList cached = lookupDnInCache(dn);
		if (cached != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("found user DN in cache: " + cached);
			return cached;
		}

		// nothing was cached, so we have to do a lookup.
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
				if (foundDN.equals(dn)) {
					if (_logger.isDebugEnabled())
						_logger.debug("found the sought DN '" + dn + "' in grid-mapfile as this userlist: " + usersFound);
					cacheDnMapping(dn, usersFound);
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

	/**
	 * a helpful method for the server side (container) that finds the preferred identity in the user's credentials, if possible. if not
	 * possible, then this just falls back to the first USER type credential available, if any. and if none of those are available, this will
	 * return null. this returns a full DN for the preferred identity or null.
	 */
	public static X509Certificate findPreferredIdentityServerSide(ICallingContext context, String ownerDN)
	{
		ArrayList<NuCredential> credSet = new ArrayList<NuCredential>();
		credSet.addAll(TransientCredentials.getTransientCredentials(context).getCredentials());

		X509Certificate clientCert = (X509Certificate) context.getSingleValueProperty(GenesisIIConstants.LAST_TLS_CERT_FROM_CLIENT);
		if (clientCert != null) {
			credSet.add(new X509Identity(new X509Certificate[] { clientCert }, IdentityType.CONNECTION));
		} else {
			_logger.error("failed to determine the calling client's TLS certificate");
		}

		if (_logger.isDebugEnabled()) {
			_logger.debug("got a credential set to search of:\n" + TrustCredential.showCredentialList(credSet, VerbosityLevel.HIGH));
			_logger.debug("searching for owner as: " + ownerDN);
		}

		X509Certificate owner = PreferredIdentity.findIdentityPatternInCredentials(ownerDN, credSet);
		if (owner == null) {
			/*
			 * there was no match for the owner, so let's just try handing out the first USER credential we can find. we expect that the
			 * caller has *some* credential, otherwise most operations will fail due to lack of permissions.
			 */
			if (_logger.isDebugEnabled())
				_logger.debug("could not resolve preferred identity, using first USER credential instead");
			CredentialWallet tempWallet = new CredentialWallet(credSet);
			owner = tempWallet.getFirstUserCredential().getOriginalAsserter()[0];
		}

		if (_logger.isDebugEnabled())
			_logger.debug("ownerDN resolved to: '" + owner.getSubjectDN() + "'");

		return owner;
	}

	/**
	 * logs the information from a line of a grid mapfile.
	 */
	public static void dumpInfo(Log logger, String info, String dn, GridMapUserList users)
	{
		logger.info(info + ": DN= " + dn + ", users list: ");
		for (int i = 0; i < users.size(); i++) {
			logger.info("#" + i + ": " + users.get(i));
		}
	}
}
