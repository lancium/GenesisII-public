package edu.virginia.vcgr.genii.container.cservices.downloadmgr;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.io.DataTransferStatistics;
import edu.virginia.vcgr.genii.client.io.URIManager;
import edu.virginia.vcgr.genii.client.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServicePropertyListener;

public class DownloadManagerContainerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(
		DownloadManagerContainerService.class);
	
	static public final String SERVICE_NAME = "Download Manager";
	
	static public final String DOWNLOAD_TMP_DIR_PROPERTY = "download-tmpdir";
	static public final String DOWNLOAD_TMP_DIR_CSERVICE_PROPERTY =
		"edu.virginia.vcgr.genii.container.cservices.downloadmgr.download-tmpdir";
	
	private Map<File, InProgressLock> _inProgressLocks = 
		new HashMap<File, InProgressLock>();
	
	private Object _lockObject = new Object();
	private String _configuredDownloadTmpDir = null;
	private File _downloadDirectory;
	
	private void selectDownloadDirectory(String propertyValue)
	{
		String dirPath;
		
		if (propertyValue == null)
		{
			if (_configuredDownloadTmpDir == null)
			{
				dirPath = String.format("%s/download-tmp",
					ConfigurationManager.getCurrentConfiguration(
						).getUserDirectory().getAbsolutePath());
			} else
				dirPath = _configuredDownloadTmpDir;
		} else
			dirPath = propertyValue;
		
		try
		{
			synchronized(_lockObject)
			{
				_downloadDirectory = new GuaranteedDirectory(
					dirPath);
			}
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(
				"Unable to find download manager service directory.",
				ioe);
		}
		
		for (File f : _downloadDirectory.listFiles())
			f.delete();	
	}
	
	private void selectDownloadDirectory()
	{
		String prop = (String)getContainerServicesProperties().getProperty(
			DOWNLOAD_TMP_DIR_CSERVICE_PROPERTY);
		selectDownloadDirectory(prop);	
	}
	
	private DownloadManagerContainerService(String downloadTmpDir)
	{
		super(SERVICE_NAME);
		
		_configuredDownloadTmpDir = downloadTmpDir;
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
	public void setProperties(Properties properties)
	{
		super.setProperties(properties);
		
		_configuredDownloadTmpDir = properties.getProperty(
			DOWNLOAD_TMP_DIR_PROPERTY);
	}

	@Override
	protected void loadService() throws Throwable
	{
		_logger.info("Loading DownloadManager Constainer Service.");
		
		getContainerServicesProperties().addPropertyChangeListener(
			Pattern.compile("^" + 
				Pattern.quote(DOWNLOAD_TMP_DIR_CSERVICE_PROPERTY) + "$"),
			new PropertyChangeListener());

		selectDownloadDirectory();
	}

	@Override
	protected void startService() throws Throwable
	{
		_logger.info("Starting DownloadManager Constainer Service.");
	}
	
	public DataTransferStatistics download(URI source, File target,
		UsernamePasswordIdentity credential) throws IOException
	{
		target = target.getAbsoluteFile();
		InProgressLock lock;
		boolean iAmResponsible = false;
		DataTransferStatistics ret = DataTransferStatistics.startTransfer();
		
		_logger.info(String.format(
			"(Download Manager) -- About to check for %s", target));
		synchronized(_inProgressLocks)
		{
			lock = _inProgressLocks.get(target);
			if (lock == null)
			{
				if (target.exists())
				{
					_logger.info(String.format(
						"(Download Manager) -- %s already exists.", target));
					return ret.finishTransfer();
				}
				
				_logger.info(String.format(
					"(Download Manager) -- %s needs to be copied from source",
					target));
				_inProgressLocks.put(target, lock = new InProgressLock());
				iAmResponsible = true;
			}
		}
		
		if (!iAmResponsible)
		{
			_logger.info(String.format(
				"(Download Manager) -- Another thread is downloading %s so I'll wait.",
				target));
			try { lock.waitForSignal(); } catch (InterruptedException ie) {}
			_logger.info(String.format(
				"(Download Manager) -- The other thread signaled me that %s is done.",
				target));
			lock.checkException();
			return ret.finishTransfer();
		}
		
		IOException exception = null;
		
		try
		{
			_logger.info(String.format(
				"(Download Manager) -- Copy %s from source.", target));
			ret.transfer(doDownload(source, target, credential));
		}
		catch (IOException ioe)
		{
			_logger.warn(String.format(
				"(Download Manager) -- Unable to copy %s from source.", target),
				ioe);
			exception = ioe;
		}
		catch (Throwable e)
		{
			_logger.warn(String.format(
				"(Download Manager) -- Unable to copy %s from source.", target),
				e);
			exception = new IOException("Unable to download file.", e);
		}
		
		synchronized(_inProgressLocks)
		{
			_inProgressLocks.remove(target);
			
			synchronized(lock)
			{
				lock.setException(exception);
				
				_logger.info(String.format(
					"(Download Manager) -- Signalling waiting threads that %s is done.",
					target));
				lock.signal();
			}
		}
		
		if (exception != null)
			throw exception;
		
		return ret.finishTransfer();
	}
	
	private long doDownload(URI source, File realTarget,
		UsernamePasswordIdentity credential) throws IOException
	{
		File tmpTarget;
		DataTransferStatistics ret;
		
		synchronized(_lockObject)
		{
			tmpTarget = File.createTempFile(
				"dload", ".tmp", _downloadDirectory);
		}
		
		ret = URIManager.get(source, tmpTarget, credential);
		tmpTarget.renameTo(realTarget);
		return ret.bytesTransferred();
	}
	
	private class PropertyChangeListener 
		implements ContainerServicePropertyListener
	{
		@Override
		public void propertyChanged(String propertyName, Serializable newValue)
		{
			selectDownloadDirectory((String)newValue);
		}
	}
}