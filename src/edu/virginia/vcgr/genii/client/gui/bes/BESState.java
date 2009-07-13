package edu.virginia.vcgr.genii.client.gui.bes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.utils.flock.FileLock;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

public class BESState
{
	static final private String FILENAME = "bes-containers.txt";
	
	static private Log _logger = LogFactory.getLog(BESState.class);
	
	static private FileLock acquireLock(File installationFile) 
		throws FileLockException
	{
		try
		{
			return new FileLock(installationFile, 5, 1000);
		}
		catch (InterruptedException ie)
		{
			throw new FileLockException("Unable to acquire file lock.", ie);
		}
	}
	
	@SuppressWarnings("unchecked")
	static private Map<String, WSName> readState()
	{
		File installationFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(),
			FILENAME);
		FileLock lock = null;
		FileInputStream fin = null;
		
		try
		{
			lock = acquireLock(installationFile);
			fin = new FileInputStream(installationFile);
			ObjectInputStream ois = new ObjectInputStream(fin);
			return (Map<String, WSName>)ois.readObject();
		}
		catch (FileNotFoundException fnfe)
		{
			_logger.debug("No BES state to read....creating an empty one.", fnfe);
		}
		catch (Throwable cause)
		{
			_logger.warn("Unable to read BES state....creating an empty one.", cause);
		}
		finally
		{
			StreamUtils.close(fin);
			StreamUtils.close(lock);
		}
		
		return new HashMap<String, WSName>();
	}
	
	static private void writeState(Map<String, WSName> state)
	{
		File installationFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(),
			FILENAME);
		FileLock lock = null;
		FileOutputStream fos = null;
		
		try
		{
			lock = acquireLock(installationFile);
			fos = new FileOutputStream(installationFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(state);
			oos.close();
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to store BES state.", cause);
		}
		finally
		{
			StreamUtils.close(fos);
			StreamUtils.close(lock);
		}
	}
	
	static public Map<String, WSName> knownBESContainers()
	{
		return Collections.unmodifiableMap(readState());
	}
	
	static public void addKnownBESContainer(String path, 
		EndpointReferenceType container)
	{
		Map<String, WSName> state = readState();
		state.put(path, new WSName(container));
		writeState(state);
	}
	
	static public void removeKnownBESContainer(String path)
	{
		Map<String, WSName> state = readState();
		state.remove(path);
		writeState(state);
	}
}