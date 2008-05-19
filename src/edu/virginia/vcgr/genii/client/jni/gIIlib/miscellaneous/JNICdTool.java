package edu.virginia.vcgr.genii.client.jni.gIIlib.miscellaneous;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNICdTool extends JNILibraryBase {
	
	public static Boolean changeDirectory(String targetDirectory){
		tryToInitialize();
		
		try{
			ICallingContext ctxt = ContextManager.getCurrentContext();
			RNSPath path = ctxt.getCurrentPath().lookup(targetDirectory, RNSPathQueryFlags.MUST_EXIST);			
			if (!(new TypeInformation(path.getEndpoint()).isRNS())){
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
