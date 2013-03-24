package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.ContainerProperties;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class GetUserDir extends BaseGridTool
{
	static private final String DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dGetUserDir";
	static private final String USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uGetUserDir";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/GetUserDir";

	public GetUserDir()
	{
		super(new FileResource(DESCRIPTION), new FileResource(USAGE), false, ToolCategory.INTERNAL);
		addManPage(new FileResource(_MANPAGE));
	}

	static public String getUserDir()
	{
		return ApplicationBase.getUserDir(ContainerProperties.containerProperties);
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