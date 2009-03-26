package edu.virginia.vcgr.genii.client.jni.giilibmirror;

import java.util.Date;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;

public abstract class JNILibraryBase extends ApplicationBase {

	public static boolean isInitialized = false;
	public static final boolean DEBUG = true;
	
	// Members for the mirroring test harness
	
	/** 
	 * If this is set to true, filesystem calls to the redirector
	 * will be mirrored to the folder specified by testRoot.
	 * e.g. C:\TestRoot will be the Root of the RNS space
	 */
	public static boolean ENABLE_LOCAL_TEST = true;
	
	/** 
	 * Easy default value for testRoot
	 */
	public static final String DEFAULT_ROOT = "C:/TestRoot";
	
	/**
	 * Folder for test harness
	 */
	public static String testRoot = DEFAULT_ROOT;
	
	synchronized public static void tryToInitialize(){
		if(!ENABLE_LOCAL_TEST) {			
			if(!isInitialized){
				initialize();
			}
			ICallingContext callingContext;
			try {
				callingContext = ContextManager.getCurrentContext(false);				
				ClientUtils.checkAndRenewCredentials(callingContext, new Date(), null);
			} catch (Exception e) {
				
				System.out.println("JNILibraryError:  Problem with relogin");
			}
		} else {
			if(!isInitialized) {
				System.out.println("GenesisServer: Starting local redirecting to " +
						DEFAULT_ROOT);
			}
			isInitialized = true;
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
