package edu.virginia.vcgr.smb.server;

import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.algorithm.filesystem.FileSystemHelper;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;

// future: not finished?
public class SMBTree
{
	static private Log _logger = LogFactory.getLog(SMBTree.class);

	private RNSPath root;
	private HashMap<Integer, SMBFile> files = new HashMap<Integer, SMBFile>();
	private HashMap<Integer, SMBSearchState> searches = new HashMap<Integer, SMBSearchState>();

	public SMBTree(RNSPath root)
	{
		this.root = root;
	}

	public RNSPath getRoot()
	{
		return root;
	}

	public int allocateFID(SMBFile file) throws SMBException
	{
		/* future: improve */
		for (int i = 0; i < 0x10000; i++) {
			if (files.get(i) == null) {
				files.put(i, file);

				return i;
			}
		}

		throw new SMBException(NTStatus.INSUFF_SERVER_RESOURCES);
	}

	public SMBFile verifyFID(int FID) throws SMBException
	{
		SMBFile file = files.get(FID);
		if (file == null)
			// The CIFS document reports to use FID
			throw new SMBException(NTStatus.SMB_BAD_FID);
		return file;
	}

	public int allocateSID(SMBSearchState search) throws SMBException
	{
		/* future: improve */
		for (int i = 0; i < 0x10000; i++) {
			if (searches.get(i) == null) {
				searches.put(i, search);

				return i;
			}
		}

		throw new SMBException(NTStatus.INSUFF_SERVER_RESOURCES);
		// future: maybe OS2_NO_MORE_SIDS
	}

	public SMBSearchState verifySID(int SID) throws SMBException
	{
		SMBSearchState search = searches.get(SID);
		if (search == null)
			// The CIFS document reports to use FID
			throw new SMBException(NTStatus.SMB_BAD_FID);
		return search;
	}

	public void releaseFID(int FID)
	{
		files.remove(FID);
	}

	public void releaseSID(int SID)
	{
		searches.remove(SID);
	}
	
	//hmmm: case-insensitive matching should be turned off since it is awfully slow.
	//hmmm: but we have also found it cannot be turned off because windows whips out these all upper case versions of our file names sometimes.
	// the protection we did by ignoring all those bogus file names may make this usable again?

	private static RNSPath lookupInsensitive(RNSPath dir, String file)
	{
		RNSPath trivial = dir.lookup(file);
		if (trivial.exists()) {
			if (_logger.isDebugEnabled())
				_logger.debug("insense: success looking up trivial path: " + file);
			return trivial;
		}

		SMBWildcard pattern = new SMBWildcard(file);
		try {
			Collection<RNSPath> list = dir.listContents(pattern);
			if (list.isEmpty()) {
				if (_logger.isDebugEnabled())
					_logger.debug("insense: failure looking up path with pattern: " + file);
				return trivial;
			}

			return list.iterator().next();
		} catch (RNSPathDoesNotExistException e) {
			if (dir.isRoot()) {
				if (_logger.isDebugEnabled())
					_logger.debug("insense: failure looking up path (non-existent): " + file);
				return trivial;
			}

			// Maybe this directory needs to be resolved
			dir = lookupInsensitive(dir.getParent(), dir.getName());
			if (_logger.isDebugEnabled())
				_logger.debug("insense: after looking up parent, dir now: " + dir.pwd());

		} catch (RNSException e) {
			if (_logger.isDebugEnabled())
				_logger.debug("insense: failure finding dir: " + file);
			return trivial;
		}

		trivial = dir.lookup(file);
		if (trivial.exists()) {
			if (_logger.isDebugEnabled())
				_logger.debug("insense: success looking up file (post dir find): " + dir.pwd());
			return trivial;
		}

		try {
			Collection<RNSPath> list = dir.listContents(pattern);
			if (list.isEmpty()) {
				if (_logger.isDebugEnabled())
					_logger.debug("insense: failure looking up path with pattern (place 2): " + file);
				return trivial;
			}

			if (_logger.isDebugEnabled())
				_logger.debug("insense: success looking up via pattern (place 2): " + file);
			return list.iterator().next();
		} catch (RNSPathDoesNotExistException e) {
			if (_logger.isDebugEnabled())
				_logger.debug("insense: failure looking up path (non-existent place 2): " + file);
			return trivial;
		} catch (RNSException e) {
			if (_logger.isDebugEnabled())
				_logger.debug("insense: failure looking up path (rns exception): " + file, e);
			return trivial;
		}
	}

