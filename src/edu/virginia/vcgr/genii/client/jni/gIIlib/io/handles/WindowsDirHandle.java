package edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles;

import java.io.IOException;
import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.jni.gIIlib.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CachedDir;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.CachedResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class WindowsDirHandle extends WindowsResourceHandle {

	CachedDir dirInCache;
	
	private WindowsDirHandle(){
		fileHandle = INVALID_HANDLE;
	}
	
	public WindowsDirHandle(String fileName, Integer requestedDeposition, 
			Integer desiredAccess) throws Exception {
		
		super(fileName, requestedDeposition, desiredAccess);
	}
	
	public WindowsDirHandle makeCopy(){
		WindowsDirHandle wdh = new WindowsDirHandle();
		wdh.dirInCache = this.dirInCache;
		dirInCache.attach(wdh);
		return wdh;
	}
	
	/** 
	 * Opens a file with given access and filePath (i.e. deposition == OPEN) 
	 * **/
	public WindowsDirHandle(RNSPath filePath, Integer desiredAccess, boolean refreshDirectory) 
		throws IOException, RNSException {
		
		super();
		
		String fileName = filePath.pwd();
		CacheManager manager = CacheManager.getInstance();
		CachedResource resource = manager.getResource(fileName, refreshDirectory);
		if(resource != null && !resource.isDirectory()){
			throw new RNSException("Directory exists where file is desired to be opened");
		}
		
		//Cast to the correct type
		CachedDir cachedDir = null;
		if(resource != null){
			cachedDir = (CachedDir)resource;
		}				
				
		if(cachedDir != null){			
			dirInCache = cachedDir;			
		}else{			
			dirInCache = new CachedDir(filePath, desiredAccess);
			manager.putResource(fileName, dirInCache);
		}							
		dirInCache.attach(this);
	}
		
	void finishOpen(String resourceName, Integer requestedDeposition, Integer desiredAccess, CachedResource cachedResource,  
		CachedDir cachedParent, RNSPath searchRoot, String pathToLookup) throws Exception{
				
		RNSPath filePath = null;
		
		CacheManager manager = CacheManager.getInstance();
		
		//Cast to the correct type
		CachedDir cachedDir = null;
		if(cachedResource != null){
			cachedDir = (CachedDir)cachedResource;
		}		
		
		//Check the deposition and attempt RNS stuff accordingly
		switch(requestedDeposition){
			case CREATE:
				
				if(cachedDir != null){
					filePath = cachedDir.getRNSPath();
				}else{				
					filePath = searchRoot.lookup(pathToLookup, RNSPathQueryFlags.MUST_NOT_EXIST);
				}
				
				filePath.mkdir();														
				
				dirInCache = new CachedDir(filePath, desiredAccess);				
				
				if(cachedParent != null) cachedParent.addEntry(filePath.getName(), makeCopy());
				
				manager.putResource(resourceName, dirInCache);
				break;					
			case OPEN:											
				
				if(cachedDir != null){					
					dirInCache = cachedDir;					
				}else{
					filePath = searchRoot.lookup(pathToLookup, RNSPathQueryFlags.MUST_EXIST);
					dirInCache = new CachedDir(filePath, desiredAccess);
					manager.putResource(resourceName, dirInCache);
				}												
				break;
			case OPEN_IF:							
								
				if(cachedDir != null){					
					dirInCache = cachedDir;
				}else{
					boolean created = false;
					filePath = searchRoot.lookup(pathToLookup, RNSPathQueryFlags.DONT_CARE);
					if(!filePath.exists()){
						filePath.mkdir();	
						created = true;
					}
					dirInCache = new CachedDir(filePath, desiredAccess);
					if(created && cachedParent != null)  
						cachedParent.addEntry(filePath.getName(), makeCopy());
					manager.putResource(resourceName, dirInCache);
				}														
				break;
			default:
				throw new RNSException("Unknown type or incorrect desposition for open dir");
		}
		//Actually do attach at the end
		dirInCache.attach(this);
	}
	
	public void invalidate(){
		dirInCache = null;
	}

	@Override
	public ResourceInformation getCachedInformation() {
		return dirInCache.getCachedInformation(fileHandle);
	}
	
	public ArrayList<ResourceInformation> getEntries(String target){
		return dirInCache.getEntries(target);
	}
	
	public boolean rename(String newPath){
						
		RNSPath oldRNSPath = dirInCache.getRNSPath();
		
		CacheManager manager = CacheManager.getInstance();
		
		/* Try to shortcut for search of new RNSPath */
		
		try{
			//Also get name of the file
			String tailName = newPath;
			if(!newPath.equals("/") && newPath.lastIndexOf('/') > 0){														
				tailName = newPath.substring(newPath.lastIndexOf('/') + 1);
			}
			
			CachedDir newPathsParent = getCachedParent(newPath);
	
			//Use RNSPath of 
			RNSPath current = newPathsParent != null ? newPathsParent.getRNSPath() : 
				RNSPath.getCurrent();
			String pathToLookup = newPathsParent != null ? tailName : newPath;
			
			RNSPath newRNSPath = current.lookup(pathToLookup, RNSPathQueryFlags.MUST_NOT_EXIST);
					
			/* Finish Shortcut */
			
			newRNSPath.link(oldRNSPath.getEndpoint());
			
			dirInCache.setRNSPath(newRNSPath);
			
			//Change to new object in cache
			manager.putResource(newPath, dirInCache);					
			manager.putResource(oldRNSPath.pwd(), null);
			
			//Unlink to finish the move
			oldRNSPath.unlink();
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}		
		return true;
	}

	@Override
	public void close(boolean deleteOnClose) {
		String filePath = dirInCache.getRNSPath().pwd();
		String filename = dirInCache.getRNSPath().getName();
		if(deleteOnClose){
			try{
				dirInCache.getRNSPath().delete();
				dirInCache.invalidate(false);				
				CachedDir cachedParent = getCachedParent(filePath);
				if(cachedParent != null){
					cachedParent.removeEntry(filename);
				}
			}catch(RNSException re){
				re.printStackTrace();
			}
		}else{
			dirInCache.detatch(this);
		}
	}
}
