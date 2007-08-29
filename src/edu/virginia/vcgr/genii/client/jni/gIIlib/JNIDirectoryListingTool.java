package edu.virginia.vcgr.genii.client.jni.gIIlib;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.JNICacheEntry;
import edu.virginia.vcgr.genii.client.jni.gIIlib.cache.JNICacheManager;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNIDirectoryListingTool extends JNILibraryBase
{
	public static ArrayList getDirectoryListing(String directory, String target) {
		tryToInitialize();
		
		
		JNICacheManager manager = JNICacheManager.getInstance();
		JNICacheEntry forDirectory; 
		JNICacheEntry toAdd = null;
		ArrayList <JNICacheEntry> cacheEntries = null;
		ArrayList<String> directoryListing = new ArrayList<String>();
		
		//If target is null then query current directory		
		directory = (directory != null && !directory.equals("") && !directory.equals("/")) 
			? directory + '/' : "";		
		
		//Check cache first
		forDirectory = manager.getCacheEntry(directory);
		if(forDirectory != null){
			if(!forDirectory.exists()){
				return null;
			}
			else{
				cacheEntries = forDirectory.getDirectoryEntries();
			}
		}
		
		//Check to see if entries are in the cache yet
		if(cacheEntries != null){
			for(JNICacheEntry entry : cacheEntries){
				directoryListing.addAll(entry.getFileInformation());
			}			
		}
		else{
		
			//We have to do it the hard way :-(						
			cacheEntries = new ArrayList<JNICacheEntry>();
			
			target = target == null ? "." : target;
					
			try
			{											
				ICallingContext ctxt = ContextManager.getCurrentContext();													
	
				RNSPath path = ctxt.getCurrentPath().lookup((directory + 
						target), RNSPathQueryFlags.MUST_EXIST);												
					
				RNSPath []entries = path.list(".*", RNSPathQueryFlags.DONT_CARE);
		
				if (entries.length > 1 || entries[0].exists())
				{
					for (RNSPath entry : entries)
					{
						if(entry.isDirectory()){
							String entryPath = directory + "/" + entry.getName();
							toAdd = new JNICacheEntry(entryPath, 
									true, -1, entry.getName(), null);
							
							//Add to cache and then to return listing
							manager.putCacheEntry(entryPath, toAdd);
							cacheEntries.add(toAdd);
							directoryListing.addAll(toAdd.getFileInformation());
						}
						else{
							TypeInformation type = new TypeInformation(
									entry.getEndpoint());
							String entryPath = directory + "/" + entry.getName();
							
							toAdd = new JNICacheEntry(entryPath, 
									false, type.getByteIOSize(), entry.getName(), null);
							
							//Add to cache and then to return listing
							manager.putCacheEntry(entryPath, toAdd);
							cacheEntries.add(toAdd);
							directoryListing.addAll(toAdd.getFileInformation());																					
						}
					}
				}												
				
				//Add the parent to the cache (adds size with query directory)
				toAdd = new JNICacheEntry(directory, true, cacheEntries.size(), path.getName(), cacheEntries);
				manager.putCacheEntry(directory, toAdd);				
			}
			catch (Exception t)
			{
				if(JNILibraryBase.DEBUG){
					t.printStackTrace();
				}
				else{
					//Print out nicer debug information
					System.out.println("JNILibrary:  Listing for directory " + directory + 
							" failed");
				}
				toAdd = JNICacheEntry.createNonExistingEntry(directory);
				manager.putCacheEntry(directory, toAdd);
				directoryListing = null;
			}
		}		
		return directoryListing;
	}
}