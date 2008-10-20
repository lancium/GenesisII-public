package edu.virginia.vcgr.genii.client.jni.gIIlib.miscellaneous;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;

public class JNILogoutTool extends JNILibraryBase {
	
	public static void logout(){		
		tryToInitialize();
		if(ENABLE_LOCAL_TEST){
			return;
		}
		
		try{					
			ICallingContext callContext = ContextManager.getCurrentContext(false);
			if (callContext != null) {
				TransientCredentials.globalLogout(callContext);
				callContext.setActiveKeyAndCertMaterial(null);
				ContextManager.storeCurrentContext(callContext);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
