package edu.virginia.vcgr.genii.client.rns;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.algorithm.structures.cache.TimedOutLRUCache;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;

/**
 * A new type of cache for RNS objects that does not rely on access patterns to keep things fresh.
 * This is based on the belief that the root and other EPRs seldom change, so why do we create
 * RNSPath objects containing these all the time? Instead, these should come from the cache, and be
 * very quick to look up. This cache does have to be cleared for some EPR invalidating events, such
 * as the ConnectTool and when a resolver is added.
 * 
 * @author Chris Koeritz
 */
public class CriticalPathFromRootCache

// implements CommonCache
// hmmm: remember to add common cache interface for this!

{
	static private Log _logger = LogFactory.getLog(CriticalPathFromRootCache.class);

	/*
	 * we cache at most 4 tiers of paths, where the root is a special case as the first tier. so
	 * this would include paths such as /home/xsede.org/fred but not /home/xsede.org/fred/sandbox.
	 */
	private final int MAX_TIERS_CACHED = 4;
	/*
	 * each tier is allowed N times as many items as the previous tier. so if we choose 8, there is
	 * one item on the first tier, 8 on the second tier, 64 on third, and 512 on fourth tier.
	 */
	private final int TIER_MULTIPLICATION_FACTOR = 8;

	// how long entries are allowed to reside in the cache when recently used, in milliseconds.
	private final int MAXIMUM_CACHE_LIFETIME = 4 * 60 * 1000;
	// how long entries can be cached if they have no cache hits.
	// private final int UNUSED_ITEM_LIFETIME = 30 * 1000;

	private final char SEPARATOR = '/';
	private final String SEPARATOR_STRING = "/";

	private ArrayList<PathsOnTier> tierRecords = new ArrayList<PathsOnTier>(MAX_TIERS_CACHED);

	public CriticalPathFromRootCache()
	{
		for (int i = 0; i < MAX_TIERS_CACHED; i++) {
			tierRecords.add(new PathsOnTier(i, (int) Math.pow(TIER_MULTIPLICATION_FACTOR, i)));
		}
	}

	/**
	 * this class is a set of cached paths that are at the same height in the RNS tree.
	 */
	private class PathsOnTier
	{
		TimedOutLRUCache<String, RNSPath> _tierEntries;
		int _depth; // our depth from the root, where zero means we are at the root.

		PathsOnTier(int depthHere, int maximumEntries)
		{
			_depth = depthHere;
			_tierEntries = new TimedOutLRUCache<String, RNSPath>(maximumEntries, MAXIMUM_CACHE_LIFETIME);
		}

		public int getDepth()
		{
			return _depth;
		}

		/**
		 * attempts to locate the "path" specified in the cache. if it cannot be found, it will be
		 * looked up instead. it is inappropriate to ask about paths that are at a different depth.
		 */
		public RNSPath findPath(String path) throws RNSPathDoesNotExistException, RNSPathAlreadyExistsException
		{
			synchronized (_tierEntries) {
				RNSPath found = _tierEntries.get(path);

				// hmmm: cache is disabled!!!!
				boolean turnedOff = true;
				if (turnedOff)
					found = null; // temp
				if (found != null) {
					if (_logger.isDebugEnabled())
						_logger.debug("CPFRC hit: " + path);
					return found;
				} else {
					if (_logger.isDebugEnabled())
						_logger.debug("CPFRC miss: " + path);
					found = RNSPath.getCurrent().lookupNoCaching(path, RNSPathQueryFlags.DONT_CARE);
					int pathDepth = calculateTier(path);
					if (pathDepth != getDepth()) {
						_logger.error("ignoring erroneous attempt to cache a path at wrong depth: our depth is " + _depth
							+ " and path is at depth " + pathDepth + " with contents: " + path);
					} else {
						if (found != null)
							addPath(path, found);
					}
				}
				return found;
			}
		}

		/**
		 * attempts to add the RNS path "rpath" into the cache under the absolute path "path". if
		 * the "rpath" is null, this is not an error and is taken to mean that the entry for the
		 * path should be invalidated.
		 */
		public void addPath(String path, RNSPath rpath)
		{
			if (path == null) {
				String msg = "failure in addPath: path is null.";
				_logger.error(msg);
				return; // skip it.
			}
			synchronized (_tierEntries) {
				RNSPath found = _tierEntries.get(path);
				if ((found != null) && (rpath != null)) {
					if (_logger.isDebugEnabled())
						_logger.debug("poor planning here; re-adding object for same path: " + path);
				}
				_tierEntries.remove(path);
				// only add it if there's something to add. a null add means remove the path.
				if (rpath != null) {
					_tierEntries.put(path, rpath);
				} else {
					if (_logger.isDebugEnabled())
						_logger.debug("wiped out any cached path for: " + path);
				}
			}
		}
	}

	/**
	 * returns true if the path is at an appropriate depth for being stored in this cache. this will
	 * only operate properly on absolute paths.
	 */
	public boolean appropriateDepth(String path)
	{
		return calculateTier(path) < MAX_TIERS_CACHED;
	}

	/**
	 * alternative check for when the depth is already known. \
	 */
	public boolean appropriateDepth(int depth)
	{
		return depth < MAX_TIERS_CACHED;
	}

	public boolean isDir(RNSPath check)
	{
		try {
			TypeInformation info = new TypeInformation(check.getEndpoint());
			return (info.isRNS() && info.isEnhancedRNS() && !info.isRByteIO());
		} catch (Throwable t) {
			_logger.error("failed isDir check with exception.", t);
			return false;
		}
	}

	public void invalidate(String path)
	{
		int tier = calculateTier(path);
		// bail if we don't even cache at that level.
		if (!appropriateDepth(tier))
			return;

		PathsOnTier tierRecord = tierRecords.get(tier);
		tierRecord.addPath(path, null);
	}

	public void add(RNSPath newPath)
	{
		int tier = calculateTier(newPath.pwd());
		// bail if we don't even cache at this level.
		if (!appropriateDepth(tier))
			return;

		PathsOnTier tierRecord = tierRecords.get(tier);
		tierRecord.addPath(newPath.pwd(), newPath);
	}

	// hmmm: move the helpful path utilities out of here to lower level jar.

	/**
	 * removes any double separator characters.
	 */
	public String canonicalize(String path)
	{
		StringBuilder toReturn = new StringBuilder(path.length());
		boolean lastWasSlash = true;
		int indy = 1;
		while (indy < path.length()) {
			if (path.charAt(indy) == SEPARATOR) {
				if (lastWasSlash == true) {
					// that's at least two in a row, so skip this one.
					indy++;
					continue;
				}
				lastWasSlash = true;
			} else {
				lastWasSlash = false;
			}
			toReturn.append(path.charAt(indy));
			indy++;
		}

		return toReturn.toString();
	}

	/**
	 * returns the Nth name found at the componentIndex. if the componentIndex is zero, this is the
	 * first component, which is blank since it's the root. all other component indices up to the
	 * value returned by calculateTier should be non-empty.
	 */
	public String getComponent(String path, int componentIndex)
	{
		String[] pieces = canonicalize(path).split(SEPARATOR_STRING, 99);
		return pieces[componentIndex];
	}

	/**
	 * returns the integral index of this path's tier, where the root is zero. this method only
	 * works properly on absolute paths. it will handle non-canonical paths though.
	 */
	public int calculateTier(String path)
	{
		int slashesFound = 0;
		int nextPlaceToLook = 1; // skip the slash in the front (absolute path required!).
		while (nextPlaceToLook < path.length()) {
			int indy = path.indexOf(SEPARATOR, nextPlaceToLook);
			if (indy < 0)
				return slashesFound;
			if (indy == nextPlaceToLook) {
				// special case for two slashes in a row.
				nextPlaceToLook++;
				continue;
			}
			nextPlaceToLook = indy + 1;
			slashesFound++;
		}
		return slashesFound;
	}

	public RNSPath lookupPath(String path, RNSPathQueryFlags queryFlag) throws RNSPathDoesNotExistException,
		RNSPathAlreadyExistsException
	{
		int tier = calculateTier(path);
		// bail if we don't even cache at this level, but still return the path object.
		if (!appropriateDepth(tier)) {
			return RNSPath.getCurrent().lookupNoCaching(path, queryFlag);
		}
		PathsOnTier tierRecord = tierRecords.get(tier);
		return tierRecord.findPath(path);
	}
}
