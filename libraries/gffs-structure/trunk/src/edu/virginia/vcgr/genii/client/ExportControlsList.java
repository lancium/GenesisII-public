package edu.virginia.vcgr.genii.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileChangeTracker;
import edu.virginia.vcgr.genii.client.ExportControl.ModeAllowance;

/**
 * Implements a set of restrictions on where exports may be created in the local filesystem. The default allowance at the root of the file
 * system is *disallow*, meaning that nothing can be exported until some configuration line allows it to be. However, if the file is
 * completely empty of restrictions, then that is currently interpreted as meaning that all paths are allowed to be exported (which
 * coincidentally matches the initial version of the configuration files that were released).
 */
public class ExportControlsList
{
	private static Log _logger = LogFactory.getLog(ExportControlsList.class);

	// file that holds onto the list of restrictions, usually read from export properties.
	File _restrictionsFile = null;

	// watches the restriction file for any changes.
	FileChangeTracker _watcher = null;

	// cached copy of all restrictions.
	ArrayList<ExportControl> _restrictions = new ArrayList<ExportControl>();

	// the single export controls list constructed per application.
	static ExportControlsList _theExpoCtrlList = null;

	/**
	 * to allow simple management of object at a static level, we provide this accessor to get the single extant copy expected.
	 */
	public static synchronized ExportControlsList getExportControlsList()
	{
		if (_theExpoCtrlList == null) {
			_theExpoCtrlList = new ExportControlsList();
		}
		return _theExpoCtrlList;
	}

	/**
	 * full blown normal constructor that uses default restrictions file to load the list.
	 */
	private ExportControlsList()
	{
		constructClassMembers();
	}

	private synchronized void constructClassMembers()
	{
		_restrictionsFile = InstallationProperties.getInstallationProperties().getExportCreationRestrictionsFile();
		_watcher = new FileChangeTracker(_restrictionsFile);
		boolean worked = readRestrictionsFromFile(_restrictionsFile);
		if (!worked) {
			/*
			 * we don't know what is desired here, and our scheme says that if the file is missing we disallow all exports.
			 */
			_logger.error("reading the export restrictions file has failed; disabling export creation.");
		} else {
			/*
			 * we are hamstrung here by the original config file we sent out, and which we cannot allow the installer to just stomp on. so, if
			 * the file is absolutely empty, then we grant rwf to the root of the file system.
			 */
			if (_restrictions.size() == 0) {
				// they have not specified any restrictions at all. our scheme for this case is to
				// allow all exports.
				_restrictions.add(new ExportControl("/", "*", ModeAllowance.getAllModes()));
				if (_logger.isDebugEnabled())
					_logger.debug("adding default allowance of all exports due to empty restrictions list");
			}
		}
	}

	/**
	 * testing version does not set up any of the data members.
	 */
	public static class TestingFlag
	{}

	public ExportControlsList(TestingFlag tf)
	{
	}

	public List<ExportControl> getAllRestrictions()
	{
		return _restrictions;
	}

	/**
	 * parses a list of restrictions from the file specified and stores it here.
	 */
	public boolean readRestrictionsFromFile(File restrictionsFile)
	{
		FileInputStream f = null;
		// open up the restrictions file as an input stream.
		try {
			f = new FileInputStream(restrictionsFile);
		} catch (FileNotFoundException e) {
			return false;
		}
		return readRestrictionsFromStream(f);
	}

