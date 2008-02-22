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
}
