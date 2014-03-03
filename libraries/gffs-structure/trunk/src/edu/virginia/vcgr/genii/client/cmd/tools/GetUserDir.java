package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class GetUserDir extends BaseGridTool {
	static private final String DESCRIPTION = "config/tooldocs/description/dGetUserDir";
	static private final String USAGE = "config/tooldocs/usage/uGetUserDir";
	static final private String _MANPAGE = "config/tooldocs/man/GetUserDir";

	public GetUserDir() {
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE),
				false, ToolCategory.INTERNAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	static public String getUserDir() {
		return InstallationProperties.getUserDir();
	}

	@Override
	protected int runCommand() {
		stdout.println(getUserDir());
		return 0;
	}

	@Override
	protected void verify() throws ToolException {
		// Do nothing
	}
}