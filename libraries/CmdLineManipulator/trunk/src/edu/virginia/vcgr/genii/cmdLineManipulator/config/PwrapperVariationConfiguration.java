package edu.virginia.vcgr.genii.cmdLineManipulator.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(namespace = CmdLineManipulatorConstants.NAMESPACE, name = "pwrapper-configuration")
public class PwrapperVariationConfiguration extends
		CommonVariationConfiguration {
	static final long serialVersionUID = 0L;

}