package edu.virginia.vcgr.genii.cmdLineManipulator.config;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

import java.util.Set;
import java.util.List;
import java.util.HashSet;

@XmlAccessorType(XmlAccessType.NONE)
public class CmdLineManipulatorConfiguration implements Serializable,
		CmdLineManipulatorConstants {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = NAMESPACE, name = "manipulator-variation", required = true, nillable = false)
	private Set<VariationConfiguration> _variationSet = new HashSet<VariationConfiguration>();

	@XmlElement(namespace = NAMESPACE, name = "call-chain", required = true, nillable = false)
	private CallChainConfiguration _callChain = new CallChainConfiguration();

	final public Set<VariationConfiguration> variationSet() {
		return _variationSet;
	}

	final public void variationSet(Set<VariationConfiguration> newVarSet) {
		_variationSet = newVarSet;
	}

	final public List<String> callChain() {
		return _callChain.getCallChain();
	}

	final public void callChain(List<String> newChain) {
		_callChain.setCallChain(newChain);
	}
}