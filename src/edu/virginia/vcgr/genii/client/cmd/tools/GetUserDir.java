package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.ContainerProperties;
import edu.virginia.vcgr.genii.client.cmd.ToolException;

public class GetUserDir extends BaseGridTool
{
	static private final String DESCRIPTION = "Prints out the user directory.";
	static private final String USAGE = "GetUserDir";
	
	public GetUserDir()
	{
		super(DESCRIPTION, USAGE, true);
	}
	
	static public String getUserDir()
	{
		return ApplicationBase.getUserDir(
			ContainerProperties.containerProperties);
	}
	
	@Override
	protected int runCommand()
	{
		stdout.println(getUserDir());
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		// Do nothing
	}
}