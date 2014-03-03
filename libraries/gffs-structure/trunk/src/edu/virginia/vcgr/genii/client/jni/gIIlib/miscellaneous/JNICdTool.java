package edu.virginia.vcgr.genii.client.jni.gIIlib.miscellaneous;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JNICdTool extends JNILibraryBase
{
	static private Log _logger = LogFactory.getLog(JNICdTool.class);

	public static Boolean changeDirectory(String targetDirectory)
	{
		tryToInitialize();

		try {
			ICallingContext ctxt = ContextManager.getExistingContext();
			RNSPath path = ctxt.getCurrentPath().lookup(targetDirectory, RNSPathQueryFlags.MUST_EXIST);
			if (!(new TypeInformation(path.getEndpoint()).isRNS()))
				return false;

			ctxt.setCurrentPath(path);
			ContextManager.storeCurrentContext(ctxt);
		} catch (Exception e) {
			_logger.info("exception occurred in changeDirectory", e);
			return false;
		}

		return true;
	}
}
