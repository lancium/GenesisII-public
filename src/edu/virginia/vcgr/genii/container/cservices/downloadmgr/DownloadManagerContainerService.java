package edu.virginia.vcgr.genii.container.cservices.downloadmgr;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;

public class DownloadManagerContainerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(
		DownloadManagerContainerService.class);
	
	static public final String SERVICE_NAME = "Download Manager";
	
	static public final String DOWNLOAD_TMP_DIR_PROPERTY = "download-tmpdir";
	
	private Map<File, InProgressLock> _inProgressLocks = 
		new HashMap<File, InProgressLock>();
	
	private File _downloadDirectory;
	
	private DownloadManagerContainerService(String downloadTmpDir)
	{
		super(SERVICE_NAME);
		
		if (downloadTmpDir == null)
		{
			downloadTmpDir = String.format("%s/download-tmp",
				ConfigurationManager.getCurrentConfiguration(
					).getUserDirectory().getAbsolutePath());
		}
		
		try
		{
			_downloadDirectory = new GuaranteedDirectory(
				downloadTmpDir);
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(
				"Unable to create download manager service.", ioe);
		}
	}
	
	public DownloadManagerContainerService(Properties constructionProperties)
	{
		this(constructionProperties.getProperty(DOWNLOAD_TMP_DIR_PROPERTY));
	}
	
	public DownloadManagerContainerService()
	{
		this((String)null);
	}
	
	@Override
	protected void loadService() throws Throwable
	{
		_logger.info("Loading DownloadManager Constainer Service.");
	}

	@Override
	protected void startService() throws Throwable
	{
		_logger.info("Starting DownloadManager Constainer Service.");
		
		for (File f : _downloadDirectory.listFiles())
			f.delete();
	}
	
	public void download(URI source, File target,
		UsernamePasswordIdentity credential) throws IOException
	{
		target = target.getAbsoluteFile();
		InProgressLock lock;
		
		synchronized(_inProgressLocks)
		{
			lock = _inProgressLocks.get(target);
			if (lock != null)
			{
				synchronized(lock)
				{
					try { lock.wait(); } catch (InterruptedException ie) {}
					lock.checkException();
					return;
				}
			}
			
			if (target.exists())
				return;
			
			_inProgressLocks.put(target, (lock = new InProgressLock()));
		}
		
		IOException exception = null;
		
		try
		{
			doDownload(source, target, credential);
		}
		catch (IOException ioe)
		{
			exception = ioe;
		}
		catch (Throwable e)
		{
			exception = new IOException("Unable to download file.", e);
		}
		
		synchronized(_inProgressLocks)
		{
			_inProgressLocks.remove(target);
			
			synchronized(lock)
			{
				lock.setException(exception);
				lock.notifyAll();
			}
		}
		
		if (exception != null)
			throw exception;
	}
	
	private void doDownload(URI source, File realTarget,
		UsernamePasswordIdentity credential) throws IOException
	{
		File tmpTarget = File.createTempFile(
			"dload", ".tmp", _downloadDirectory);
		
		URIManager.get(source, tmpTarget, credential);
		tmpTarget.renameTo(realTarget);
	}
}