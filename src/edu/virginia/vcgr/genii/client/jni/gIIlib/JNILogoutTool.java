package edu.virginia.vcgr.genii.client.jni.gIIlib;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;

public class JNILogoutTool extends JNILibraryBase {
	
	public static void logout(){
		tryToInitialize();
		
		try{					
			ICallingContext callContext = ContextManager.getCurrentContext(false);
			if (callContext != null) {
				TransientCredentials.globalLogout(callContext);
				ClientUtils.setClientKeyAndCertMaterial(callContext, null);
				ContextManager.storeCurrentContext(callContext);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
