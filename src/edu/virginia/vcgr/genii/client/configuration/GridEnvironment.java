package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.morgan.util.io.StreamUtils;
import org.mortbay.log.LogFactory;

public class GridEnvironment
{
	static public final String GRID_ENVIRONMENT_FILENAME = ".gridenv.properties";
	
	static public final String GRID_PATH_ENV_VARIABLE = "GPATH";
	
	static private Log _logger = LogFactory.getLog(GridEnvironment.class);
	
	static private boolean _loaded = false;
	
	static private File getGridEnvFile()
	{
		String homedirPath = System.getProperty("user.home");
		if (homedirPath == null)
		{
			_logger.warn("Unable to find user's home directory property.");
			return null;
		}
		
		File homedir = new File(homedirPath);
		File gridenvFile = new File(homedir, GRID_ENVIRONMENT_FILENAME);
		if (!gridenvFile.exists() || !gridenvFile.isFile() || 
			!gridenvFile.canRead())
		{
			_logger.debug(String.format(
				"Unable to locate grid environment file %s.", gridenvFile));
			return null;
		}
		
		return gridenvFile;
	}
	
	static private Properties loadProperties(File gridFile)
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(gridFile);
			Properties props = new Properties();
			props.load(fin);
			return props;
		}
		catch (IOException ioe)
		{
			_logger.warn(String.format(
				"Unable to load grid environment file %s.", gridFile));
			return null;
		}
		finally
		{
			StreamUtils.close(fin);
		}
	}
	
	synchronized static public void loadGridEnvironment() 
	{
		if (_loaded)
			return;
		
		_loaded = true;
		
		File gridEnvFile = getGridEnvFile();
		if (gridEnvFile != null)
		{
			Properties props = loadProperties(gridEnvFile);
			if (props != null)
			{
				for (Object key : props.keySet())
				{
					String keyString = key.toString();
					System.setProperty(
						keyString, props.getProperty(keyString));
				}
			}
		}
	}
}