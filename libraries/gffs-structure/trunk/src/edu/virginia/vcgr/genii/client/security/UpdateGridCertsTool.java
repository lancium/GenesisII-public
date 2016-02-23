package edu.virginia.vcgr.genii.client.security;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.compression.UnpackTar;
import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.client.cmd.tools.CopyTool;
import edu.virginia.vcgr.genii.client.cmd.tools.RmTool;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.rns.PathOutcome;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class UpdateGridCertsTool
{
	static private Log _logger = LogFactory.getLog(UpdateGridCertsTool.class);

	// how frequently the updater process should attempt to run.
	public static final int UPDATER_SNOOZE_DURATION = 1000 * 60 * 60; // one hour.

	/**
	 * given the name of a certificate package, this will download it and install it into the state directory in grid-certificates. this
	 * manages the file locking necessary to keep the certs dir in synch with other users.
	 */
	static private int getCertPackAndInstall(RNSPath certPackFile, PrintWriter stderr) throws IOException
	{
		File downloadLocation = File.createTempFile("tempCertFile", "tgz");

		_logger.debug("copying " + certPackFile + " to path " + downloadLocation.getAbsolutePath());

		// download the current certificate file from grid.
		PathOutcome outc =
			CopyTool.copy("grid:" + certPackFile.toString(), "local:" + downloadLocation.getAbsolutePath(), false, false, null, null);
		if (outc.differs(PathOutcome.OUTCOME_SUCCESS)) {
			_logger.error("failure during copy of grid certificate package to local directory: " + outc.toString());
			return 1;
		}

		_logger.info("copied certificate package out of the grid: " + certPackFile);

		// lock here to prevent simultaneous access.
		ThreadAndProcessSynchronizer.acquireLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);

		try {
			// quick check to make sure we didn't get scooped by someone else.
			Properties props = CertUpdateHelpers.getCertUpdateProperties();
			if (!timeForUpdate(props)) {
				return 0;
			}

			// make a new temp file, but turn it into a directory under the state dir.
			File newCertsDir = File.createTempFile("newCertsDir", null, new File(InstallationProperties.getUserDir()));
			newCertsDir.delete();

			// unpack current file using new untar support.
			UnpackTar.uncompressTarGZ(downloadLocation, newCertsDir, false);

			// clean up any old certs dir.
			File oldCertsDir = new File(InstallationProperties.getUserDir() + "/grid-certificates.old");
			if (oldCertsDir.exists()) {
				outc = RmTool.rm(oldCertsDir, true, true, stderr);
				if (outc.differs(PathOutcome.OUTCOME_SUCCESS)) {
					_logger.error("failed to remove the existing old certificates directory");
					return 1;
				}
			}

			// move current certs dir out of way into old certs dir.
			File currentCertsDir = new File(InstallationProperties.getUserDir() + "/grid-certificates");
			if (currentCertsDir.exists()) {
				boolean movedOkay = currentCertsDir.renameTo(oldCertsDir);
				if (!movedOkay) {
					_logger.error("failed to move the existing certificates directory out of the way");
					return 1;
				}
			}

			_logger.info("moved old certificates out of the way");

			/*
			 * move new certs dir into place. we need to go a level deeper due to the tar file starting at a directory called certificates.
			 */
			File subCerts = new File(newCertsDir.getAbsolutePath(), "/certificates");
			boolean movedOkay = subCerts.renameTo(currentCertsDir);
			if (!movedOkay) {
				_logger.error("failed to move the new certificates directory into place");
				// exiting, unlock file lock on cert update properties.
				return 1;
			}
			// now remove the empty directory that used to have the new certs.
			newCertsDir.delete();

			_logger.info("updated grid-certificates in local directory");

			// update name for last file used.
			props.setProperty(CertUpdateHelpers.LAST_UPDATE_PACKAGE_PROPNAME, certPackFile.getName());

			justWriteProps(props, true);
			// // reschedule updates for next interval.
			// Long interval =
			// Long.parseLong(props.getProperty(CertUpdateHelpers.UPDATE_INTERVAL_PROPNAME));
			// if (interval <= 60 * 1000) {
			// // reset the interval since checking every minute or less is insane. we could be
			// // more stringent here, but want to allow testing at small intervals.
			// interval = CertUpdateHelpers.DEFAULT_CERT_UPDATE_INTERVAL;
			// }
			// long timeNow = new Date().getTime();
			// props.setProperty(CertUpdateHelpers.NEXT_UPDATE_PROPNAME, "" + (interval + timeNow));
			// CertUpdateHelpers.putCertUpdateProperties(props);

		} catch (Throwable t) {
			_logger.error("cert update process croaked with exception", t);
			throw t;
		} finally {
			// exiting, unlock file lock on cert update properties.
			ThreadAndProcessSynchronizer.releaseLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
		}

		return 0;
	}

	/**
	 * locks the consistency lock before stuffing the properties provided into our external config file. this sets the next update time to
	 * reflect a successful certificate update of some sort, which will postpone checking certificates again until the checking interval
	 * elapses. the caller can have the consistency lock already, but then must specify this in the "alreadyLocked" parameter.
	 */
	static public void justWriteProps(Properties props, boolean alreadyLocked)
	{
		if (!alreadyLocked) {
			ThreadAndProcessSynchronizer.acquireLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
		}

		try {
			Long interval = Long.parseLong(props.getProperty(CertUpdateHelpers.UPDATE_INTERVAL_PROPNAME));
			if (interval <= 60 * 1000) {
				// reset the interval since checking every minute or less is insane. we could be
				// more stringent here, but want to allow testing at small intervals.
				interval = CertUpdateHelpers.DEFAULT_CERT_UPDATE_INTERVAL;
			}
			long timeNow = new Date().getTime();
			props.setProperty(CertUpdateHelpers.NEXT_UPDATE_PROPNAME, "" + (interval + timeNow));
			CertUpdateHelpers.putCertUpdateProperties(props);
		} finally {
			if (!alreadyLocked) {
				// exiting, unlock file lock on cert update properties.
				ThreadAndProcessSynchronizer.releaseLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
			}
		}
	}

	/**
	 * return true if the properties say it's time for a certificate update.
	 */
	static private boolean timeForUpdate(Properties props)
	{
		/*
		 * if we got a bad properties file, something is hosed up. we will say it's not time to block anyone else from using the file.
		 */
		if (props == null)
			return false;

		// validate that it is time for the update to occur.
		long currTime = new Date().getTime();
		Long nextUpdate = Long.parseLong(props.getProperty(CertUpdateHelpers.NEXT_UPDATE_PROPNAME));
		if ((nextUpdate != null) && (currTime < nextUpdate)) {
			_logger.info("not time for next certificate update yet; skipping it.");
			return false;
		}
		return true;
	}

	/**
	 * determines the name of the new certificates package. returns null if there is no need to update yet, either because the file is missing
	 * or because it's the same as last update.
	 */
	static private RNSPath getNewCertPackageName(Properties props)
	{
		// check current contents of directory.
		Collection<GeniiPath.PathMixIn> paths = GeniiPath.pathExpander(CertUpdateHelpers.RNS_CERTS_FOLDER + "/*");
		if (paths.isEmpty()) {
			_logger.info("no certificates package found in grid.  skipping update.");
			return null;
		}
		if (paths.size() > 1) {
			_logger.info("warning--more than one certificate package found in grid; will use first seen.");
		}
		RNSPath certPack = null;
		for (GeniiPath.PathMixIn path : paths) {
			RNSPath cp = path._rns;
			if (cp == null) {
				_logger.error("unexpected null path in cert pack folder list");
			} else if (cp.getName().equals("*")) {
				_logger.info("no certificates package found in grid.  skipping update.");
				return null;
			} else {
				certPack = cp;
			}
			if (certPack != null)
				break;
		}
		if (certPack == null) {
			_logger.error("somehow no certificate package paths were valid.");
			return null;
		}

		// find out what file we last downloaded.
		String lastCertFile = props.getProperty(CertUpdateHelpers.LAST_UPDATE_PACKAGE_PROPNAME);

		// is file name different (or old one didn't exist)? if not different, skip update.
		if (!certPack.getName().equals(lastCertFile)) {
			// we need to update now.
			_logger.info("certificate update pack is different; last was " + lastCertFile + " and new is " + certPack.getName());
			return certPack;
		} else {
			// it's the same file, so do not update.
			_logger.info("certificate update pack is same as last time (" + lastCertFile + "); will not update.");
			return null;
		}
	}

	/**
	 * updates the grid-certificates directory in the state directory based on the latest certificates available in the grid.
	 */
	static public int runGridCertificateUpdates()
	{
		_logger.info("starting check for updated grid certificates");
		Properties props = null;
		// lock the consistency file so we get a good copy of the properties.
		ThreadAndProcessSynchronizer.acquireLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
		try {
			props = CertUpdateHelpers.getCertUpdateProperties();
		} finally {
			ThreadAndProcessSynchronizer.releaseLock(CertUpdateHelpers.CONSISTENCY_LOCK_FILE);
		}

		// is it time to even check for an updated package of certificates?
		if (!timeForUpdate(props)) {
			return 0; // nope.
		}

		// debugging dump of all properties.
		if (_logger.isTraceEnabled()) {
			StringWriter writer = new StringWriter();
			props.list(new PrintWriter(writer));
			_logger.debug("loaded cert update props, got: " + writer.getBuffer().toString());
		}

		// find out if there's a certificate package present and its name.
		RNSPath certPack = getNewCertPackageName(props);
		if (certPack == null) {
			// we don't want to redo the file check right away if there's nothing there.
			// we can wait until the next update time.
			justWriteProps(props, false);
			return 0;
		}

		try {
			// if we decide to call this, then we expect to actually update the trust store.
			// hmmm: can we find a stderr to pass here instead of null?
			if (getCertPackAndInstall(certPack, null) != 0) {
				return 1;
			}
		} catch (Exception e) {
			_logger.error("failure during update of certificate pack: " + e.getLocalizedMessage(), e);
			return 1;
		}

		_logger.debug("dropping trust store due to a grid-certificates update");
		// make sure we reload with the latest, but only if we actually updated.
		KeystoreManager.dropTrustStores();

		return 0;
	}

}
