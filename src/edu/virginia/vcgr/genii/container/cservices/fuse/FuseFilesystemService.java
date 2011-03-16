package edu.virginia.vcgr.genii.container.cservices.fuse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.jsdl.JSDLFileSystem;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServicePropertyListener;

public class FuseFilesystemService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(
		FuseFilesystemService.class);
	
	static final private int NUM_CREATE_DIR_ATTEMPTS = 8;
	static final private long BACKOFF_BASE = 100L;
	
	static final public String SERVICE_NAME = "Fuse Filesystem Service";
	static public final String FUSE_DIR_PROPERTY = "fuse-directory";
	static public final String FUSE_DIR_CSERVICE_PROPERTY =
		"edu.virginia.vcgr.genii.container.cservices.fuse.fuse-directory";
	
	private Map<File, Map<String, Long>> _parentDir2MountPoint2Id =
		new HashMap<File, Map<String,Long>>();
	
	private Object _lockObject = new Object();
	private String _configuredFuseDir = null;
	private File _fuseDirectory;
	
	private void selectFuseDirectory(String propertyValue)
	{
		String dirPath;
		
		if (propertyValue == null)
		{
			if (_configuredFuseDir == null)
			{
				dirPath = ConfigurationManager.getCurrentConfiguration(
						).getUserDirectory().getAbsolutePath();
			} else
				dirPath = _configuredFuseDir;
		} else
			dirPath = propertyValue;
		
		try
		{
			synchronized(_lockObject)
			{
				_fuseDirectory = new GuaranteedDirectory(
					dirPath, "fuse-mounts");
				_fuseDirectory = _fuseDirectory.getAbsoluteFile();
			}
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(
				"Unable to find fuse service directory.",
				ioe);
		}
	}
	
	private void selectFuseDirectory()
	{
		String prop = (String)getContainerServicesProperties().getProperty(
			FUSE_DIR_CSERVICE_PROPERTY);
		selectFuseDirectory(prop);	
	}
	
	@Override
	protected void loadService() throws Throwable
	{
		_logger.info(String.format("Loading %s.", SERVICE_NAME));
		Connection connection = null;
		
		try
		{
			connection = getConnectionPool().acquire(true);
			FuseFilesystemDatabase.createTables(connection);
			
			_parentDir2MountPoint2Id.clear();
			FuseFilesystemDatabase.loadAll(connection, 
				_parentDir2MountPoint2Id);
		}
		finally
		{
			getConnectionPool().release(connection);
		}
		
		getContainerServicesProperties().addPropertyChangeListener(
			Pattern.compile("^" + 
				Pattern.quote(FUSE_DIR_CSERVICE_PROPERTY) + "$"), 
			new PropertyChangeListener());
		
		selectFuseDirectory();
	}

	@Override
	protected void startService() throws Throwable
	{
		// Clean up old directories that don't have matching reservations
		for (File parentDir : _parentDir2MountPoint2Id.keySet())
		{
			Map<String, Long> mountPoint = 
				_parentDir2MountPoint2Id.get(parentDir);
			for (File child : parentDir.listFiles())
			{
				if (!mountPoint.containsKey(child.getName()))
					child.delete();
			}
		}
	}
	
	private FuseFilesystemService(String fuseDir)
	{
		super(SERVICE_NAME);
		
		_configuredFuseDir = fuseDir;
	}
	
	public FuseFilesystemService(Properties constructionProperties)
	{
		this(constructionProperties.getProperty(FUSE_DIR_PROPERTY));
	}
	
	public FuseFilesystemService()
	{
		this((String)null);
	}
	
	@Override
	public void setProperties(Properties properties)
	{
		super.setProperties(properties);
		
		_configuredFuseDir = properties.getProperty(
			FUSE_DIR_PROPERTY);
	}
	
	final public JSDLFileSystem reserveFuseFilesystem() throws IOException, SQLException
	{
		return new FuseFileSystem(acquire());
	}
	
	final public File acquire() throws IOException, SQLException
	{
		File ret = null;
		long id;
		
		for (int attempt = 0; attempt < NUM_CREATE_DIR_ATTEMPTS; attempt++)
		{
			synchronized(_lockObject)
			{
				ret = new File(_fuseDirectory, new GUID().toString());
				if (!ret.mkdirs())
					ret = null;
			}
			
			if (ret != null)
				break;
			
			try
			{
				Thread.sleep(BACKOFF_BASE << attempt);
			}
			catch (Throwable cause)
			{
				_logger.warn(
					"Exception thrown while waiting for another attempt.", 
					cause);
			}
		}
		
		if (ret == null)
			throw new IOException(String.format(
				"Unable to find suitable fuse mount point."));
		
		Connection conn = null;
		try
		{
			conn = getConnectionPool().acquire(true);
			id = FuseFilesystemDatabase.store(
				conn, _fuseDirectory, ret.getName());
		}
		finally
		{
			getConnectionPool().release(conn);
		}
		
		Map<String, Long> mounts;
		synchronized(_parentDir2MountPoint2Id)
		{
			mounts = _parentDir2MountPoint2Id.get(_fuseDirectory);
			if (mounts == null)
				_parentDir2MountPoint2Id.put(_fuseDirectory,
					mounts = new HashMap<String, Long>());
		}
		
		synchronized(mounts)
		{
			mounts.put(ret.getName(), id);
		}
		
		return ret;
	}
	
	final public void release(File directory) throws SQLException
	{
		File parent = directory.getParentFile().getAbsoluteFile();
		Map<String, Long> mounts;
		Long id = null;
		
		synchronized(_parentDir2MountPoint2Id)
		{
			mounts = _parentDir2MountPoint2Id.get(parent);
		}
		
		if (mounts != null)
		{
			synchronized(mounts)
			{
				id = mounts.remove(directory.getName()); 
			}
		}
		
		if (id != null)
		{
			Connection conn = null;
			try
			{
				conn = getConnectionPool().acquire(true);
				FuseFilesystemDatabase.remove(conn, id);
			}
			finally
			{
				getConnectionPool().release(conn);
			}
		}
		
		directory.delete();
	}
	
	private class PropertyChangeListener
		implements ContainerServicePropertyListener
	{
		@Override
		public void propertyChanged(String propertyName, Serializable newValue)
		{
			selectFuseDirectory((String)newValue);
		}
	}
}