package edu.virginia.vcgr.genii.container.bes;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.GuaranteedDirectory;

import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.container.Container;

public class BESUtilities
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(BESUtilities.class);
	
	static public File getBESWorkerDir()
	{
		String workerDirString = Container.getContainerConfiguration(
			).getGlobalProperties().getProperty(
				BESConstants.CONFIG_PROPERTY_WORKER_DIR);
		
		if (workerDirString == null)
			throw new ConfigurationException(
				"Unable to find BES worker dir configuration property.");
		try
		{
			return new GuaranteedDirectory(workerDirString);
		}
		catch (IOException ioe)
		{
			throw new ConfigurationException(
				"Invalid BES worker dir configuration property.", ioe);
		}
	}
	
	static public boolean canOverrideBESWorkerDir()
	{
		String canOverrideString = Container.getContainerConfiguration(
			).getGlobalProperties().getProperty(
				BESConstants.CONFIG_PROPERTY_WORKER_DIR_ALLOW_OVERRIDE);
		if (canOverrideString != null && canOverrideString.equalsIgnoreCase("true"))
			return true;
		
		return false;
	}
	
	static public void markDeletable(File directory) throws IOException
	{
		File marker = new File(directory, ".genesisIIDeletable");
		marker.createNewFile();
	}
	
	static public boolean isDeletable(File directory)
	{
		File marker = new File(directory, ".genesisIIDeletable");
		return marker.exists();
	}
}