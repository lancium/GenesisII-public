package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class PingTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Pings a target.";
	static final private String _USAGE =
		"ping [--attempts=<number>] <target> <query>";
	
	public PingTool() {
		super(_DESCRIPTION, _USAGE, false);
	}
	
	private int _attempts = 1;
	
	public void setAttempts(String attempts) {
		_attempts = Integer.parseInt(attempts);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath path = RNSPath.getCurrent();
		
		path = path.lookup(getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
			path.getEndpoint());

		for (int i = 0; i < _attempts; i++) {
			String response = common.ping(getArgument(1));
			stdout.println("Response " + i + ": " + response);
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException {
		if (numArguments() != 2) {
			throw new InvalidToolUsageException();
		}
	}
}