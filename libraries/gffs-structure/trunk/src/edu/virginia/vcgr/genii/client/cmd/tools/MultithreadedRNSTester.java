package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.feistymeow.process.ethread;

import edu.virginia.vcgr.genii.algorithm.math.SimpleRandomizer;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.PermissionDeniedException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;

public class MultithreadedRNSTester extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dtest-rns";
	static final private String _USAGE = "config/tooldocs/usage/utest-rns";
	static final private String _MANPAGE = "config/tooldocs/man/test-rns";

	static private Log _logger = LogFactory.getLog(MultithreadedRNSTester.class);

	/*
	 * the number of threads for testing RNS paths. this should be done with the number of allowed connections per address in mind, since if
	 * there are more threads than connections, some threads will need to wait.
	 */
	int _numberOfThreads = 8;

	public static volatile boolean _threadsShouldStop = false;

	enum DirectoryStatus {
		LOOKUP_NOT_DONE, // haven't tried the lookup yet.
		LOOKUP_OKAY, // lookup succeeded.
		LOOKUP_FAILED_PERMISSION, // the user did not have permissions to do the lookup.
		LOOKUP_FAILED_NON_EXISTENT // there was an error claiming the path didn't exist.

		// hmmm: currently the non-existent failure is pretty bogus. it seems we get these if the directory is empty!
	}

	/*
	 * a map of the directory path to the names in the directory. this will be continually shuffled through and the directories contained in
	 * it will be re-read. if subdirectories didn't exist before in the structure, they are added.
	 */
	public static HashMap<String, DirectoryStatus> _storedDirectories = new HashMap<String, DirectoryStatus>(1000);

	// length of test run, in seconds.
	private long _runtime = 60 * 10;

	public MultithreadedRNSTester()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	/**
	 * a tester that just tries to randomly read directories starting from the root using multiple threads.
	 */
	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		GeniiPath initialLookup = new GeniiPath("/");
		RNSPath firstRNS = initialLookup.lookupRNS();
		_logger.debug("performed first lookup to prime the pump, got dir: " + firstRNS.pwd());

		// test objects.
		TesterListingThread testers[] = new TesterListingThread[_numberOfThreads];
		for (int i = 0; i < _numberOfThreads; i++) {
			testers[i] = new TesterListingThread();
			testers[i].start();
		}

		long endOfRun = (new Date()).getTime() + _runtime * 1000;

		while ((new Date()).getTime() < endOfRun) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				_logger.debug("sleep interrupted");
			}
			// not thread safe but probably not a huge deal...
			_logger.info("holding " + _storedDirectories.size() + " path elements.");

			// if (_logger.isDebugEnabled()) {
			// synchronized (_storedDirectories) {
			// _logger.debug("names are:");
			// Set<String> nameslist = _storedDirectories.keySet();
			// for (String name : nameslist) {
			// _logger.debug(name);
			// }
			// }
			// }
		}

		// we're opting here for a cooperative stop instead of forcing the threads to quit.
		_threadsShouldStop = true;

		// we don't want to stop the threads since that could interrupt our stuck call. we're opting for cooperative stop instead.
		// for (int i = 0; i < NUMBER_OF_TESTER_THREADS; i++) {
		// testers[i].stop();
		// }

		// this bit isn't necessary but i wanted to know the threads weren't locked up on exit.
		boolean allExited = false;
		while (!allExited) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			allExited = true;
			for (int i = 0; i < _numberOfThreads; i++) {
				if (testers[i].threadAlive()) {
					allExited = false;
				}
			}
			if (!allExited) {
				_logger.info("still waiting for threads to shut down...");
			}
		}
		_logger.info("all threads have shut down, program exiting.");

		return 0;
	}

	@Option({ "runtime" })
	public void setRuntime(String seconds)
	{
		_runtime = Long.parseLong(seconds);
	}

	@Option({ "threads" })
	public void setThreads(String threads)
	{
		_numberOfThreads = Integer.parseInt(threads);
	}

	@Override
	protected void verify() throws ToolException
	{
	}

	public static class TesterListingThread extends ethread
	{
		public TesterListingThread()
		{
			super(20); // short period between reads.
		}

		@Override
		public boolean performActivity()
		{
			if (_threadsShouldStop) {
				// our soft sentinel said it's time to go.
				return false;
			}

			String chosenName = null;
			// pick a directory to do a lookup on.
			synchronized (_storedDirectories) {
				// find how many names there are currently.
				Set<String> nameslist = _storedDirectories.keySet();
				boolean likeTheIndex = false; // not true until we're happy with our choice.
				while (!likeTheIndex) {
					int index = SimpleRandomizer.randomInteger(0, nameslist.size());
					Iterator<String> iter = nameslist.iterator();
					int i = 0;
					while (i++ <= index && iter.hasNext()) {
						chosenName = iter.next();
					}
					if (chosenName == null) {
						// jump-start the array by using root for our lookup.
						chosenName = "/";
					}
					DirectoryStatus status = _storedDirectories.get(chosenName);
					if ((status == null) || (status == DirectoryStatus.LOOKUP_OKAY) || (status == DirectoryStatus.LOOKUP_NOT_DONE)) {
						// it's not a failure code here, so say we're happy.
						likeTheIndex = true;
					}
				}
			}
			_logger.debug("chosen name for thread lookup is " + chosenName);

			GeniiPath gpath = new GeniiPath(chosenName);
			RNSPath rpath = gpath.lookupRNS();
			try {
				Collection<RNSPath> contents = rpath.listContents(true);
				String[] names = new String[contents.size()];
				ArrayList<String> newNames = new ArrayList<String>();
				int i = 0;
				// make a simple list of the full names of the contents.
				for (RNSPath name : contents) {
					names[i++] = name.pwd();
					// also record if we see a subdir in this dir. we will add it later if new.
					if (name.isRNS()) {
						newNames.add(name.pwd());
					}
				}
				synchronized (_storedDirectories) {
					// see if we found a new directory to record.
					if (!_storedDirectories.containsKey(rpath.pwd())
						|| (_storedDirectories.get(rpath.pwd()) == DirectoryStatus.LOOKUP_NOT_DONE)) {
						// this directory hadn't been seen yet (or wasn't read yet), so add it.
						_storedDirectories.put(rpath.pwd(), DirectoryStatus.LOOKUP_OKAY);
					}
					// now add any subdirs that were new for future lookup. we will fill them in later.
					for (String name : newNames) {
						if (!_storedDirectories.containsKey(name)) {
							_logger.debug("thread adding new dir " + name);
							_storedDirectories.put(name, DirectoryStatus.LOOKUP_NOT_DONE);
						}
					}
				}
			} catch (Throwable e) {
				_logger.info("could not successfully lookup contents of: " + rpath.pwd() + ", exception was: "
					+ e.getClass().getCanonicalName());
				synchronized (_storedDirectories) {
					// we only want to mark it as bad if this is an access problem! temporary screwup problems should be tried again.
					if (e instanceof PermissionDeniedException || e instanceof RNSPathDoesNotExistException) {
						// we will never mark the root as bad, since then we are hosed for any further lookups.
						if (!rpath.isRoot()) {
							if (_storedDirectories.get(rpath.pwd()) == DirectoryStatus.LOOKUP_NOT_DONE) {
								// we only whack the item if it didn't have any existing contents.
								_logger.info("removing path permanently from consideration: " + rpath.pwd());
								_storedDirectories.put(rpath.pwd(),
									(e instanceof PermissionDeniedException) ? DirectoryStatus.LOOKUP_FAILED_PERMISSION
										: DirectoryStatus.LOOKUP_FAILED_NON_EXISTENT);
							} else {
								// this one had already been looked up in some way, so just mention this if it used to be good.
								if (_storedDirectories.get(rpath.pwd()) == DirectoryStatus.LOOKUP_OKAY) {
									_logger.error("had a bad directory lookup on a previously okay dir: " + rpath.pwd(), e);
								}
							}
						} else {
							_logger.error("crazy permission error when looking up root!", e);
						}
					}
				}
			}

			return true;
		}

	}
}
