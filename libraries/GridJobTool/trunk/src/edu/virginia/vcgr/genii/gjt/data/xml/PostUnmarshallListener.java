package edu.virginia.vcgr.genii.gjt.data.xml;

import edu.virginia.vcgr.genii.gjt.data.ModificationBroker;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;

public interface PostUnmarshallListener {
	public void postUnmarshall(ParameterizableBroker parameterBroker,
			ModificationBroker modificationBroker);
}