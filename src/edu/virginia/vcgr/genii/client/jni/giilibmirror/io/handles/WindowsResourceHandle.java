package edu.virginia.vcgr.genii.client.jni.giilibmirror.io.handles;

import edu.virginia.vcgr.genii.client.jni.giilibmirror.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.cache.CachedDir;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.cache.CachedResource;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public abstract class WindowsResourceHandle {
	/* Requested deposition */
	public final static int SUPERSEDE = 0; //Delete + Create
	public final static int OPEN = 1;
	public final static int CREATE = 2;
	public final static int OPEN_IF = 3;
	public final static int OVERWRITE = 4; // Truncate
	public final static int OVERWRITE_IF = 5;
	
	/* Desired Access */
	public final static int INFORMATION_ONLY = 0;
	public final static int FILE_READ_DATA = 1;
	public final static int FILE_WRITE_DATA = 2;
	public final static int FILE_APPEND_DATA = 4;
	public final static int FILE_EXECUTE = 8; //Don't handle
	public final static int DELETE = 16;	//Don't handle
	
	public static final int INVALID_HANDLE = -1;
	
	public int fileHandle;
	private static int nextFileHandle=0;
	
	public WindowsResourceHandle() {
		fileHandle = INVALID_HANDLE;
	}
	
	/**
	 * General open for file and directory handles
	 * @param fileName
	 * @param requestedDeposition
	 * @param desiredAccess
	 * @throws Exception
	 */
	public WindowsResourceHandle(String fileName, Integer requestedDeposition, 
			Integer desiredAccess) throws Exception {
		
		//Obtain file from the cache first
		CacheManager manager = CacheManager.getInstance();
		CachedResource resource = manager.getResource(fileName, true);		
		
		//Also get name of the file
		String tailName = fileName;
		if(!fileName.equals("/") && fileName.lastIndexOf('/') > 0){														
			tailName = fileName.substring(fileName.lastIndexOf('/') + 1);
		}
		
		CachedDir cachedParent = getCachedParent(fileName);
		
		//Use RNSPath of 
		RNSPath current = cachedParent != null ? cachedParent.getRNSPath() : 
			RNSPath.getCurrent();
		String pathToLookup = cachedParent != null ? tailName : fileName;
		
		fileHandle = nextHandle();
		
		finishOpen(fileName, requestedDeposition, desiredAccess, resource, 
				cachedParent, current, pathToLookup);
	}
	
	public static WindowsResourceHandle openResource(String fileName, Integer requestedDeposition, 
			Integer desiredAccess) throws Exception {
		
		//Obtain file from the cache first
		CacheManager manager = CacheManager.getInstance();
		CachedResource resource = manager.getResource(fileName, true);
		
		//Automatically use this type
		if(resource != null)
			if(resource.isDirectory()){
				return new WindowsDirHandle(fileName, requestedDeposition, desiredAccess);
			}
			else{
				return new WindowsFileHandle(fileName, requestedDeposition, desiredAccess);
		}
		
		//Nothing in the cache for this yet
		
		//Get name of the file
		String tailName = fileName;
		if(!fileName.equals("/") && fileName.lastIndexOf('/') > 0){														
			tailName = fileName.substring(fileName.lastIndexOf('/') + 1);
		}
		
		CachedDir cachedParent = getCachedParent(fileName);
		
		//Use RNSPath of 
		RNSPath current = cachedParent != null ? cachedParent.getRNSPath() : 
			RNSPath.getCurrent();
		String pathToLookup = cachedParent != null ? tailName : fileName;
		
		RNSPath filePath =  current.lookup(pathToLookup, RNSPathQueryFlags.DONT_CARE);
		
		//Save some time and use "Special" constructors
		if(new TypeInformation(filePath.getEndpoint()).isRNS()){
			return new WindowsDirHandle(filePath, desiredAccess, true);
		}else{
			return new WindowsFileHandle(filePath, requestedDeposition, 
					desiredAccess, cachedParent);
		}
	}
	
	
	
	/**
	 * Performs the Directory / File specific open for subclasses
	 * @param resourceName
	 * @param requestedDeposition
	 * @param desiredAccess
	 * @param cachedResource
	 * @param cachedParent
	 * @param searchRoot
	 * @param pathToLookup
	 * @throws Exception
	 */
	abstract void finishOpen(String resourceName, Integer requestedDeposition, Integer desiredAccess, CachedResource cachedResource, 
			CachedDir cachedParent, RNSPath searchRoot, String pathToLookup) throws Exception;
		
	/** 
	 * Gets information about the resource
	 */
	public abstract ResourceInformation getCachedInformation();
	
	public abstract void close(boolean deleteOnClose);
	
	/**
	 * Returns the cache entry for the parent of the given path
	 * @param childPath
	 * @return cached directory for the given child
	 */
	public static CachedDir getCachedParent(String childPath){
		//Get parent String 
		String parent = null;
		CacheManager manager = CacheManager.getInstance();
				
		if(!childPath.equals("/") && childPath.lastIndexOf('/') > 0){									
			parent = childPath.substring(0, childPath.lastIndexOf('/'));					
		}
		if(!childPath.equals("/") && childPath.lastIndexOf('/') == 0){
			parent = "/";			
		}
		
		//Get cache entry for the parent
		CachedDir cachedParent = null;
		if(parent != null){
			//I only need RNSPath (no need to propagate new entries
			CachedResource parentResource = manager.getResource(parent, false);
			if(parentResource != null){
				cachedParent = parentResource.isDirectory() ? (CachedDir) parentResource :
					null;
			}
		}	
		
		return cachedParent;
	}
	static public synchronized int nextHandle(){
		int lastFileHandle = nextFileHandle;
		nextFileHandle++;
		return lastFileHandle;			
	}	
}
