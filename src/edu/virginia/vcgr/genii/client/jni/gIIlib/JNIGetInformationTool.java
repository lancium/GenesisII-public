package edu.virginia.vcgr.genii.client.jni.gIIlib;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.JNICacheEntry;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.JNICacheManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

/* Gets the information related to a resource i.e. a Cache Entry */
public class JNIGetInformationTool extends JNILibraryBase {
	public static ArrayList getInformation(String path){		
		tryToInitialize();
		
		if(path.contains(":") || path.contains("desktop.ini")){
			if(JNILibraryBase.DEBUG)
				System.out.println("No Info for bogus file: " + path);
			return null;
		}
		
		path = (path == null || path.equals("") || path.equals("/")) ? "" : path;		
		
		JNICacheManager cacheManager = JNICacheManager.getInstance();		
		JNICacheEntry cacheEntry = cacheManager.getCacheEntry(path);
		
		//Cache Miss
		if(cacheEntry == null){			
			//Let's try to get this data
			try{
				String parent = null;
				//Try to get whole parent folder of information in										
				int slashIdx = path.lastIndexOf('/');
				if(slashIdx != -1 && slashIdx != 0){
					parent = path.substring(0, slashIdx);
				}
				if(parent != null){		
					ArrayList parentEntries;
					//Should fill cache here with parent DL
					parentEntries = 
						JNIDirectoryListingTool.getDirectoryListing(parent, "");

					//Crazy logic, parent has no items so this path does not exist
					if(parentEntries == null){
						cacheEntry = JNICacheEntry.createNonExistingEntry(path);
						cacheManager.putCacheEntry(path, cacheEntry);
					}
					else{						
						//Should be valid now
						cacheEntry = cacheManager.getCacheEntry(path);
					}
				}		
				else{
					//No parent
					boolean isDirectory = false;
					long fileSize;
					RNSPath current = RNSPath.getCurrent();			
					RNSPath filePath = current.lookup(path, RNSPathQueryFlags.MUST_EXIST);
					String name;
					
					//Fill in directory information
					if(filePath.isDirectory()){ 
						isDirectory = true;
						fileSize = -1;
					}
					else{
						TypeInformation type = new TypeInformation(
								filePath.getEndpoint());						
						fileSize = type.getByteIOSize();																		
					}
					name = filePath.getName();
					
					cacheEntry = new JNICacheEntry(path, isDirectory, fileSize, name, null);
					
					//Add it to the cache!
					cacheManager.putCacheEntry(path, cacheEntry);
				}
						
				
			}catch(Exception e){
				if(JNILibraryBase.DEBUG){
					System.out.println("JNILibary:  Could not find file " + path);
				}			
			}								
		}		
		if(cacheEntry == null){
			//Put into cache that this path does not exist
			cacheEntry = JNICacheEntry.createNonExistingEntry(path);
			cacheManager.putCacheEntry(path, cacheEntry);
		}
		
		//We cache non existent resources to speed up lookups
		if(!cacheEntry.exists()){
			return null;
		}
		else{
			return cacheEntry.getFileInformation();
		}
	}
}

//OLD CODE
////Last chance (if we still couldn't get it)
//if(parent!= null && cacheEntry == null){				
//	boolean isDirectory = false;
//	long fileSize;
//	RNSPath current = RNSPath.getCurrent();			
//	RNSPath filePath = current.lookup(path, RNSPathQueryFlags.MUST_EXIST);
//	String name;
//	
//	//Fill in directory information
//	if(filePath.isDirectory()){ 
//		isDirectory = true;
//		fileSize = 0;
//	}
//	else{
//		TypeInformation type = new TypeInformation(
//				filePath.getEndpoint());						
//		fileSize = type.getByteIOSize();																		
//	}
//	name = filePath.getName();
//	
//	cacheEntry = new JNICacheEntry(path, isDirectory, fileSize, name, null);
//	
//	//Add it to the cache!
//	cacheManager.putCacheEntry(path, cacheEntry);
//}