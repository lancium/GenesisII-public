package edu.virginia.vcgr.smb.server;
import java.util.Collection;
import java.util.HashMap;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;


// TODO
public class SMBTree {
	private RNSPath root;
	private HashMap<Integer, SMBFile> files = new HashMap<Integer, SMBFile>();
	private HashMap<Integer, SMBSearchState> searches = new HashMap<Integer, SMBSearchState>();
	
	public SMBTree(RNSPath root) {
		this.root = root;
	}
	
	public RNSPath getRoot() {
		return root;
	}
	
	public int allocateFID(SMBFile file) throws SMBException {
		/* TODO: improve */
		for (int i = 0; i < 0x10000;i++) {
			if (files.get(i) == null) {
				files.put(i, file);
				
				return i;
			}
		}

		throw new SMBException(NTStatus.INSUFF_SERVER_RESOURCES);
	}
	
	public SMBFile verifyFID(int FID) throws SMBException {
		SMBFile file = files.get(FID);
		if (file == null)
			// The CIFS document reports to use FID
			throw new SMBException(NTStatus.SMB_BAD_FID);
		return file;
	}
	
	public int allocateSID(SMBSearchState search) throws SMBException {
		/* TODO: improve */
		for (int i = 0; i < 0x10000;i++) {
			if (searches.get(i) == null) {
				searches.put(i, search);
				
				return i;
			}
		}

		throw new SMBException(NTStatus.INSUFF_SERVER_RESOURCES);// XXX: maybe OS2_NO_MORE_SIDS
	}
	
	public SMBSearchState verifySID(int SID) throws SMBException {
		SMBSearchState search = searches.get(SID);
		if (search == null)
			// The CIFS document reports to use FID
			throw new SMBException(NTStatus.SMB_BAD_FID);
		return search;
	}
	
	public void releaseFID(int FID) {
		files.remove(FID);
	}
	
	public void releaseSID(int SID) {
		searches.remove(SID);
	}

	public static String pathNormalize(String path) {
		// TODO: improve this
		path = path.replace('\\', '/');
		while (path.length() > 0 && path.charAt(0) == '/')
			path = path.substring(1);
		
		return path;
	}
	
	private static RNSPath lookupInsensitive(RNSPath dir, String file) {
		SMBWildcard pattern = new SMBWildcard(file);
		
		RNSPath trivial = dir.lookup(file);
		if (trivial.exists())
			return trivial;
		
		try {
			Collection<RNSPath> list = dir.listContents(pattern);
			if (list.isEmpty())
				return trivial;
			
			return list.iterator().next();
		} catch (RNSPathDoesNotExistException e) {
			if (dir.isRoot())
				return trivial;
			
			// Maybe this directory needs to be resolved
			dir = lookupInsensitive(dir.getParent(), dir.getName());
		} catch (RNSException e) {
			return trivial;
		}
		
		trivial = dir.lookup(file);
		if (trivial.exists())
			return trivial;
		
		try {
			Collection<RNSPath> list = dir.listContents(pattern);
			if (list.isEmpty())
				return trivial;
			
			return list.iterator().next();
		} catch (RNSPathDoesNotExistException e) {
			return trivial;
		} catch (RNSException e) {
			return trivial;
		}
	}
	
	private static RNSPath lookup(RNSPath root, String path) {
		if (path.isEmpty())
			return root;
		
		// TODO: improve this
		path = path.replace('\\', '/');
		while (path.length() > 0 && path.charAt(0) == '/')
			path = path.substring(1);
		
		return root.lookup(path);
	}
	
	public static RNSPath lookup(RNSPath root, String path, boolean caseSensitive) {
		RNSPath file = lookup(root, path);
		
		// File exists no case-insensitive search needed
		if (caseSensitive || file.exists())
			return file;
		
		return lookupInsensitive(file.getParent(), file.getName());
	}
	
	public RNSPath lookup(String path, boolean caseSensitive) {
		return SMBTree.lookup(root, path, caseSensitive);
	}

	public static TypeInformation stat(RNSPath file) throws SMBException {
		try {
			return new TypeInformation(file.getEndpoint());
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		}
	}
	
	public static SMBFile open(RNSPath file, int fileAttr, boolean create, boolean excl, boolean trunc) throws SMBException {
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

	public static void rm(RNSPath dir) throws SMBException {
		try {
			// XXX: is this correct
			dir.delete();
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.NO_SUCH_FILE);
		} catch (RNSException e) {
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}

	public static Collection<RNSPath> listContents(RNSPath dir, RNSFilter pattern) throws SMBException {
		try {
			return dir.listContents(pattern);
		} catch (RNSPathDoesNotExistException e) {
			throw new SMBException(NTStatus.OBJECT_PATH_NOT_FOUND);
		} catch (RNSException e) {
			// Can be anything really
			throw new SMBException(NTStatus.ACCESS_DENIED);
		}
	}
	
	public Collection<RNSPath> listContents(String path, boolean caseSensitive) throws SMBException {
		int sep = path.lastIndexOf('\\');
		if (sep == -1) {
			// Only filename
			return SMBTree.listContents(root, new SMBWildcard(path));
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.listContents(dir, new SMBWildcard(path.substring(sep + 1)));
		}
	}
	
	public Collection<RNSPath> listContents(String path, RNSFilter filter, boolean caseSensitive) throws SMBException {
		int sep = path.lastIndexOf('\\');
		if (sep == -1) {
			// Only filename
			return SMBTree.listContents(root, new SMBSearchFilter(new SMBWildcard(path), filter));
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.listContents(dir, new SMBSearchFilter(new SMBWildcard(path.substring(sep + 1)), filter));
		}
	}
	
	public static SMBSearchState search(RNSPath dir, SMBWildcard pattern) throws SMBException {
		Collection<RNSPath> list = listContents(dir, pattern);
		
		RNSPath dot = null, dotdot = null;
		// Seems to work; probably wrong
		if (pattern.matches("")) {
			dot = dir;
			dotdot = dir.getParent();
		}
		
		return new SMBSearchState(dot, dotdot, list);
	}
	
	public static SMBSearchState search(RNSPath dir, SMBWildcard pattern, RNSFilter filter) throws SMBException {
		Collection<RNSPath> list = listContents(dir, new SMBSearchFilter(pattern, filter));
		
		RNSPath dot = null, dotdot = null;
		// Seems to work; probably wrong
		if (pattern.matches("")) {
			dot = dir;
			dotdot = dir.getParent();
		}
		
		return new SMBSearchState(dot, dotdot, list);
	}
	
	public SMBSearchState search(String path, boolean caseSensitive) throws SMBException {
		int sep = path.lastIndexOf('\\');
		if (sep == -1) {
			// Only filename
			return SMBTree.search(root, new SMBWildcard(path));
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.search(dir, new SMBWildcard(path.substring(sep + 1)));
		}
	}
	
	public SMBSearchState search(String path, RNSFilter filter, boolean caseSensitive) throws SMBException {
		int sep = path.lastIndexOf('\\');
		if (sep == -1) {
			// Only filename
			return SMBTree.search(root, new SMBWildcard(path), filter);
		} else {
			RNSPath dir = lookup(path.substring(0, sep), caseSensitive);
			return SMBTree.search(dir, new SMBWildcard(path.substring(sep + 1)), filter);
		}
	}

	public void rename(RNSPath oldPath, RNSPath newPath) throws SMBException {
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