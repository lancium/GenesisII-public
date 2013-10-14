package edu.virginia.vcgr.genii.cmdLineManipulator.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(namespace = CmdLineManipulatorConstants.NAMESPACE, name = "sudo-pwrapper-configuration")
public class SudoPwrapperVariationConfiguration extends CommonVariationConfiguration
{

	private static final long serialVersionUID = 0;

	@XmlElement(namespace = NAMESPACE, name = "target-user", required = true, nillable = false)
	private String _targetUser = null;

	@XmlElement(namespace = NAMESPACE, name = "sudo-bin-path", required = true, nillable = false)
	private String _sudoBinPath = null;

	public String getTargetUser()
	{
		return _targetUser;
	}

	public void setTargetUser(String _targetUser)
	{
		this._targetUser = _targetUser;
	}

	public String getSudoBinPath()
	{
		return _sudoBinPath;
	}

	public void setSudoBinPath(String _sudoBinPath)
	{
		this._sudoBinPath = _sudoBinPath;
	}
}
