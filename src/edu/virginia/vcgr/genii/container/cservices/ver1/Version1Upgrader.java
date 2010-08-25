package edu.virginia.vcgr.genii.container.cservices.ver1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import edu.virginia.vcgr.genii.container.cservices.ContainerService;
import edu.virginia.vcgr.genii.container.cservices.conf.ContainerServiceConfiguration;

public class Version1Upgrader
{
	static private void writeUpgrade(File targetDirectory,
		ContainerServiceConfiguration conf) 
			throws JAXBException, ClassNotFoundException
	{
		String className = conf.serviceClass().getName();
		int index = className.lastIndexOf('.');
		if (index >= 0)
			className = className.substring(index + 1);
		
		File target = new File(targetDirectory,
			className + ".xml");
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
				Class<? extends ContainerService> serviceClass =
					v1Service.serviceClass(macros);
				if (serviceClass != null)
				{
					ContainerServiceConfiguration v2Conf =
						new ContainerServiceConfiguration(
							serviceClass, v1Service.properties(macros));
					writeUpgrade(targetDirectory, v2Conf);
				}
			}
			
			version1File.delete();
		}
		catch (ClassNotFoundException e)
		{
			throw new IOException(
				"Unable to load class.", e);
		}
		catch (JAXBException e)
		{
			throw new IOException(
				"Failed to upgrade old container services configuration.", e);
		}
	}
}