package edu.virginia.vcgr.genii.client.cmd.tools;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesDocumentResponse;

public class GetAttributesDocumentTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Retrieves and prints the attribute document for a target.";
	static final private String _USAGE =
		"get-attributes <target>";
	
	public GetAttributesDocumentTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath path = RNSPath.getCurrent();
		path = path.lookup(getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class,
			path.getEndpoint());
		GetAttributesDocumentResponse resp = common.getAttributesDocument(null);
		MessageElement document = new MessageElement(
			new QName(GenesisIIConstants.GENESISII_NS, "attributes"));
		for (MessageElement child : resp.get_any())
		{
			document.addChild(child);
		}
		
		stdout.println(document);
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}