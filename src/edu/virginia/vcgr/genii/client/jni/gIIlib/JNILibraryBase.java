package edu.virginia.vcgr.genii.client.jni.gIIlib;

import edu.virginia.vcgr.genii.client.ApplicationBase;

public abstract class JNILibraryBase extends ApplicationBase {

	public static boolean isInitialized = false;
	
	public static void initialize(){
		try{
			prepareClientApplication(null);
			isInitialized = true;
		}catch(RuntimeException e){
			System.out.println("Application already started");
		}
	}
}
