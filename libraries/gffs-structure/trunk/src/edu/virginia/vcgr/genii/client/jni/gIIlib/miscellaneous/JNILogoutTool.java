package edu.virginia.vcgr.genii.client.jni.gIIlib.miscellaneous;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;

public class JNILogoutTool extends JNILibraryBase
{
	static private Log _logger = LogFactory.getLog(JNILogoutTool.class);

	public static void logout()
	{
		tryToInitialize();

		try {
			ICallingContext callContext = ContextManager.getCurrentContext();
			if (callContext != null) {
				ClientUtils.invalidateCredentials(callContext);
				ContextManager.storeCurrentContext(callContext);
			}
		} catch (Exception e) {
			_logger.info("exception occurred in logout", e);
		}
	}
}
