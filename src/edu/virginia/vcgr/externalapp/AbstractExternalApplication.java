package edu.virginia.vcgr.externalapp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractExternalApplication 
	implements ExternalApplication
{
	static private Log _logger = LogFactory.getLog(
		AbstractExternalApplication.class);
	
	protected abstract void doRun(File content) throws Throwable;
	
	protected AbstractExternalApplication()
	{
	}
	
	protected File launch(File content) throws Throwable
	{
		if (content.exists())
		{
			if (!content.canRead())
				throw new IOException(String.format(
					"Unable to read file \"%s\".", content));
			
			if (!content.canWrite())
				throw new IOException(String.format(
					"Unable to modify file \"%s\".", content));
		}
		
		long lastModified = content.lastModified();
		doRun(content);
		if (content.lastModified() > lastModified)
			return content;
		
		return null;
	}
	
	
	
	@Override
	public ExternalApplicationToken launch(File content,
		ExternalApplicationCallback... callbacks)
	{
		Launchable token = new Launchable(content, callbacks);
		Thread th = new Thread(token, "Abstract External Application Runner");
		th.setDaemon(false);
		th.start();
		return token;
	}
	
	private class Launchable implements Runnable, ExternalApplicationToken
	{
		private File _fileContent;
		private ExternalApplicationCallback []_callbacks;
		volatile private boolean _done = false;
		private Object _lockObject = new Object();
		private Throwable _exception = null;
		
		private Launchable(File fileContent,
			ExternalApplicationCallback []callbacks)
		{
			_fileContent = fileContent;
			_callbacks = callbacks;
		}
		
		@Override
		public void run()
		{
			try
			{
				_fileContent = launch(_fileContent);
				for (ExternalApplicationCallback callback : _callbacks)
				{
					try
					{
						callback.externalApplicationExited(_fileContent);
					}
					catch (Throwable cause)
					{
						_logger.warn(
							"Error calling callback for external application.",
							cause);
					}
				}
			}
			catch (Throwable cause)
			{
				_exception = cause;
				for (ExternalApplicationCallback callback : _callbacks)
				{
					try
					{
						callback.externalApplicationFailed(cause);
					}
					catch (Throwable cause2)
					{
						_logger.warn(
							"Error calling callback for external application.",
							cause2);
					}
				}
			}
			finally
			{
				synchronized(_lockObject)
				{
					_done = true;
					_lockObject.notifyAll();
				}
			}
		}

		@Override
		public File getResult() throws Throwable
		{
			synchronized(_lockObject)
			{
				while (!_done)
				{
					_lockObject.wait();
				}
			}
			
			if (_exception != null)
				throw _exception;
			
			return _fileContent;
		}
	}
}