	public static RNSPath lookup(RNSPath root, String path, boolean caseSensitive) throws SMBException
	{
		if (_logger.isDebugEnabled())
			_logger.debug("SMBTree looking up path(3): " + path);

		if (path.isEmpty())
			return root;

		path = FileSystemHelper.sanitizeFilename(path);

		/*
		 * hmmm: !!!! ham-handed kludge for the names windows keeps asking us about and which cause us to endlessly look for things in the grid.
		 */
		if (path.endsWith("folder.gif") ||
			path.endsWith("folder.jpg") ||
			path.endsWith("Thumbs.db") ||
			path.endsWith("desktop.ini") ) {
			if (_logger.isDebugEnabled())
				_logger.debug("ignoring windows specific file name: " + path);
			throw new SMBException(NTStatus.NO_SUCH_FILE);
		}
		
		if (path.endsWith("srvsvc")) {
			// attempting to shut it up fast on srvsvc, although this is more problematic than these others.
			// it indicates a whole slew of expected services.
			
			if (_logger.isDebugEnabled())
				_logger.debug("special path seen; pretending it's root: " + path);
			
			///throw new SMBException(NTStatus.NOT_IMPLEMENTED);
			
			// pretend they're asking about the root.
			return root;
		}			

		
		RNSPath file = root.lookup(path);
		
		//hmmm: disabling case insensitive searches here.
//		return file; //added to disable case insensitivity.
		// File exists or no case-insensitive search needed?
		if (caseSensitive || file.exists()) {
			if (_logger.isDebugEnabled())
				_logger.debug("success looking up path: " + path);
			return file;
		}
		return lookupInsensitive(file.getParent(), file.getName());
	}

	public RNSPath lookup(String path, boolean caseSensitive) throws SMBException
	{
		return SMBTree.lookup(root, path, caseSensitive);
	}

	public static TypeInformation stat(RNSPath file) throws SMBException
	{
		try {
			return new TypeInformation(file.getEndpoint());
		} catch (RNSPathDoesNotExistException e) {
			_logger.error("failed to find path: " + file.pwd(), e);
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		}
	}

	public static SMBFile open(RNSPath file, int fileAttr, boolean create, boolean excl, boolean trunc) throws SMBException
	{
		EndpointReferenceType epr;

		try {
			if (create) {
				if ((fileAttr & SMBFileAttributes.DIRECTORY) != 0) {
					file.mkdir();

					epr = file.getEndpoint();
				} else {
					epr = file.createNewFile();
				}
			} else {
				epr = file.getEndpoint();
			}
		} catch (RNSPathAlreadyExistsException e) {
			if (excl) {
				throw new SMBException(NTStatus.OBJECT_NAME_COLLISION);
			} else {
				try {
					epr = file.getEndpoint();
				} catch (RNSPathDoesNotExistException e2) {
					// This should never happen; hopefully
					throw new SMBException(NTStatus.NO_SUCH_FILE);
				}
			}
		} catch (RNSPathDoesNotExistException e) {
			if (create) {
				throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
			} else {
				throw new SMBException(NTStatus.NO_SUCH_FILE);
			}
		} catch (RNSException e) {
			// Guess
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}

		TypeInformation info = new TypeInformation(epr);
		SMBFile handle;
		if (info.isRByteIO()) {
			handle = SMBRByteIOFile.fromRNS(file, epr);
		} else {
			handle = new SMBGenericIOFile(file, epr);
		}

		if (trunc)
			handle.truncate();

		return handle;
	}

