package edu.virginia.vcgr.genii.client.gfs.cache;

import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FSFilesystem;
import edu.virginia.vcgr.fsii.FileHandleTable;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSEntryAlreadyExistsException;
import edu.virginia.vcgr.fsii.exceptions.FSEntryNotFoundException;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.exceptions.FSNotADirectoryException;
import edu.virginia.vcgr.fsii.exceptions.FSNotAFileException;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.gfs.GenesisIIFilesystem;
import edu.virginia.vcgr.genii.client.gfs.cache.handles.GeniiOpenDirHandle;
import edu.virginia.vcgr.genii.client.gfs.cache.handles.GeniiOpenFileHandle;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedDir;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedFile;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedResource;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class GenesisIICachedFilesystem implements FSFilesystem{
	
	private static final int CACHE_LIFETIME = 30;
	private GeniiCacheManager _cacheManager = null;
	private FileHandleTable<GeniiOpenFileHandle> _resourceHandleTable =
		new FileHandleTable<GeniiOpenFileHandle>(1024);
	private GenesisIIFilesystem _fs = null;
	
	public GenesisIICachedFilesystem(GenesisIIFilesystem fs) {
		_cacheManager = new GeniiCacheManager(CACHE_LIFETIME, fs);
		_fs = fs;
	}

	@Override
	public void chmod(String[] path, Permissions permissions)
			throws FSException {
		//Pass straight through
		_fs.chmod(path, permissions);		
	}

	@Override
	public void close(long fileHandle) throws FSException {		
		GeniiOpenFileHandle gofh = _resourceHandleTable.get((int)fileHandle);
		if(gofh != null) {
			_resourceHandleTable.release((int)fileHandle);				
			gofh.close();
		}						
	}

	@Override
	public void flush(long fileHandle) throws FSException {
		_resourceHandleTable.get((int)fileHandle).flush();	
	}

	@Override
	public void link(String[] sourcePath, String[] targetPath)
			throws FSException {
		//Pass straight through
		_fs.link(sourcePath, targetPath);			
		
		//Make sure parent isn't cached.  If it is, force refresh
		GeniiCachedDir cachedParent = 
			(GeniiCachedDir)_cacheManager.getCacheItem(getParent(targetPath), false);		
		if(cachedParent != null) {
			try {				
				//Force refresh.  Easier this way o/w would have to find type etc
				cachedParent.refresh();				
			} catch(FSEntryAlreadyExistsException feaee){
				//Can't tell if it was created or not from here.  Pass it
			}
		}						
	}

	@Override
	public DirectoryHandle listDirectory(String[] path) throws FSException {
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);
		GeniiCachedDir cachedDir = (GeniiCachedDir)_cacheManager.getCacheItem(fullPath, true);		
		if(cachedDir == null) {			
			cachedDir = new GeniiCachedDir(path, null, false);
			_cacheManager.putCacheItem(fullPath, cachedDir);
		}
		return cachedDir.listDirectory();
	}

	@Override
	public void mkdir(String[] path, Permissions initialPermissions)
			throws FSException {
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);
		
		GeniiCachedDir cachedDir = (GeniiCachedDir)_cacheManager.getCacheItem(fullPath, false);		
		if(cachedDir != null) {			
			throw new FSEntryAlreadyExistsException(String.format(
					"Directory %s already exists.", fullPath));
		} else {
			cachedDir = new GeniiCachedDir(path, initialPermissions, true);
			_cacheManager.putCacheItem(fullPath, cachedDir);
			
			//Make sure parent isn't cached.  If it is, add to it
			GeniiCachedDir cachedParent = 
				(GeniiCachedDir)_cacheManager.getCacheItem(getParent(path), true);		
			if(cachedParent != null) {
				cachedParent.addEntry(path[path.length-1], new GeniiOpenDirHandle(path, cachedDir));
			}
		}		
	}

	@Override
	public long open(String[] path, OpenFlags flags, OpenModes mode,
			Permissions initialPermissions) throws FSException {
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);
		
		try {
			GeniiCachedFile cachedFile = (GeniiCachedFile)_cacheManager.getCacheItem(fullPath, true);
			if(cachedFile != null) {
				//If CREATE (should not be in cache)
				if (flags.isExclusive())
					throw new FSEntryAlreadyExistsException(String.format(
						"Path %s already exists.", fullPath));				
			} else {				
				cachedFile = new GeniiCachedFile(path, flags, mode, 
					initialPermissions);
			}	
			GeniiOpenFileHandle gofh = new GeniiOpenFileHandle(path, flags, mode);
			cachedFile.attach(gofh);
				
			if(flags.isCreate()) {
				//Make sure parent isn't cached.  If it is, add to it
				GeniiCachedDir cachedParent = 
					(GeniiCachedDir)_cacheManager.getCacheItem(getParent(path), true);		
				if(cachedParent != null) {
					try {
						cachedParent.addEntry(path[path.length-1], gofh);
					} catch(FSEntryAlreadyExistsException feaee){
						//Can't tell if it was created or not from here.  Pass it
					}
				}				
			}												
			return _resourceHandleTable.allocate(gofh);		
		}catch (ClassCastException ce) {
			throw new FSNotADirectoryException("Open called for file that is a directory");
		}
	}
	
	@Override
	public void read(long fileHandle, long offset, ByteBuffer target)
			throws FSException {
		_resourceHandleTable.get((int)fileHandle).read(offset, target);
	}

	@Override
	public void rename(String[] fromPath, String[] toPath) throws FSException {
		_fs.rename(fromPath, toPath);
		
		//Make sure parent isn't cached.  If it is, add to it
		GeniiCachedDir fromCachedParent = 
			(GeniiCachedDir)_cacheManager.getCacheItem(getParent(fromPath), false);		
		if(fromCachedParent != null) {				
			//Force refresh.  Easier this way o/w would have to find type etc
			fromCachedParent.refresh();							
		}	
		
		//Make sure parent isn't cached.  If it is, add to it
		GeniiCachedDir toCachedParent = 
			(GeniiCachedDir)_cacheManager.getCacheItem(getParent(toPath), false);		
		if(toCachedParent != null) {		
			//Force refresh.  Easier this way o/w would have to find type etc
			toCachedParent.refresh();				
		}			
	}

	@Override
	public FilesystemStatStructure stat(String[] path) throws FSException {
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);
		GeniiCachedResource cachedResource = _cacheManager.getCacheItem(fullPath, true);
		if(cachedResource == null) {
			RNSPath target = _fs.lookup(path);			
			
			if(target.exists()){
				try {
					TypeInformation ti = new TypeInformation(target.getEndpoint());				
					if(ti.isRNS()) {
						cachedResource = new GeniiCachedDir(path, null, false);						
					} else {
						OpenFlags flags = new OpenFlags(false, false, false, false);
						OpenModes mode = OpenModes.READ; 
						cachedResource = new GeniiCachedFile(path, flags, mode, null);
					}
					if(cachedResource != null) {
						_cacheManager.putCacheItem(fullPath, cachedResource);
					}
				} catch(RNSPathDoesNotExistException rnde) {
					//Nothing to do here
				}				
			}
		}
		if (cachedResource == null) {
			throw new FSEntryNotFoundException(String.format(
					"Couldn't find path %s.", fullPath));
		} else {
			return cachedResource.stat();
		}
	}

	@Override
	public void truncate(String[] path, long newSize) throws FSException {
		//Files need to be able to truncate!!!
		//_resourceHandleTable.get((int)fileHandle).write(offset, source);
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);
		GeniiCachedResource resource = _cacheManager.getCacheItem(fullPath, true);
		if(resource != null){
			if(resource.isDirectory()){
				throw new FSNotAFileException("Truncate received a directory instead of a file");
			} else {
				GeniiCachedFile file = (GeniiCachedFile) resource;
				file.truncate(newSize);
			}
		} else {
			OpenFlags flags = new OpenFlags(false, false, false, false);
			OpenModes mode = OpenModes.READ_WRITE; 
			GeniiCachedFile cachedFile = new GeniiCachedFile(path, flags, mode, null);
			cachedFile.truncate(newSize);
			_cacheManager.putCacheItem(fullPath, cachedFile);
		}				
	}

	@Override
	public void unlink(String[] path) throws FSException {
		//Finally call underlying fs
		_fs.unlink(path);
		
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);
		
		//Remove item from the cache.  Invalidate
		GeniiCachedResource resource = 
			_cacheManager.removeCacheItem(fullPath);
		
		if(null != resource) {
			resource.invalidate();
		}
		
		//Make sure parent isn't cached.  If it is, remove entry
		GeniiCachedDir cachedParent = 
			(GeniiCachedDir)_cacheManager.getCacheItem(getParent(path), false);		
		if(cachedParent != null) {
			cachedParent.refresh();
		}				
	}

	@Override
	public void updateTimes(String[] path, long accessTime,
			long modificationTime) throws FSException {
		String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
				path);
		GeniiCachedResource resource = _cacheManager.getCacheItem(fullPath, true);
		if(resource != null){			
			resource.updateTimes(accessTime, modificationTime);			
		} else {
			_fs.updateTimes(path, accessTime, modificationTime);
		}				
	}

	@Override
	public void write(long fileHandle, long offset, ByteBuffer source)
			throws FSException {
		_resourceHandleTable.get((int)fileHandle).write(offset, source);		
	}
	
	//Helper method to get the full path of the parent of this entry
	private String getParent(String[] path) {
		String[] parentPath = Arrays.copyOf(path, path.length-1);
		return UnixFilesystemPathRepresentation.INSTANCE.toString(
			parentPath);
	}	
}
