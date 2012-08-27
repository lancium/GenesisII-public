package edu.virginia.vcgr.genii.client.jni.giilibmirror.io.handles;



import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.jni.giilibmirror.ResourceInformation;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.cache.CacheManager;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.cache.CachedDir;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.cache.CachedFile;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.cache.CachedResource;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class WindowsFileHandle extends WindowsResourceHandle {	
	
	CachedFile fileInCache;
    static private Log _logger = LogFactory.getLog(WindowsFileHandle.class);
	
	public WindowsFileHandle(){
		fileHandle = INVALID_HANDLE;
	}
	
	public WindowsFileHandle(String fileName, Integer requestedDeposition, 
			Integer desiredAccess) throws Exception {
		
		super(fileName, requestedDeposition, desiredAccess);
	}
	
	public WindowsFileHandle makeCopy(){
		WindowsFileHandle wfh = new WindowsFileHandle();
		wfh.fileInCache = this.fileInCache;
		try{
			fileInCache.attach(wfh, WindowsResourceHandle.INFORMATION_ONLY, false);
		}
		catch(Exception e){
			System.err.println("G-ICING:  Failure to attach on clone of " + 
					fileInCache.getRNSPath().pwd());
		}
		return wfh;
	}
	
	/** 
	 * Opens a file with given access and filePath (i.e. deposition == OPEN) 
	 * **/
	public WindowsFileHandle(RNSPath filePath, Integer requestedDeposition, Integer desiredAccess,
			CachedDir cachedParent) 
		throws RNSException, IOException {
		
		super();
		
		String fileName = filePath.pwd();
		CacheManager manager = CacheManager.getInstance();
		CachedResource resource = manager.getResource(fileName, true);
		if(resource != null && resource.isDirectory()){
			throw new RNSException("Directory exists where file is desired to be opened");
		}
		
		//Cast to the correct type
		CachedFile cachedFile = null;
		if(resource != null){
			cachedFile = (CachedFile)resource;
		}		
		
		//For case supersede (assume not in cache - only time we would use it)
		switch(requestedDeposition)
		{
			case SUPERSEDE:				
				//Remove from parent
				if(cachedParent != null) cachedParent.removeEntry(fileName);
				
				//Invalidate Cache (really should have anything here) i.e Safety Check
				if(cachedFile != null){
					//Shouldn't have any (so warn if any)
					filePath = cachedFile.getRNSPath();
					cachedFile.invalidate(true);					
				}								
				
				if(filePath.exists()){
					filePath.delete();						
				}							
				filePath.createNewFile();												
				fileInCache = new CachedFile(filePath, desiredAccess, false, true);
				fileInCache.attach(this, desiredAccess, false);
				
				//Add back to parent
				if(cachedParent != null) cachedParent.addEntry(filePath.getName(), makeCopy());
				
				manager.putResource(fileName, fileInCache);
				break;
			case OPEN:
			case OVERWRITE:								
				if(cachedFile != null){
					cachedFile.attach(this, desiredAccess, false);
					fileInCache = cachedFile;
				}else{
					fileInCache = new CachedFile(filePath, desiredAccess, 
							(requestedDeposition == OVERWRITE), true);
					fileInCache.attach(this, desiredAccess, false);
					manager.putResource(fileName, fileInCache);					
				}												
				break;			
		}																
	}
	
	/** 
	 * Opens a file for a Non-ByteIO 
	 * **/
	private WindowsFileHandle(RNSPath filePath) 
		throws RNSException, IOException {
		
		super();
		
		String fileName = filePath.pwd();
		CacheManager manager = CacheManager.getInstance();
		CachedResource resource = manager.getResource(fileName, true);
		if(resource != null && resource.isDirectory()){
			throw new RNSException("Directory exists where file is desired to be opened");
		}
		
		//Cast to the correct type
		CachedFile cachedFile = null;
		if(resource != null){
			cachedFile = (CachedFile)resource;
		}										
		if(cachedFile != null){
			cachedFile.attach(this, WindowsResourceHandle.INFORMATION_ONLY, false);
			fileInCache = cachedFile;
		}else{
			fileInCache = new CachedFile(filePath, WindowsResourceHandle.INFORMATION_ONLY, 
					false, false);
			fileInCache.attach(this, WindowsResourceHandle.INFORMATION_ONLY, false);
			manager.putResource(fileName, fileInCache);					
		}																													
	}
	
	public static WindowsFileHandle createNonByteIOFileHandle(RNSPath filePath) 
			throws RNSException, IOException {
		return new WindowsFileHandle(filePath);
	}
				
	void finishOpen(String resourceName, Integer requestedDeposition, Integer desiredAccess, CachedResource cachedResource,  
				CachedDir cachedParent, RNSPath searchRoot, String pathToLookup) throws Exception{
		
		boolean truncate = (requestedDeposition & OVERWRITE) > 0;
		RNSPath filePath = null;
		
		CacheManager manager = CacheManager.getInstance();
		
		//Cast to the correct type
		CachedFile cachedFile = null;
		if(cachedResource != null){
			cachedFile = (CachedFile)cachedResource;
		}
					
		//Check the deposition and attempt RNS stuff accordingly
		switch(requestedDeposition){
			case SUPERSEDE:
				
				//Invalidate Cache (really should have anything here) i.e Safety Check
				if(cachedFile != null){
					//Shouldn't have any (so warn if any)
					filePath = cachedFile.getRNSPath();
					cachedFile.invalidate(true);					
				}
				else{				
					filePath =  searchRoot.lookup(pathToLookup, RNSPathQueryFlags.DONT_CARE);
				}
				
				if(filePath.exists()){
					filePath.delete();						
				}				
				
				//Should just go straight into the CREATE (no break)
			case CREATE:

				if(cachedFile != null){
					filePath = cachedFile.getRNSPath();
				}else{				
					filePath = searchRoot.lookup(pathToLookup, RNSPathQueryFlags.MUST_NOT_EXIST);
				}
				
				filePath.createNewFile();												
				fileInCache = new CachedFile(filePath, desiredAccess, truncate, true);
				fileInCache.attach(this, desiredAccess, false);
				if(cachedParent != null) cachedParent.addEntry(filePath.getName(), makeCopy());
				
				manager.putResource(resourceName, fileInCache);
				break;					
			case OPEN:
			case OVERWRITE:				
				
				if(cachedFile != null){									
					fileInCache = cachedFile;
					fileInCache.attach(this, desiredAccess, truncate);
				}else{
					filePath = searchRoot.lookup(pathToLookup, RNSPathQueryFlags.MUST_EXIST);
					if(!(new TypeInformation(filePath.getEndpoint()).isRNS())){
						fileInCache = new CachedFile(filePath, desiredAccess, truncate, true);
						fileInCache.attach(this, desiredAccess, false);
						manager.putResource(resourceName, fileInCache);
					}else{
						throw new RNSPathDoesNotExistException("Directory exists where file is requewsted");
					}
				}												
				break;
			case OPEN_IF:
			case OVERWRITE_IF:
				
				if(cachedFile != null){					
					fileInCache = cachedFile;
					fileInCache.attach(this, desiredAccess, truncate);
				}else{
					boolean created = false;
					filePath = searchRoot.lookup(pathToLookup, RNSPathQueryFlags.DONT_CARE);
					if(!filePath.exists()){
						filePath.createNewFile();
						created = true;
					}
										
					fileInCache = new CachedFile(filePath, desiredAccess, truncate, true);
					fileInCache.attach(this, desiredAccess, false);
					
					if(created && cachedParent != null) 
						cachedParent.addEntry(filePath.getName(), makeCopy());
					manager.putResource(resourceName, fileInCache);
				}					
				
				break;
			default:
				throw new RNSException("Unknown type");			
		}		
	}
	
	public void invalidate(){
		fileInCache = null;
	}

	@Override
	public ResourceInformation getCachedInformation() {
		return fileInCache.getCachedInformation(fileHandle);
	}
	
	public byte[] read(Long offset, Integer length){
		return fileInCache.read(offset, length);				
	}
	
	public int write(byte[] data, Long offset){
		return fileInCache.write(data, offset);				
	}
	
	public int truncateAppend(byte[] data, Long offset){
		return fileInCache.truncateAppend(data, offset);				
	}
	
	public boolean rename(String newPath){
		RNSPath oldRNSPath = fileInCache.getRNSPath();
		
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
			
			fileInCache.setRNSPath(newRNSPath);
			
			//Change to new object in cache
			manager.putResource(newPath, fileInCache);					
			manager.putResource(oldRNSPath.pwd(), null);
			
			//Unlink to finish the move
			oldRNSPath.unlink();
		}catch(Exception e){
			_logger.info("exception occurred in rename", e);
			return false;
		}		
		return true;
	}

	@Override
	public void close(boolean deleteOnClose) {
		String filePath = fileInCache.getRNSPath().pwd();
		String filename = fileInCache.getRNSPath().getName();
		System.out.println("G-ICING:  Close received for "
				+ filename + " deleteOnClose: " + deleteOnClose);
		if(deleteOnClose){
			try{
				fileInCache.getRNSPath().delete();
				fileInCache.invalidate(false);				
				CachedDir cachedParent = getCachedParent(filePath);
				if(cachedParent != null){
					cachedParent.removeEntry(filename);
				}
			}catch(RNSException re){
				_logger.info("exception occurred in close", re);
			}
		}else{
			fileInCache.detatch(this);
		}
	}
}
