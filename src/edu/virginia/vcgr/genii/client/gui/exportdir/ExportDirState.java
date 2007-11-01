package edu.virginia.vcgr.genii.client.gui.exportdir;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.utils.flock.FileLock;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;

public class ExportDirState
{
	static final private String _KNOWN_EXPORTS_FILENAME = "known-exports";
	
	static private Log _logger = LogFactory.getLog(ExportDirState.class);
	
	static private FileLock acquireLock(File installFile)
		throws FileLockException
	{
		try
		{
			return new FileLock(installFile, 5, 1000);
		}
		catch (InterruptedException ie)
		{
			_logger.fatal("Unexpected interruption exception while trying to read known exports state.", ie);
			throw new FileLockException("Unable to lock file.", ie);
		}
	}
	
	@SuppressWarnings("unchecked")
	static private HashMap<String, Collection<ExportDirInformation>> readState(File installFile)
	{
		FileInputStream fin = null;
		
		try
		{
			fin = new FileInputStream(installFile);
			ObjectInputStream ois = new ObjectInputStream(fin);
			return (HashMap<String, Collection<ExportDirInformation>>)ois.readObject();
		}
		catch (FileNotFoundException fnfe)
		{
			// No problem, we just haven't got state yet
			return new HashMap<String, Collection<ExportDirInformation>>();
		}
		catch (ClassNotFoundException fnfe)
		{
			// Corrupt state
			_logger.error("Corrupt state found in installation description -- continuing with empty state.", fnfe);
			return new HashMap<String, Collection<ExportDirInformation>>();
		}
		catch (IOException ioe)
		{
			// Corrupt state
			_logger.error("Corrupt state found in installation description -- continuing with empty state.", ioe);
			return new HashMap<String, Collection<ExportDirInformation>>();
		}
	}
	
	static private void writeState(File installFile, HashMap<String, Collection<ExportDirInformation>> state)
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
	
	static public HashMap<String, Collection<ExportDirInformation>> getKnownExports()
		throws FileLockException
	{
		File installFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(), _KNOWN_EXPORTS_FILENAME);
		FileLock flock = null;
		
		try
		{
			flock = acquireLock(installFile);
			return readState(installFile);
		}
		finally
		{
			StreamUtils.close(flock);
		}
	}
	
	static public void addExport(String deployment, ExportDirInformation info)
		throws FileLockException, IOException
	{
		File installFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(), _KNOWN_EXPORTS_FILENAME);
		FileLock flock = null;
		
		try
		{
			flock = acquireLock(installFile);
			HashMap<String, Collection<ExportDirInformation> > current = readState(installFile);
			Collection<ExportDirInformation> deployments = current.get(deployment);
			if (deployments == null)
			{
				deployments = new ArrayList<ExportDirInformation>();
				current.put(deployment, deployments);
			}
			
			deployments.add(info);
			writeState(installFile, current);
		}
		finally
		{
			StreamUtils.close(flock);
		}
	}
	
	static public void removeExport(ExportDirInformation info)
		throws FileLockException, IOException
	{
		File installFile = new File(
			ConfigurationManager.getCurrentConfiguration().getUserDirectory(), _KNOWN_EXPORTS_FILENAME);
		FileLock flock = null;
		
		try
		{
			flock = acquireLock(installFile);
			HashMap<String, Collection<ExportDirInformation> > current = readState(installFile);
			for (String deployment : current.keySet())
			{
				Collection<ExportDirInformation> deployments = current.get(deployment);
				if (deployments.remove(info))
					break;
			}
			
			writeState(installFile, current);
		}
		finally
		{
			StreamUtils.close(flock);
		}
	}
}