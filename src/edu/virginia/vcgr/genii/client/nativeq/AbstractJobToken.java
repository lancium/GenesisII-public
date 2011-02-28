package edu.virginia.vcgr.genii.client.nativeq;

import java.util.List;

public class AbstractJobToken implements JobToken
{
	private static final long serialVersionUID = 1L;
	
	//final cmdLine stored for accounting purposes
	protected List<String> _cmdLine;
	
	public void setCmdLine(List<String> cmdLine){
		_cmdLine = cmdLine;
	}
	
	public List<String> getCmdLine(){
		return _cmdLine;
	}
}