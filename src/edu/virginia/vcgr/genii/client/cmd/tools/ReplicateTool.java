package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.List;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.authz.acl.ResourceSecurityPolicy;

import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.resource.IResource;

public class ReplicateTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dreplicate";
	static final private String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/ureplicate";
	static final private String _MANPAGE = 
		"edu/virginia/vcgr/genii/client/cmd/tools/man/replicate";

	public ReplicateTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE),
				false, ToolCategory.ADMINISTRATION);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected void verify() throws ToolException
	{
		if ((numArguments() < 2) || (numArguments() > 3))
			throw new InvalidToolUsageException();
	}

	/**
	 * Create a replica of the given resource in the given container.
	 */
	@Override
	protected int runCommand() throws Throwable
	{
		String sourcePath = getArgument(0);
		String containerPath = getArgument(1);
		String linkPath = (numArguments() < 3 ? null : getArgument(2));
		RNSPath current = RNSPath.getCurrent();
		RNSPath sourceRNS = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType sourceEPR = sourceRNS.getEndpoint();
		WSName sourceName = new WSName(sourceEPR);
		URI endpointIdentifier = sourceName.getEndpointIdentifier();
		if (endpointIdentifier == null)
		{
			stdout.println("replicate error: " + sourceRNS + ": EndpointIdentifier not found");
			return(-1);
		}
		TypeInformation type = new TypeInformation(sourceEPR);
		List<ResolverDescription> resolverList = sourceName.getResolvers();
		if ((resolverList.size() == 0) &&  (!type.isEpiResolver()))
		{
			stdout.println("replication error: " + sourceRNS + ": Resource has no resolver element");
			return(-1);
		}
		String serviceName = type.getBestMatchServiceName();
		if (serviceName == null)
		{
			stdout.println("replicate: " + sourceRNS + ": Type does not support replication");
			return(-1);
		}
		String servicePath = containerPath + '/' + "Services" + '/' + serviceName;
		RNSPath serviceRNS = current.lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType serviceEPR = serviceRNS.getEndpoint();
		RNSPath linkRNS = null;
		if (linkPath != null)
		{
			linkRNS = current.lookup(linkPath, RNSPathQueryFlags.MUST_NOT_EXIST);
		}
		
		MessageElement[] elementArr = new MessageElement[2];
		elementArr[0] = new MessageElement(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM,
				endpointIdentifier);
		elementArr[1] = new MessageElement(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM,
				sourceEPR);
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, serviceEPR);
		VcgrCreate request = new VcgrCreate(elementArr);
		VcgrCreateResponse response = common.vcgrCreate(request);
		EndpointReferenceType newEPR = response.getEndpoint();
		if (linkRNS != null)
		{
			linkRNS.link(newEPR);
		}
		ResourceSecurityPolicy oldSP = new ResourceSecurityPolicy(sourceEPR);
		ResourceSecurityPolicy newSP = new ResourceSecurityPolicy(newEPR);
		newSP.copyFrom(oldSP);
		/*
		if (ADD_RESOURCES_TO_ACLS)
		{
			// Allow the new resource to modify the old resource, and vice versa,
			// even if the user did not delegate his identity to the resource.
			newSP.addResource(oldSP);
			oldSP.addResource(newSP);
			// Allow the new resource to update the resolver without user delegation.
			if (resolverList.size() > 0)
			{
				ResourceSecurityPolicy resolverSP = new ResourceSecurityPolicy(
						resolverList.get(0).getEPR());
				resolverSP.addResource(newSP);
			}
		}
		*/
		return 0;
	}
}
