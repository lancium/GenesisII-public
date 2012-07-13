package edu.virginia.vcgr.genii.client.jni.gIIlib.miscellaneous;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.security.credentials.TransientCredentials;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JNILogoutTool extends JNILibraryBase 
{	
	static private Log _logger = LogFactory.getLog(JNILogoutTool.class);

	public static void logout()
	{		
		tryToInitialize();

		try
		{					
			ICallingContext callContext = 
				ContextManager.getCurrentContext(false);
			if (callContext != null) 
			{
				TransientCredentials.globalLogout(callContext);
				callContext.setActiveKeyAndCertMaterial(null);
				ContextManager.storeCurrentContext(callContext);
			}
		}
		catch(Exception e)
		{
			_logger.info("exception occurred in logout", e);
		}
	}
}
