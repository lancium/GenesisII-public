package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

public class ScriptBasedQueueConfiguration 
	implements Serializable, NativeQConstants
{
	static final long serialVersionUID = 0L;
	
	@XmlAttribute(name = "bash-binary", required = false)
	private String _bashBinary = null;
	
	@XmlAttribute(name = "submit-script-name", required = false)
	private String _submitScriptName = null;
	
	final public File bashBinary(File defaultBashBinary)
	{
		return (_bashBinary == null) ? defaultBashBinary : new File(_bashBinary);
	}
	
	final public void bashBinary(String newBashBinary)
	{
		_bashBinary = newBashBinary;
	}
	
	final public String submitScriptName()
	{
		return _submitScriptName;
	}
	
	final public void submitScriptName(String name)
	{
		_submitScriptName = name;
	}
}