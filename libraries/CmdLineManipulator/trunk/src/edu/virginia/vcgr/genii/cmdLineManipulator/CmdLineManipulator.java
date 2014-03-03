package edu.virginia.vcgr.genii.cmdLineManipulator;

import java.util.List;
import java.util.Map;

import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;

public interface CmdLineManipulator<ConfigType> {
	public String getManipulatorType();

	public Class<ConfigType> variationConfigurationType();

	public List<String> transform(Map<String, Object> jobProperties,
			CmdLineManipulatorConfiguration manipulatorConfiguration,
			String variationName) throws CmdLineManipulatorException;

}