package edu.virginia.vcgr.genii.gjt.conf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;
import edu.virginia.vcgr.jsdl.GPUProcessorArchitecture;

public class Configuration
{
	static private Logger _logger = Logger.getLogger(Configuration.class);

	static public Configuration configuration;

	static {
		try {
			configuration = new Configuration();
		} catch (Throwable cause) {
			_logger.error("Unable to create configuration.", cause);
			System.exit(-1);
		}
	}

	private Filter<OperatingSystemNames> _operatingSystemNamesFilter;
	private Filter<ProcessorArchitecture> _processorArchitectureFilter;
	private Filter<GPUProcessorArchitecture> _gpuProcessorArchitectureFilter;
	private Map<String, SPMDVariation> _spmdVariations;

	private Configuration() throws IOException, JAXBException
	{
		File userHome = new File(System.getProperty("user.home"));
		File confDirectory = new File(userHome, ".grid-job-tool");
		if (!confDirectory.exists())
			confDirectory.mkdirs();
		if (!confDirectory.exists())
			throw new RuntimeException(String.format("Unable to find/create configuration directory %s.", confDirectory));
		if (!confDirectory.isDirectory())
			throw new RuntimeException(String.format("Unable to find/create configuration directory %s.", confDirectory));

		_operatingSystemNamesFilter = new Filter<OperatingSystemNames>(confDirectory, OperatingSystemNames.class);
		_processorArchitectureFilter = new Filter<ProcessorArchitecture>(confDirectory, ProcessorArchitecture.class);
		_gpuProcessorArchitectureFilter = new Filter<GPUProcessorArchitecture>(confDirectory, GPUProcessorArchitecture.class);
		_spmdVariations = SPMDVariations.readVariations(confDirectory).variations();
	}

	final public Filter<OperatingSystemNames> operatingSystemNamesFilter()
	{
		return _operatingSystemNamesFilter;
	}

	final public Filter<ProcessorArchitecture> processorArchitectureFilter()
	{
		return _processorArchitectureFilter;
	}
	
	final public Filter<GPUProcessorArchitecture> gpuProcessorArchitectureFilter()
	{
		return _gpuProcessorArchitectureFilter;
	}

	final public Map<String, SPMDVariation> spmdVariations()
	{
		return Collections.unmodifiableMap(_spmdVariations);
	}
}