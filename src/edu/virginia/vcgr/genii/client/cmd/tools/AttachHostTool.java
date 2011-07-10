package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;

public class AttachHostTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dattachhost";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uattach-host";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/attach-host";
	
	public AttachHostTool()
	{
		super(new FileResource(_DESCRIPTION), 
				new FileResource(_USAGE), false,ToolCategory.ADMINISTRATION);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String containerURL = getArgument(0);
		RNSPath path = lookup(new GeniiPath(getArgument(1)),
			RNSPathQueryFlags.MUST_NOT_EXIST);
		
		containerURL = Hostname.normalizeURL(containerURL);
		
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