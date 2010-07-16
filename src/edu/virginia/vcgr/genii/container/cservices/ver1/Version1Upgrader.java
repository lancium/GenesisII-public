package edu.virginia.vcgr.genii.container.cservices.ver1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import edu.virginia.vcgr.genii.container.cservices.conf.ContainerServiceConfiguration;

public class Version1Upgrader
{
	static private void writeUpgrade(File targetDirectory,
		ContainerServiceConfiguration conf) throws JAXBException
	{
		File target = null;
		if (targetDirectory.isDirectory())
			return;
		
		JAXBContext v2Context = JAXBContext.newInstance(
			ContainerServiceConfiguration.class);
		v2Context.createMarshaller().marshal(conf, target);
	}
	
	static public void upgrade(File version1File, File targetDirectory)
		throws IOException
	{
		if (!version1File.exists())
			return;
		
		if (!targetDirectory.exists())
			targetDirectory.mkdirs();
		if (!targetDirectory.exists())
			throw new FileNotFoundException(String.format(
				"Unable to create target directory \"%s\".", targetDirectory));
		
		try
		{
			JAXBContext v1Context = JAXBContext.newInstance(
				Version1Configuration.class);
			Version1Configuration v1Conf = 
				(Version1Configuration)v1Context.createUnmarshaller(
					).unmarshal(version1File);
			Properties macros = v1Conf.variables();
			
			for (Version1ContainerService v1Service : v1Conf.services())
			{
				ContainerServiceConfiguration v2Conf = new ContainerServiceConfiguration(
					v1Service.className(macros), v1Service.properties(v1Conf.variables()));
				writeUpgrade(targetDirectory, v2Conf);
			}
		}
		catch (JAXBException e)
		{
			throw new IOException(
				"Failed to upgrade old container services configuration.", e);
		}
	}
}