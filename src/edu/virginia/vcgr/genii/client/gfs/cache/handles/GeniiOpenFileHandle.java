package edu.virginia.vcgr.genii.client.gfs.cache.handles;



import java.nio.ByteBuffer;

import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.genii.client.gfs.cache.GeniiCacheGenericFileObject;
import edu.virginia.vcgr.genii.client.gfs.cache.GeniiCacheManager;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedFile;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedResource;

public class GeniiOpenFileHandle extends GeniiOpenHandle<GeniiCachedFile> implements GeniiCacheGenericFileObject{			
	OpenFlags _flags;
	OpenModes _mode;
	//If my file handle caused writes
	boolean dirty = false;
	
	public GeniiOpenFileHandle(String[] path, OpenFlags flags, OpenModes modes) {
		_path = path;
		_flags = flags;
		_mode = modes;
	}					
	
	public void attach(GeniiCachedFile cachedFile){
		_cacheObject = cachedFile;
	}
	
	public FilesystemStatStructure stat() throws FSException {
		if(_cacheObject == null || !_cacheObject.isValid()) {
			GeniiCacheManager manager = GeniiCacheManager.getInstance();
			String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
					_path);
			synchronized(manager) {				
				GeniiCachedResource fromCache = manager.getCacheItem(fullPath, true);
				if(fromCache == null) {
					OpenFlags flags = new OpenFlags(false, false, false, false);
					OpenModes mode = OpenModes.READ; 
					_cacheObject = new GeniiCachedFile(_path, flags, mode, null);
					manager.putCacheItem(fullPath, _cacheObject);
				} else {
					_cacheObject = (GeniiCachedFile)fromCache;
				}
			}
		}
		return _cacheObject.stat();
	}		

	@Override
	public void close() throws FSException {
		if(null != _cacheObject) {
			flush();		
			_cacheObject.detatch(this);
		}
	}

	@Override
	public synchronized void flush() throws FSException {
		if(dirty){
			_cacheObject.flush();
			dirty = false;
		}
	}

	@Override
	public void read(long offset, ByteBuffer target) throws FSException {
		_cacheObject.read(offset, target);
	}

	@Override
	public synchronized void write(long offset, ByteBuffer source) throws FSException {
		dirty = true;
		_cacheObject.write(offset, source);		
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	public OpenModes getMode() {
		return _mode;
	}

	public OpenFlags getFlags() {
		return _flags;
	}

	@Override
	public synchronized void truncate(long newSize) throws FSException {
		dirty = true;
		_cacheObject.truncate(newSize);		
	}
}
