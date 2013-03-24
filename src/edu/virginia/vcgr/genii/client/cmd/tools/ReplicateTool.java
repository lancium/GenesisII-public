package edu.virginia.vcgr.genii.client.cmd.tools;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.InsertType;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.security.axis.ResourceSecurityPolicy;

import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.rns.GeniiDirPolicy;
import edu.virginia.vcgr.genii.container.sync.SyncProperty;

public class ReplicateTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dreplicate";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/ureplicate";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/replicate";

	private boolean _policy;
	private boolean _destroy;

	public ReplicateTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.ADMINISTRATION);
		addManPage(new FileResource(_MANPAGE));
	}

	@Option({ "policy", "p" })
	public void setP()
	{
		_policy = true;
	}

	@Option({ "destroy" })
	public void setDestroy()
	{
		_destroy = true;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_destroy) {
			if (numArguments() != 1)
				throw new InvalidToolUsageException();
			return;
		}
		if ((numArguments() < 2) || (numArguments() > 3))
			throw new InvalidToolUsageException();
	}

	/**
	 * Create a replica of the given resource in the given container.
	 */
	@Override
	protected int runCommand() throws Throwable
	{
		if (_destroy) {
			return destroyReplica();
		}
		String sourcePath = getArgument(0);
		String containerPath = getArgument(1);
		String linkPath = (numArguments() < 3 ? null : getArgument(2));
		RNSPath current = RNSPath.getCurrent();
		RNSPath sourceRNS = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType sourceEPR = sourceRNS.getEndpoint();
		WSName sourceName = new WSName(sourceEPR);
		URI endpointIdentifier = sourceName.getEndpointIdentifier();
		if (endpointIdentifier == null) {
			stdout.println("replicate error: " + sourceRNS + ": EndpointIdentifier not found");
			return (-1);
		}
		List<ResolverDescription> resolverList = ResolverUtils.getResolvers(sourceName);
		if ((resolverList == null) || (resolverList.size() == 0)) {
			stdout.println("replication error: " + sourceRNS + ": Resource has no resolver element");
			return (-1);
		}
		TypeInformation type = new TypeInformation(sourceEPR);
		String serviceName = type.getBestMatchServiceName();
		if (serviceName == null) {
			stdout.println("replicate: " + sourceRNS + ": Type does not support replication");
			return (-1);
		}
		String servicePath = containerPath + '/' + "Services" + '/' + serviceName;
		RNSPath serviceRNS = current.lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType serviceEPR = serviceRNS.getEndpoint();
		RNSPath linkRNS = null;
		if (linkPath != null) {
			linkRNS = current.lookup(linkPath, RNSPathQueryFlags.MUST_NOT_EXIST);
		}
		// Setup existing resource tree before creating new resources.
		if (type.isRNS() && _policy) {
			Stack<RNSPath> stack = new Stack<RNSPath>();
			stack.push(sourceRNS);
			while (stack.size() > 0) {
				RNSPath currentRNS = stack.pop();
				addPolicy(currentRNS, stack);
			}
		}
		MessageElement[] elementArr = new MessageElement[2];
		elementArr[0] = new MessageElement(IResource.ENDPOINT_IDENTIFIER_CONSTRUCTION_PARAM, endpointIdentifier);
		elementArr[1] = new MessageElement(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM, sourceEPR);
		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, serviceEPR);
		VcgrCreate request = new VcgrCreate(elementArr);
		VcgrCreateResponse response = common.vcgrCreate(request);
		EndpointReferenceType newEPR = response.getEndpoint();
		if (linkRNS != null) {
			linkRNS.link(newEPR);
		}
		ResourceSecurityPolicy oldSP = new ResourceSecurityPolicy(sourceEPR);
		ResourceSecurityPolicy newSP = new ResourceSecurityPolicy(newEPR);
		newSP.copyFrom(oldSP);
		/*
		 * if (ADD_RESOURCES_TO_ACLS) { // Allow the new resource to modify the old resource, and
		 * vice versa, // even if the user did not delegate his identity to the resource.
		 * newSP.addResource(oldSP); oldSP.addResource(newSP); }
		 */
		return 0;
	}

	private void addPolicy(RNSPath currentRNS, Stack<RNSPath> stack) throws RemoteException, RNSException
	{
		stdout.println("addPolicy " + currentRNS);
		GeniiCommon dirService = ClientUtils.createProxy(GeniiCommon.class, currentRNS.getEndpoint());
		MessageElement[] elementArr = new MessageElement[1];
		elementArr[0] = new MessageElement(GeniiDirPolicy.REPLICATION_POLICY_QNAME, "true");
		InsertResourceProperties insertReq = new InsertResourceProperties(new InsertType(elementArr));
		dirService.insertResourceProperties(insertReq);

		Collection<RNSPath> contents = currentRNS.listContents();
		for (RNSPath child : contents) {
			TypeInformation type = new TypeInformation(child.getEndpoint());
			if (type.isRNS())
				stack.push(child);
		}
	}

	/**
	 * Destroy a single replica without destroying the entire virtual resource. (Note -- "rm file"
	 * destroys all replicas.)
	 * 
	 * Be careful! If you specify a pathname that refers to an EPR with a resolver element, then
	 * this may failover and destroy the wrong replica.
	 * 
	 * Ideally, specify a pathname of an EPR with no resolver.
	 * 
	 * Recursive destroy replica is not supported because of the risk of destroying replicas on
	 * other containers or unreplicated resources.
	 */
	private int destroyReplica() throws Throwable
	{
		String replicaPath = getArgument(0);
		RNSPath current = RNSPath.getCurrent();
		RNSPath replicaRNS = current.lookup(replicaPath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType replicaEPR = replicaRNS.getEndpoint();

		MessageElement[] elementArr = new MessageElement[1];
		elementArr[0] = new MessageElement(SyncProperty.UNLINKED_REPLICA_QNAME, "true");
		UpdateType update = new UpdateType(elementArr);
		UpdateResourceProperties request = new UpdateResourceProperties(update);

		GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, replicaEPR);
		common.updateResourceProperties(request);
		common.destroy(new Destroy());

		// Leave directory entry unchanged.
		// In the ideal case, maybe the directory entry should be replaced with an
		// equivalent entry with a different default address?
		// If the entry had no resolver element, or if this was the last replica,
		// then the entry should be removed?

		return 0;
	}
}
