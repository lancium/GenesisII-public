package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.container.bes.execution.phases.ByteIORedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSink;

public abstract class CommonApplicationUnderstanding 
	implements ApplicationUnderstanding
{
	static private Log _logger = LogFactory.getLog(
		CommonApplicationUnderstanding.class);
	
	private String _workingDirectory = null;
	
	public void setWorkingDirectory(String workingDirectory)
	{
		_workingDirectory = workingDirectory;
	}
	
	@Override
	public String getWorkingDirectory()
	{
		return _workingDirectory;
	}
	
	protected StreamRedirectionSink discoverTTYRedirectionSink()
	{
		try
		{
			ICallingContext ctxt = ContextManager.getCurrentContext();
			byte []data = 
				(byte[])ctxt.getSingleValueProperty(
					TTYConstants.TTY_CALLING_CONTEXT_PROPERTY);
			if (data != null)
				return new ByteIORedirectionSink(
					EPRUtils.fromBytes(data));
			
			return null;
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to get the TTY property.", cause);
			return null;
		}
	}
}