package edu.virginia.vcgr.genii.client.jni.gIIlib;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.rns.RNSPath;


public class JNIPwdTool extends JNILibraryBase{
	
	public static String getCurrentDirectory(){
		if(!isInitialized){
			initialize();
		}
		try {
			return RNSPath.getCurrent().getName();
		} catch (ConfigurationException e) {			
			return "no context";
		}
	}
}