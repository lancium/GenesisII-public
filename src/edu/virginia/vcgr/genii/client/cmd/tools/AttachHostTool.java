package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;

public class AttachHostTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Attachs the indicated container into the griven hosting envrionment.";
	static final private String _USAGE =
		"attach-host <container-url> <rns-path>";
	
	public AttachHostTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String containerURL = getArgument(0);
		String rnsPath = getArgument(1);
		
		containerURL = Hostname.normalizeURL(containerURL);
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(rnsPath, RNSPathQueryFlags.MUST_NOT_EXIST);
		
		OGSARP rp = (OGSARP)ResourcePropertyManager.createRPInterface(
			EPRUtils.makeEPR(containerURL), OGSARP.class);
		
		EndpointReferenceType epr = rp.getResourceEndpoint();
		
		path.link(epr);
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
}