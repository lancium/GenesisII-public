package edu.virginia.vcgr.genii.client.jni.gIIlib;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNICdTool extends JNILibraryBase {
	
	public static Boolean changeDirectory(String targetDirectory){
		if(!isInitialized){
			initialize();
		}
		try{
			ICallingContext ctxt = ContextManager.getCurrentContext();
			RNSPath path = ctxt.getCurrentPath().lookup(targetDirectory, RNSPathQueryFlags.MUST_EXIST);			
			if (!path.isDirectory()){
				return false;
			}			
			ctxt.setCurrentPath(path);
			ContextManager.storeCurrentContext(ctxt);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
