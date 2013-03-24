package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.container.bes.execution.phases.ByteIORedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSink;

public abstract class CommonApplicationUnderstanding implements ApplicationUnderstanding
{
	static private Log _logger = LogFactory.getLog(CommonApplicationUnderstanding.class);

	private FilesystemManager _fsManager;
	private BESWorkingDirectory _workingDirectory;

	protected CommonApplicationUnderstanding(FilesystemManager fsManager, BESWorkingDirectory workingDirectory)
	{
		_fsManager = fsManager;
		_workingDirectory = workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory)
	{
		_workingDirectory.setWorkingDirectory(workingDirectory, false);
		_fsManager.setWorkingDirectory(_workingDirectory.getWorkingDirectory());
	}

	@Override
	public BESWorkingDirectory getWorkingDirectory()
	{
		return _workingDirectory;
	}

	@Override
	public FilesystemManager getFilesystemManager()
	{
		return _fsManager;
	}

	protected StreamRedirectionSink discoverTTYRedirectionSink()
	{
		try {
			ICallingContext ctxt = ContextManager.getExistingContext();
			byte[] data = (byte[]) ctxt.getSingleValueProperty(TTYConstants.TTY_CALLING_CONTEXT_PROPERTY);
			if (data != null)
				return new ByteIORedirectionSink(EPRUtils.fromBytes(data));

			return null;
		} catch (Throwable cause) {
			_logger.warn("Unable to get the TTY property.", cause);
			return null;
		}
	}
}