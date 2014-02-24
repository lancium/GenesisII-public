package edu.virginia.vcgr.genii.cmdLineManipulator;

import java.util.ServiceLoader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmdLineManipulators
{
	static private Log _logger = LogFactory.getLog(CmdLineManipulators.class);

	// TODO: does this even work properly? some issues with generic types here.
	@SuppressWarnings("rawtypes")
	static private ServiceLoader<CmdLineManipulator> loader = ServiceLoader.load(CmdLineManipulator.class);

	synchronized static public CmdLineManipulator<?> getCmdLineManipulator(String manipulatorType)
		throws CmdLineManipulatorException
	{
		for (CmdLineManipulator<?> manipulator : loader) {
			if (manipulator.getManipulatorType().equals(manipulatorType)) {
				_logger.debug(String.format("Loaded cmdline manipulator of type: %s", manipulatorType));
				return manipulator;
			}
		}
		throw new CmdLineManipulatorException(String.format("Could not locate manipulator of type \"%s\" for loading.",
			manipulatorType));
	}
}