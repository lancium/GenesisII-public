package edu.virginia.vcgr.genii.client.container;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.utils.flock.FileLock;

public class ContainerIDFile
{
	static private Log _logger = LogFactory.getLog(ContainerIDFile.class);
	
	static final private String _CONTAINER_ID_FILENAME = "container-id.dat";
	
	static public GUID containerID()
	{
		File target = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(),
			_CONTAINER_ID_FILENAME);
		
		if (!target.exists())
			return null;
		
		FileLock lock = null;
		FileReader reader = null;
		BufferedReader bReader = null;
		
		try
		{
			lock = new FileLock(target, 5, 1000L);
			reader = new FileReader(target);
			bReader = new BufferedReader(reader);
			String line;
			if ( (line = bReader.readLine()) != null)
				return GUID.fromString(line.trim());
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to read container id from file.", cause);
		}
		finally
		{
			StreamUtils.close(bReader);
			StreamUtils.close(reader);
			StreamUtils.close(lock);
		}
		
		return null;
	}
	
	static public void containerID(GUID guid)
	{
		File target = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(),
			_CONTAINER_ID_FILENAME);
		
		FileLock lock = null;
		PrintWriter pw = null;
		
		try
		{
			lock = new FileLock(target, 5, 1000L);
			pw = new PrintWriter(target);
			pw.println(guid);
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to write container id to container id file.");
		}
		finally
		{
			StreamUtils.close(pw);
			StreamUtils.close(lock);
		}
	}
}
