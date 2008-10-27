package edu.virginia.vcgr.genii.container.cservices.scratchmgr;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.io.FileUtils;
import edu.virginia.vcgr.genii.container.cservices.AbstractContainerService;
import edu.virginia.vcgr.genii.container.cservices.ContainerServicePropertyListener;

public class ScratchFSManagerContainerService extends AbstractContainerService
{
	static private Log _logger = LogFactory.getLog(ScratchFSManagerContainerService.class);
	
	static final public String SERVICE_NAME = "Swap File Manager";
	
	static final public String SCRATCH_DIRECTORY_PROPERTY = "scratch-directory";
	static final public String SCRATCH_SPACE_CSERVICES_PROPERTY =
		"edu.virginia.vcgr.genii.container.cservices.scratchmgr.scratch-directory";
	
	/* Amount of time a swap file can remain idle before it is 
	   reclaimed -- 1 day */
	static final public long DEFAULT_IDLE_TIMEOUT_MILLIS = 
		1000L * 60 * 60 * 24;
	
	/* Amount of time a swap file can be in use without getting returned
	   before we declare it leaked and relcaim the use token -- 1 week */
	
	static final public long DEFAULT_DIR_USE_TIMEOUT_MILLIS = 
		1000L * 60 * 60 * 24 * 7;
	
	private String _configuredScratchSpaceName = null;
	private ScratchFSDatabase _db;
	private File _uberDirectory;
	
	private File getSwapFilesystemDirectory(String directoryName)
	{
		synchronized(_db)
		{
			return new File(_uberDirectory, directoryName).getAbsoluteFile();
		}
	}
	
	@Override
	protected void loadService() throws Throwable
	{
		_logger.info("Loading SwapFSManager Constainer Service.");
		
		Connection conn = null;
		
		try
		{
			conn = getConnectionPool().acquire();
			_db = new ScratchFSDatabase(conn);
			conn.commit();
			
			getContainerServicesProperties().addPropertyChangeListener(
				Pattern.compile("^" + 
					Pattern.quote(SCRATCH_SPACE_CSERVICES_PROPERTY) + "$"), 
				new PropertyChangeListener());

			selectUberDirectory();
		}
		finally
		{
			getConnectionPool().release(conn);
		}
	}

	@Override
	protected void startService() throws Throwable
	{
		_logger.info("Starting SwapFSManager Constainer Service.");
		
		Collection<File> directoriesToDelete;
		
		Connection conn = null;
		
		try
		{
			conn = getConnectionPool().acquire();
			
			_db.cleanupExpiredReservations(conn, 
				DEFAULT_DIR_USE_TIMEOUT_MILLIS);
			_db.patchIdles(conn);
			directoriesToDelete = _db.cleanupExpiredDirectories(conn,
				DEFAULT_IDLE_TIMEOUT_MILLIS);
			
			for (File dir : directoriesToDelete)
				FileUtils.recursivelyRemove(dir);
			
			conn.commit();
		}
		finally
		{
			getConnectionPool().release(conn);
		}
	}
	
	private void selectUberDirectory(String propertyValue)
	{
		String path;
		
		if (propertyValue == null)
		{
			if (_configuredScratchSpaceName == null)
			{
				path = String.format("%s/scratch-space", 
					ConfigurationManager.getCurrentConfiguration(
						).getUserDirectory().getAbsolutePath());
			} else
				path = _configuredScratchSpaceName;
		} else
			path = propertyValue;
		
		try
		{
			synchronized(_db)
			{
				_uberDirectory = new GuaranteedDirectory(path);
			}
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException("Unable to create swap space.",
				ioe);
		}	
	}
	
	private void selectUberDirectory()
	{
		selectUberDirectory(
			(String)getContainerServicesProperties().getProperty(
				SCRATCH_SPACE_CSERVICES_PROPERTY));
	}
	
	private ScratchFSManagerContainerService(String scratchDirectory)
	{
		super(SERVICE_NAME);
		
		_configuredScratchSpaceName = scratchDirectory;
	}
	
	public ScratchFSManagerContainerService(Properties constructionProperties)
	{
		this(constructionProperties.getProperty(SCRATCH_DIRECTORY_PROPERTY));	
	}
	
	public ScratchFSManagerContainerService()
	{
		this((String)null);
	}
	
	public ScratchFileSystem reserveSwapFilesystem(String directoryName)
		throws IOException
	{
		Connection conn = null;
		File directory = getSwapFilesystemDirectory(directoryName);
		long reservationID;
		
		synchronized(_db)
		{
			if (!directory.exists())
				directory.mkdirs();
			if (!directory.exists())
				throw new IOException(String.format(
					"Unable to create scratch space directory \"%s\".", 
					directory));
			if (!directory.isDirectory())
				throw new IOException(String.format(
					"Scratch space path \"%s\" does not appear to be a directory.",
					directory));
			
			try
			{
				conn = getConnectionPool().acquire();
				
				reservationID = _db.reserveDirectory(conn, directory);
				
				conn.commit();

				return new ScratchFileSystem(directory, reservationID);
			}
			catch (SQLException sqe)
			{
				throw new IOException("Unable to reserve swap file system.", sqe);
			}
			finally
			{
				getConnectionPool().release(conn);
			}
		}
	}
	
	public void releaseReservation(ScratchFileSystem fileSystemReservation)
		throws IOException
	{
		Connection conn = null;
		long reservationID = fileSystemReservation.getReservationID();
		
		synchronized(_db)
		{
			try
			{
				conn = getConnectionPool().acquire();
				
				long dirID = _db.releaseReservation(conn, reservationID);
				_db.patchIdle(conn, dirID);
				
				conn.commit();
			}
			catch (SQLException sqe)
			{
				throw new IOException("Unable to release reservation.", sqe);
			}
			finally
			{
				getConnectionPool().release(conn);
			}
		}
	}
	
	private class PropertyChangeListener
		implements ContainerServicePropertyListener
	{
		@Override
		public void propertyChanged(String propertyName, Serializable newValue)
		{
			selectUberDirectory((String)newValue);
		}
	}
}