package edu.virginia.vcgr.genii.cmdLineManipulator.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;

import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class CommonVariationConfiguration implements Serializable,
		CmdLineManipulatorConstants {
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = NAMESPACE, name = "exec-command", required = false, nillable = false)
	private String _execCommand = null;

	@XmlElement(namespace = NAMESPACE, name = "additional-arg", required = false, nillable = false)
	private List<String> _additionalArgs = null;

	// catch any other information that might be in configuration
	@XmlAnyElement
	private Collection<Element> _any = new ArrayList<Element>();

	final public String execCmd() {
		return _execCommand;
	}

	final public void execCmd(String newCmd) {
		_execCommand = newCmd;
	}

	final public List<String> additionalArgs() {
		return _additionalArgs;
	}

	final public void additionalArgs(List<String> newArgs) {
		_additionalArgs = newArgs;
	}

}