	/**
	 * parses a list of restrictions from the stream specified and stores it here.
	 */
	public boolean readRestrictionsFromStream(InputStream stream)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		// iterate through the file, grabbing whole lines and parsing them.
		String line = null;
		while (true) {
			// read the next line.
			line = ExportControl.readNextLine(reader);
			// drop out if we are at end of file.
			if (line.length() == 0)
				break;
			if (_logger.isDebugEnabled())
				_logger.debug("read line: '" + line + "'");
			ExportControl r = ExportControl.parseLine(line);
			if (r != null) {
				_restrictions.add(r);
			}
		}
		// no errors means we're sort of successful...
		return true;
	}

	/**
	 * retrieves all restrictions that apply to the path and user in question.
	 */
	List<ExportControl> findAppropriateRestrictions(String path, String user)
	{
		ArrayList<ExportControl> toReturn = new ArrayList<ExportControl>();

		if (_logger.isDebugEnabled())
			_logger.debug("finding restrictions for path='" + path + "' and user='" + user + "'");

		// scan the list of restrictions.
		for (ExportControl r : _restrictions) {
			if (_logger.isDebugEnabled())
				_logger.debug("considering restriction: " + r);
			if (r.isUserAppropriate(user) && (r.isPathAppropriate(path) > 0)) {
				// add this restriction that is appropriate for the requested path.
				toReturn.add(r);

				if (_logger.isDebugEnabled())
					_logger.debug("restriction gets included!: " + r);

			}
		}
		return toReturn;
	}

	/**
	 * finds the most relevant restriction or allowance for the path in question. this just finds the restriction of the highest
	 * appropriateness value. note that this expects the appropriateness for a particular user to have already been established in the list of
	 * "restrictions" passed in, which is why this is not considering the user name.
	 */
	ExportControl getMostRelevantRestriction(List<ExportControl> restrictions, String path)
	{
		ExportControl toReturn = null;
		int priorRelevance = -1;
		for (ExportControl res : restrictions) {
			// test whether the path is appropriate or not.
			int currentRelevance = res.isPathAppropriate(path);
			if ((toReturn == null) || (currentRelevance > priorRelevance)) {
				// the current item is more relevant than the previous choice.
				toReturn = res;
				priorRelevance = currentRelevance;
			} else if (currentRelevance == toReturn.isPathAppropriate(path)) {
				/*
				 * the current item is the same level of relevance as previous choice, which now means we have to decide which one is more
				 * tightly bound, i.e. whichever item matches the exact user name should win.
				 */
				if (toReturn.allowAnyUser()) {
					/*
					 * okay, the previous one was less tightly bound (probably). if they have two paths which are both wildcards, then the
					 * later one gets to win. it makes no sense to have two that are both wildcards, but it's not explicitly disallowed
					 * either.
					 */
					toReturn = res;
				}
			}
		}
		return toReturn;
	}

	/**
	 * finds the most relevant restriction or allowance for a path given a requested mode of access. if true is returned, then it means there
	 * are no restrictions that disallow that mode. false means the request cannot be granted.
	 */
	boolean checkMostRelevantRestriction(String path, String user, Set<ModeAllowance> modesRequested)
	{
		List<ExportControl> restrictions = findAppropriateRestrictions(path, user);
		if (restrictions == null) {
			// nothing matched at all? not very permissive.
			return false;
		}

		ExportControl mostRel = getMostRelevantRestriction(restrictions, path);

		if (_logger.isDebugEnabled())
			_logger.debug("most relevant restriction is: " + mostRel);

		for (ModeAllowance m : modesRequested) {
			boolean okay = mostRel.actionAllowed(m);
			if (!okay)
				return false;
		}
		return true;
	}

	/**
	 * tests a path to see whether exports are allowed on it or not for a particular user. the "modes" list supplies all rights that are
	 * desired. if any of those is missing for the "path" specified, then false is returned.
	 */
	public boolean checkCreationOkay(String path, String user, Set<ExportControl.ModeAllowance> modes)
	{
		if ((_watcher != null) && _watcher.hasFileChanged()) {
			if (_logger.isDebugEnabled())
				_logger.debug("export controls file changed; reloading: " + _restrictionsFile);
			constructClassMembers();
		}

		try {
			Path realPath = Paths.get(path).toRealPath((LinkOption) null);
			if (path != realPath.toString()) {
				_logger.debug("did see a change in the path, probably due to a link...");
				_logger.debug("...changed from " + path + " to " + realPath.toString());
			}
			path = realPath.toString();
		} catch (Exception e) {
			// we don't want to blow up on this since some platforms may not implement same way.
			_logger.error("could not lookup path, so did not convert to real form: '" + path + "'");
		}

		// this part is just guaranteeing that every mode they requested is allowed by some set of
		// restrictions.
		return checkMostRelevantRestriction(path, user, modes);
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (ExportControl ec : _restrictions) {
			sb.append(ec.toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}
