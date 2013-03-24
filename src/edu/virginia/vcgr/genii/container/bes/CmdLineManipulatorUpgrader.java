package edu.virginia.vcgr.genii.container.bes;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.client.cmdLineManipulator.CmdLineManipulatorUtils;

class CmdLineManipulatorUpgrader
{
	static private Log _logger = LogFactory.getLog(CmdLineManipulatorUpgrader.class);

	static boolean upgrade(BESConstructionParameters params) throws IOException
	{
		boolean ret = false;

		CmdLineManipulatorConfiguration manipulatorConf = params.getCmdLineManipulatorConfiguration();

		// determine if pwrapper should be added
		if (manipulatorConf == null) {
			if (_logger.isDebugEnabled())
				_logger.debug("Upgrader: null cmdline manipulator configuration found.");
			manipulatorConf = new CmdLineManipulatorConfiguration();
			CmdLineManipulatorUtils.addPwrapperManipulator(manipulatorConf);
			params.setCmdLineManipulatorConfiguration(manipulatorConf);
			ret = true;
		} else if (manipulatorConf.callChain().size() == 0) {
			if (_logger.isDebugEnabled())
				_logger.debug("Upgrader: empty cmdline manipulator call chain found.");
			CmdLineManipulatorUtils.addPwrapperManipulator(manipulatorConf);
			params.setCmdLineManipulatorConfiguration(manipulatorConf);
			ret = true;
		}

		return ret;
	}
}
