package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.rmi.RemoteException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class CdTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Changes the current directory to the one indicated.";
	static final private String _USAGE =
		"cd <target-dir>";
	
	public CdTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		chdir(getArgument(0));
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
	

	static public void chdir(String target)
		throws ResourceException, RNSException, RemoteException,
			ConfigurationException, IOException
	{
		ICallingContext ctxt = ContextManager.getCurrentContext();
		RNSPath path = ctxt.getCurrentPath().lookup(
			target, RNSPathQueryFlags.MUST_EXIST);
		TypeInformation typeInfo = new TypeInformation(path.getEndpoint());
		if (!typeInfo.isRNS())
			throw new RNSException("Path \"" + path.pwd() + 
				"\" is not an RNS directory.");
		
		ctxt.setCurrentPath(path);
		ContextManager.storeCurrentContext(ctxt);
	}
}