	public static void rm(RNSPath dir) throws SMBException
	{
		try {
			// future: is this correct
			dir.delete();
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.NO_SUCH_FILE);
		} catch (RNSException e) {
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}

	public static Collection<RNSPath> listContents(RNSPath dir, RNSFilter pattern) throws SMBException
	{
		try {
			return dir.listContents(pattern);
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		} catch (RNSException e) {
			// Can be anything really
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}

	public Collection<RNSPath> listContents(String path, boolean caseSensitive) throws SMBException
	{
		path = FileSystemHelper.sanitizeFilename(path);
		int sep = path.lastIndexOf('/');
		if (sep == -1) {
			// Only filename
			return SMBTree.listContents(root, new SMBWildcard(path));
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.listContents(dir, new SMBWildcard(path.substring(sep + 1)));
		}
	}

	public Collection<RNSPath> listContents(String path, RNSFilter filter, boolean caseSensitive) throws SMBException
	{
		path = FileSystemHelper.sanitizeFilename(path);
		int sep = path.lastIndexOf('/');
		if (sep == -1) {
			// Only filename
			return SMBTree.listContents(root, new SMBSearchFilter(new SMBWildcard(path), filter));
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.listContents(dir, new SMBSearchFilter(new SMBWildcard(path.substring(sep + 1)), filter));
		}
	}

	public static SMBSearchState search(RNSPath dir, SMBWildcard pattern) throws SMBException
	{
		Collection<RNSPath> list = listContents(dir, pattern);

		RNSPath dot = null, dotdot = null;
		// Seems to work; probably wrong
		if (pattern.matches("")) {
			dot = dir;
			dotdot = dir.getParent();
		}

		return new SMBSearchState(dot, dotdot, list);
	}

	public static SMBSearchState search(RNSPath dir, SMBWildcard pattern, RNSFilter filter) throws SMBException
	{
		Collection<RNSPath> list = listContents(dir, new SMBSearchFilter(pattern, filter));

		RNSPath dot = null, dotdot = null;
		// Seems to work; probably wrong
		if (pattern.matches("")) {
			dot = dir;
			dotdot = dir.getParent();
		}

		return new SMBSearchState(dot, dotdot, list);
	}

	public SMBSearchState search(String path, boolean caseSensitive) throws SMBException
	{
		path = FileSystemHelper.sanitizeFilename(path);
		int sep = path.lastIndexOf('/');
		if (sep == -1) {
			// Only filename
			return SMBTree.search(root, new SMBWildcard(path));
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.search(dir, new SMBWildcard(path.substring(sep + 1)));
		}
	}

	public SMBSearchState search(String path, RNSFilter filter, boolean caseSensitive) throws SMBException
	{
		path = FileSystemHelper.sanitizeFilename(path);
		int sep = path.lastIndexOf('/');
		if (sep == -1) {
			// Only filename
			return SMBTree.search(root, new SMBWildcard(path), filter);
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.search(dir, new SMBWildcard(path.substring(sep + 1)), filter);
		}
	}

	public void rename(RNSPath oldPath, RNSPath newPath) throws SMBException
	{
		EndpointReferenceType epr;
		try {
			epr = oldPath.getEndpoint();
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.NO_SUCH_FILE);
		}

		try {
			newPath.link(epr);
		} catch (RNSPathAlreadyExistsException e) {
			throw new SMBException(NTStatus.OBJECT_NAME_COLLISION);
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		} catch (RNSException e) {
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}

		try {
			oldPath.unlink();
		} catch (RNSPathDoesNotExistException e) {
			// Whatever
		} catch (RNSException e) {
			// Should we remove the new file; probably not, just in case
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}
}