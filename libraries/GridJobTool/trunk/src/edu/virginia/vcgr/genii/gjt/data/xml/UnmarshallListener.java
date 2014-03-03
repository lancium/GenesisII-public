package edu.virginia.vcgr.genii.gjt.data.xml;

import javax.xml.bind.Unmarshaller;

import edu.virginia.vcgr.genii.gjt.data.ModificationBroker;
import edu.virginia.vcgr.genii.gjt.data.Modifyable;
import edu.virginia.vcgr.genii.gjt.data.variables.Parameterizable;
import edu.virginia.vcgr.genii.gjt.data.variables.ParameterizableBroker;

public class UnmarshallListener extends Unmarshaller.Listener {
	private ParameterizableBroker _parameterBroker;
	private ModificationBroker _modificationBroker;

	public UnmarshallListener(ParameterizableBroker parameterBroker,
			ModificationBroker modificationBroker) {
		_parameterBroker = parameterBroker;
		_modificationBroker = modificationBroker;
	}

	@Override
	public void afterUnmarshal(Object target, Object parent) {
		if (target instanceof PostUnmarshallListener)
			((PostUnmarshallListener) target).postUnmarshall(_parameterBroker,
					_modificationBroker);
	}

	@Override
	public void beforeUnmarshal(Object target, Object parent) {
		if (target instanceof Parameterizable)
			((Parameterizable) target)
					.addParameterizableListener(_parameterBroker);

		if (target instanceof Modifyable)
			((Modifyable) target).addModificationListener(_modificationBroker);
	}
}