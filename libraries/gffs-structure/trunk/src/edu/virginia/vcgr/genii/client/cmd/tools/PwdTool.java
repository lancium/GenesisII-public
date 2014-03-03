package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class PwdTool extends BaseGridTool {
	static final private String _DESCRIPTION = "config/tooldocs/description/dpwd";
	static final private String _USAGE = "config/tooldocs/usage/upwd";
	static final private String _MANPAGE = "config/tooldocs/man/pwd";

	public PwdTool() {
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE),
				false, ToolCategory.GENERAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable {
		stdout.println(RNSPath.getCurrent().pwd());
		return 0;
	}

	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 0)
			throw new InvalidToolUsageException();
	}
}