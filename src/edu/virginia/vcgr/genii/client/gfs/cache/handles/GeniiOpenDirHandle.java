package edu.virginia.vcgr.genii.client.gfs.cache.handles;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.genii.client.gfs.cache.GeniiCacheManager;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedDir;
import edu.virginia.vcgr.genii.client.gfs.cache.objects.GeniiCachedResource;


public class GeniiOpenDirHandle extends GeniiOpenHandle<GeniiCachedDir> {
	
	static private Log _logger = LogFactory.getLog(GeniiOpenDirHandle.class);
	
	public GeniiOpenDirHandle(String[] path) {
		_path = path;
	}
	public GeniiOpenDirHandle(String[] path, GeniiCachedDir cachedDir) {
		_path = path;
		_cacheObject = cachedDir;
	}
	
	@Override
	public synchronized FilesystemStatStructure stat() throws FSException {
		if(_cacheObject == null || !_cacheObject.isValid()) {
			GeniiCacheManager manager = GeniiCacheManager.getInstance();
			String fullPath = UnixFilesystemPathRepresentation.INSTANCE.toString(
					_path);
			synchronized(manager) {
				//Do not refresh on a stat.  Waste of time for directories
				GeniiCachedResource fromCache = manager.getCacheItem(fullPath, false);				
				if(fromCache == null) {
					_cacheObject = new GeniiCachedDir(_path, null, false);
					manager.putCacheItem(fullPath, _cacheObject);
				} else {
					_logger.debug(String.format("Attaching to cache object found for "
							+ "%s", fullPath));
					_cacheObject = (GeniiCachedDir)fromCache;
				}
			}
		}
		return _cacheObject.stat();
	}

	@Override
	public boolean isDirectory() {		
		return true;
	}
}
