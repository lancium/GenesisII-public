package edu.virginia.vcgr.genii.client.cmd;

import edu.virginia.vcgr.genii.client.ApplicationBase;

public class GetUserDir
{
	static public String getUserDir()
	{
		return ApplicationBase.getUserDir();
	}
	
	static public void main(String [] args)
	{
		String userDir = getUserDir();
		System.out.print(userDir + "\n");
	}
}