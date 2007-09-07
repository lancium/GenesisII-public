package edu.virginia.vcgr.genii.client.jni.gIIlib;

import edu.virginia.vcgr.genii.client.ApplicationBase;

public abstract class JNILibraryBase extends ApplicationBase {

	public static boolean isInitialized = false;
	public static final boolean DEBUG = true;
	
	synchronized public static void tryToInitialize(){
		if(!isInitialized){
			initialize();
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
