package edu.virginia.vcgr.genii.cmdLineManipulator.config;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(namespace = CmdLineManipulatorConstants.NAMESPACE, name = "mpich-configuration")
public class MpichVariationConfiguration extends CommonVariationConfiguration {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = NAMESPACE, name = "processNum-flag", required = false, nillable = false)
	private String _processNumFlag = null;

	@XmlElement(namespace = NAMESPACE, name = "supported-spmd-variation", required = true, nillable = false)
	private Set<String> _spmdVariations = null;

	final public String processNumFlag() {
		return _processNumFlag;
	}

	final public void processNumFlag(String newFlag) {
		_processNumFlag = newFlag;
	}

	final public Set<String> spmdVariations() {
		return _spmdVariations;
	}

	final public void spmdVariations(Set<String> spmdVars) {
		_spmdVariations = spmdVars;
	}

}