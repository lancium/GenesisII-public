package edu.virginia.vcgr.genii.client.security;

import java.io.File;
import java.io.FileNotFoundException;
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

	// set to true if the update process actually updated the certificates during the last run.
	static private boolean _updatedCertsRecently = false;

	// how frequently the updater process should attempt to run.
	public static final int UPDATER_SNOOZE_DURATION = 1000 * 60 * 60; // one hour.

//	// tracks the single thread running certificate updates, if there is one.
//	static Thread _updaterThread = null;
//	// sentinel for stopping the thread.
//	static volatile boolean _stopTheThread = false;

	/**
	 * given the name of a certificate package, this will download it and install it into the state
	 * directory in grid-certificates.
	 */
	static private int getCertPackAndInstall(RNSPath certPackFile) throws IOException
	{
		File downloadLocation = File.createTempFile("tempCertFile", "tgz");

		_logger.debug("copying " + certPackFile + " to path " + downloadLocation.getAbsolutePath());

		// download the current certificate file from grid.
		PathOutcome outc =
			CopyTool.copy("grid:" + certPackFile.toString(), "local:" + downloadLocation.getAbsolutePath(), false, false, null,
				null);
		if (outc.differs(PathOutcome.OUTCOME_SUCCESS)) {
			_logger.error("failure during copy of grid certificate package to local directory: " + outc.toString());
			return 1;
		}

		_logger.info("copied certificate package out of the grid: " + certPackFile);

		// make a new temp file, but turn it into a directory under the state dir.
		File newCertsDir = File.createTempFile("newCertsDir", null, new File(InstallationProperties.getUserDir()));
		newCertsDir.delete();

		// unpack current file using new untar support.
		UnpackTar.uncompressTarGZ(downloadLocation, newCertsDir);

		// clean up any old certs dir.
		File oldCertsDir = new File(InstallationProperties.getUserDir() + "/grid-certificates.old");
		if (oldCertsDir.exists()) {
			RmTool cleaner = new RmTool();
			outc = cleaner.rm(oldCertsDir, true, true);
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

		// move new certs dir into place. we need to go a level deeper due to the tar file starting
		// at a directory called certificates.
		File subCerts = new File(newCertsDir.getAbsolutePath(), "/certificates");
		boolean movedOkay = subCerts.renameTo(currentCertsDir);
		if (!movedOkay) {
			_logger.error("failed to move the new certificates directory into place");
			return 1;
		}
		// now remove the empty directory that used to have the new certs.
		newCertsDir.delete();

		_logger.info("moved new certificates into place");

		return 0;
	}

	/**
	 * updates the grid-certificates directory in the state directory based on the latest
	 * certificates available in the grid. this function assumes that the consistency lock is held
	 * by the caller. this is important for synchronization.
	 */
	static private int updateGridCertificates() throws FileNotFoundException
	{
		boolean successfulUpdate = false;

		Properties props = CertUpdateHelpers.getCertUpdateProperties();
		StringWriter writer = new StringWriter();
		props.list(new PrintWriter(writer));
		_logger.debug("loaded cert update props, got: " + writer.getBuffer().toString());

		// validate that it is time for the update to occur.
		long currTime = new Date().getTime();
		Long nextUpdate = Long.parseLong(props.getProperty(CertUpdateHelpers.NEXT_UPDATE_PROPNAME));
		if (currTime < nextUpdate) {
			_logger.info("not time for next certificate update yet; skipping it.");
			return 0;
		}

		// check current contents of directory.
		Collection<GeniiPath.PathMixIn> paths = GeniiPath.pathExpander(CertUpdateHelpers.RNS_CERTS_FOLDER + "/*");
		if (paths.isEmpty()) {
			_logger.info("no certificates package found in grid.  skipping update.");
			return 0;
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
				return 0;
			} else {
				certPack = cp;
			}
			if (certPack != null)
				break;
		}
		if (certPack == null) {
			_logger.error("somehow no certificate package paths were valid.");
			return 1;
		}

		// find out what file we last downloaded.
		String lastCertFile = props.getProperty(CertUpdateHelpers.LAST_UPDATE_PACKAGE_PROPNAME);

		// is file name different (or old one didn't exist)? if not different, skip update.
		if (!certPack.getName().equals(lastCertFile)) {
			// we need to update now.
			_logger
				.info("certificate update pack is different; last was " + lastCertFile + " and new is " + certPack.getName());
			try {
				int retval = getCertPackAndInstall(certPack);
				if (retval == 0) {
					successfulUpdate = true;
					// update name for last file used.
					props.setProperty(CertUpdateHelpers.LAST_UPDATE_PACKAGE_PROPNAME, certPack.getName());
					_updatedCertsRecently = true;
				} else {
					return 1;
				}
			} catch (Exception e) {
				_logger.error("failure during retrieval of certificate pack: " + e.getLocalizedMessage());
				_logger.error("failing exception", e);
				return 1;
			}
			_logger.info("updated grid-certificates in local directory");
		} else {
			// it's the same file, so do not update.
			_logger.info("certificate update pack is same as last time (" + lastCertFile + "); will not update.");
			successfulUpdate = true;
		}

		if (successfulUpdate) {
			// reschedule updates for next interval.
			Long interval = Long.parseLong(props.getProperty(CertUpdateHelpers.UPDATE_INTERVAL_PROPNAME));
			if (interval <= 60 * 1000) {
				// reset the interval since checking every minute or less is insane. we could be
				// more stringent here, but want to allow testing at small intervals.
				interval = CertUpdateHelpers.DEFAULT_CERT_UPDATE_INTERVAL;
			}
			long timeNow = new Date().getTime();
			props.setProperty(CertUpdateHelpers.NEXT_UPDATE_PROPNAME, "" + (interval + timeNow));
			CertUpdateHelpers.putCertUpdateProperties(props);
		}

		return 0;
	}

	static public int runGridCertificateUpdates()
	{
		_logger.info("starting check for updated grid certificates");
		// lock here to prevent simultaneous access.
		ThreadAndProcessSynchronizer.acquireLock();

		_updatedCertsRecently = false;

		int toReturn = 1;
		try {
			toReturn = updateGridCertificates();
		} catch (Exception e) {
			_logger.error("failure while updating grid certificates", e);
		}

		// exiting, unlock file lock on cert update properties.
		ThreadAndProcessSynchronizer.releaseLock();

		if (_updatedCertsRecently) {
			// make sure we reload with the latest, but only if we actually updated.
			KeystoreManager.dropTlsTrustStore();
		}

		return toReturn;
	}

//	static public class UpdaterThread implements Runnable
//	{
//		public void run()
//		{
//			while (true) {
//				runGridCertificateUpdates();
//
//				if (_stopTheThread) {
//					return;
//				}
//
//				try {
//					Thread.sleep(UPDATER_THREAD_SNOOZE_DURATION);
//				} catch (InterruptedException e) {
//					if (_stopTheThread) {
//						return;
//					}
//				}
//			}
//		}
//	}
//
//	static public boolean startUpdaterThread()
//	{
//		if (_updaterThread != null) {
//			_logger.error("the updater thread has already been started!");
//			return false;
//		}
//		_stopTheThread = false;
//		_updaterThread = new Thread(new UpdateGridCertsTool.UpdaterThread());
//		_updaterThread.start();
//		return true;
//	}
//
//	static public void stopUpdaterThread()
//	{
//		if (_updaterThread == null) {
//			_logger.error("the updater thread has not yet been started!");
//			return;
//		}
//		_stopTheThread = true;
//		_updaterThread.interrupt();
//		while (_updaterThread.isAlive()) {
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//			}
//			_updaterThread.interrupt();
//		}
//		_updaterThread = null;
//	}

}
