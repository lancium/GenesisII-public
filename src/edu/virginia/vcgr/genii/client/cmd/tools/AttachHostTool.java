package edu.virginia.vcgr.genii.client.cmd.tools;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rattrs.GetAttributesResponse;

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
		
		GeniiCommon common = ClientUtils.createProxy(
			GeniiCommon.class, EPRUtils.makeEPR(containerURL));
		GetAttributesResponse resp = common.getAttributes(
			new QName[] {
				OGSAWSRFBPConstants.RESOURCE_ENDPOINT_REFERENCE_ATTR_QNAME
			});
		MessageElement []elements = resp.get_any();
		if (elements == null || elements.length < 1)
			throw new Exception("Couldn't get EPR for target container.");
		
		EndpointReferenceType epr = 
			(EndpointReferenceType)elements[0].getObjectValue(
				EndpointReferenceType.class);
		
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