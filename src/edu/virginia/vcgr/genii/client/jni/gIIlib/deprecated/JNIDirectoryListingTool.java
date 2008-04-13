package edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNIDirectoryListingTool extends JNILibraryBase
{
	public static ArrayList<String> getDirectoryListing(String directory, String target) {
		tryToInitialize();
							
		JNICacheManager manager = JNICacheManager.getInstance();
		JNICacheEntry forDirectory; 
		JNICacheEntry toAdd = null;
		ArrayList <JNICacheEntry> cacheEntries = null;
		ArrayList<String> directoryListing = new ArrayList<String>();		
		
		//If target is null then query current directory
		//All paths are absolute (cleanup)
		directory = (directory != null && !directory.equals("") && !directory.equals("/")) 
			? directory : "/";		
		directory = (directory.length() > 0 && !directory.startsWith("/")) ? 
				"/" + directory : directory;
		
		//Default .* behavior or replace all * with .*
		target = ((target=="" || target == null) ? ".*" : target.replace("*", ".*")); 
		
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
		
		//Not in the cache (parent might be found)
		if(cacheEntries == null)
		{		
			//We have to do it the hard way :-(						
			cacheEntries = new ArrayList<JNICacheEntry>();			
					
			//Mostly borrowed from LSTool
			try
			{											
				ICallingContext ctxt = ContextManager.getCurrentContext();													
	
				RNSPath path = ctxt.getCurrentPath().lookup(directory, 
						RNSPathQueryFlags.MUST_EXIST);												
					
				//ALWAYS get all entries
				RNSPath []entries = path.list(".*", RNSPathQueryFlags.DONT_CARE);
		
				if (entries.length > 1 || entries[0].exists())
				{
					for (RNSPath entry : entries)
					{
						if(entry.isDirectory()){														
							toAdd = new JNICacheEntry(entry.pwd(), 
									true, -1, entry.getName(), null);													
							
							//Add to cache and then to return listing
							manager.putCacheEntry(entry.pwd(), toAdd);
							cacheEntries.add(toAdd);
						}
						else{
							TypeInformation type = new TypeInformation(
									entry.getEndpoint());							
							
							toAdd = new JNICacheEntry(entry.pwd(), 
									false, type.getByteIOSize(), entry.getName(), null);
							
							//Add to cache and then to return listing
							manager.putCacheEntry(entry.pwd(), toAdd);
							cacheEntries.add(toAdd);																				
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
		
		//Match against the target		
		for(JNICacheEntry entry : cacheEntries){
			if(entry.getName().matches(target)){
				directoryListing.add(String.valueOf(-1));
				directoryListing.addAll(entry.getFileInformation());
			}
		}					
		
		return directoryListing;
	}
}