package edu.virginia.vcgr.genii.client.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.utils.flock.FileLock;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

public class InstallationState implements Serializable
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(InstallationState.class);
	
	private HashMap<String, ContainerInformation> _runningContainers;	// Map of deployment name to port
	
	private InstallationState()
	{
		_runningContainers = new HashMap<String, ContainerInformation>();
	}
	
	static private FileLock acquireLock(File installFile)
		throws FileLockException
	{
		try
		{
			return new FileLock(installFile, 5, 1000);
		}
		catch (InterruptedException ie)
		{
			_logger.fatal("Unexpected interruption exception while trying to read installation state.", ie);
			throw new FileLockException("Unable to lock file.", ie);
		}
	}
	
	static private InstallationState readState(File installFile)
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(installFile);
			ObjectInputStream ois = new ObjectInputStream(fin);
			return (InstallationState)ois.readObject();
		}
		catch (FileNotFoundException fnfe)
		{
			// No problem, we just haven't got state yet
			return new InstallationState();
		}
		catch (ClassNotFoundException fnfe)
		{
			// Corrupt state
			_logger.error("Corrupt state found in installation description -- continuing with empty state.", fnfe);
			return new InstallationState();
		}
		catch (IOException ioe)
		{
			// Corrupt state
			_logger.error("Corrupt state found in installation description -- continuing with empty state.", ioe);
			return new InstallationState();
		}
	}
	
	static private void writeState(File installFile, InstallationState state)
		throws IOException
	{
		FileOutputStream fout = null;
		
		try
		{
			fout = new FileOutputStream(installFile);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(state);
			oos.flush();
			oos.close();
		}
		catch (IOException ioe)
		{
			_logger.fatal("Unable to write installation state to permenant storage.", ioe);
			throw ioe;
		}
		finally
		{
			StreamUtils.close(fout);
		}
	}
	
	static public HashMap<String, ContainerInformation> getRunningContainers()
		throws FileLockException
	{
		File installFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(), "installation-state");
		FileLock flock = null;
		
		try
		{
			flock = acquireLock(installFile);
			return readState(installFile)._runningContainers;
		}
		finally
		{
			StreamUtils.close(flock);
		}
	}
	
	static public void addRunningContainer(String deploymentName, URL containerURL)
		throws IOException, FileLockException
	{
		File installFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(), "installation-state");
		FileLock flock = null;
		
		try
		{
			flock = acquireLock(installFile);
			InstallationState state = readState(installFile);
			state._runningContainers.put(deploymentName, new ContainerInformation(deploymentName, containerURL));
			writeState(installFile, state);
		}
		finally
		{
			StreamUtils.close(flock);
		}
	}
	
	static public void removeRunningContainer(String deploymentName)
		throws IOException, FileLockException
	{
		File installFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(), "installation-state");
		FileLock flock = null;
		
		try
		{
			flock = acquireLock(installFile);
			InstallationState state = readState(installFile);
			state._runningContainers.remove(deploymentName);
			writeState(installFile, state);
		}
		finally
		{
			StreamUtils.close(flock);
		}
	}
}