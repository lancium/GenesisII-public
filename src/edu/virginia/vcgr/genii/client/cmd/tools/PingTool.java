package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;

public class PingTool extends BaseGridTool
{
	static final private String _DEFAULT_MESSAGE = "Hello, World!";
	
	static final private String _DESCRIPTION =
		"Pings a target.";
	static final private String _USAGE =
		"ping [--attempts=<number>] <target> [<query>]";
	
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
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if(gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<target> must be a grid path. ");
		RNSPath path = lookup(gPath, RNSPathQueryFlags.MUST_EXIST);
		
		EndpointReferenceType target = path.getEndpoint();
		/*
		target.getAddress().get_value().setQueryString("genii-container-id=" + new GUID());
		*/
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
			target);

		String msg = _DEFAULT_MESSAGE;
		if (numArguments() == 2)
			msg = getArgument(1);
		
		for (int i = 0; i < _attempts; i++) 
		{
			String response = common.ping(msg);
			stdout.format("Response %d:  %s\n", i, response);
			stdout.format("Endpoint Information:  %s\n", 
				ClientUtils.getLastEndpointInformation(common));
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if ( (numArgs < 1) || (numArgs > 2) )
			throw new InvalidToolUsageException();
	}
}