package edu.virginia.vcgr.genii.client.jni.giilibmirror.miscellaneous;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.jni.giilibmirror.JNILibraryBase;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class JNIPwdTool extends JNILibraryBase{
	
	public static String getCurrentDirectory(){
		tryToInitialize();
		if(ENABLE_LOCAL_TEST){
			return "/";
		}
		
		try {
			return RNSPath.getCurrent().getName();
		} catch (ConfigurationException e) {			
			return "no context";
		}
	}
}