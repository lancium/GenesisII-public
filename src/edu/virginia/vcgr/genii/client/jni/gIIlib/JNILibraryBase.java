package edu.virginia.vcgr.genii.client.jni.gIIlib;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public abstract class JNILibraryBase extends ApplicationBase {

	public static boolean isInitialized = false;
	public static final boolean DEBUG = true;
	
	synchronized public static void tryToInitialize(){
		if(!isInitialized){
			initialize();
		}
		ICallingContext callingContext;
		try {
			callingContext = ContextManager.getCurrentContext(false);
			ClientUtils.checkAndRenewCredentials(callingContext);
		} catch (Exception e) {
			
			System.out.println("JNILibraryError:  Problem with relogin");
		}		
	}
	
	public static void initialize(){
		try{
			prepareClientApplication();
			isInitialized = true;
		}catch(RuntimeException e){
			System.out.println("Application already started");
		}
	}
	
	/** 
	 * Cleans up paths so that they are all  consistent (important for Caching)
	 */
	public static String cleanupPath(String path){
		String newPath = path;
		
		//All root directory pointers are '/'
		if(path == null || path.equals("") || path.equals("/")){
			newPath = "/";
		}
		
		//All paths are absolute
		if(!newPath.startsWith("/")){
			newPath = "/" + newPath;
		}
		
		//No paths end with '/'
		if(newPath.length() > 1 && newPath.endsWith("/")){
			newPath = newPath.substring(0, newPath.lastIndexOf('/'));
		}
		
		return newPath;				
	}
	
	/**
	 * Checks for valid paths (filters out commonly unused names)
	 * @param path
	 * @return
	 */
	public static boolean isValidPath(String path){
		if(path != null){
			if(path.contains(":") || 
					path.matches(".*[Dd][Ee][Ss][Kk][Tt][Oo][Pp].[Ii][Nn][Ii]")){
				if(JNILibraryBase.DEBUG)
					System.out.println("GENESIS:  Path filtered out: " + path);
				return false;
			}else if (path.endsWith("Thumbs.db")){
				return false;
			}
		}
		return true;
	}
}
