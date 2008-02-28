package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.ArrayList;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;

import edu.virginia.vcgr.genii.client.bes.BESConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class SetDeployersTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Set's the deployers that work with a given BES.";
	static final private String _USAGE =
		"set-deployers <target-bes> <deployer-path...>";
	
	public SetDeployersTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		ArrayList<MessageElement> deployers =
			new ArrayList<MessageElement>();
		
		RNSPath current = RNSPath.getCurrent();
		RNSPath besPath = current.lookup(
			getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		GeniiCommon bes = ClientUtils.createProxy(
			GeniiCommon.class, besPath.getEndpoint());
		
		for (int lcv = 1; lcv < numArguments(); lcv++)
		{
			RNSPath target = current.lookup(
				getArgument(lcv), RNSPathQueryFlags.MUST_EXIST);
			deployers.add(new MessageElement(
				BESConstants.DEPLOYER_EPR_ATTR, target.getEndpoint()));
		}
		
		bes.updateResourceProperties(new UpdateResourceProperties(
			new UpdateType(deployers.toArray(new MessageElement[0]))));
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() <= 1)
			throw new InvalidToolUsageException();
	}
}