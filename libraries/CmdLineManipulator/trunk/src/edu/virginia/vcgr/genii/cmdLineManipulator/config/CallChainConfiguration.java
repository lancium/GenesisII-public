package edu.virginia.vcgr.genii.cmdLineManipulator.config;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class CallChainConfiguration implements Serializable
{
	static final long serialVersionUID = 0L;

	@XmlElement(namespace = CmdLineManipulatorConstants.NAMESPACE, name = "manipulator-name", required = true, nillable = false)
	private List<String> _manipulatorNames = new ArrayList<String>();

	final protected List<String> getCallChain()
	{
		return _manipulatorNames;
	}

	final protected void setCallChain(List<String> newChain)
	{
		_manipulatorNames = newChain;
	}

}