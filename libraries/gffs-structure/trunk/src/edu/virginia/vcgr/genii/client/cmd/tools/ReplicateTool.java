package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.RNSEntryResponseType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.docs.wsrf.rp_2.UpdateResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.UpdateType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.GeniiDirPolicy;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.axis.ResourceSecurityPolicy;
import edu.virginia.vcgr.genii.client.sync.SyncProperty;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;

public class ReplicateTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dreplicate";
	static final private String _USAGE = "config/tooldocs/usage/ureplicate";
	static final private String _MANPAGE = "config/tooldocs/man/replicate";

	private boolean _policy;
	private boolean _destroy;
	private boolean _list;
	private boolean _clearResolver;

	public ReplicateTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.ADMINISTRATION);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "policy", "p" })
	public void setP()
	{
		_policy = true;
	}

	@Option({ "clear", "c" })
	public void setClear()
	{
		_clearResolver = true;
	}

	
	@Option({ "destroy", "d" })
	public void setDestroy()
	{
		_destroy = true;
	}

	
	@Option({ "list", "l" })
	public void setList()
	{
		_list = true;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_destroy ) {
			if (numArguments() != 2)
				throw new InvalidToolUsageException();
			return;
		}
		if (_clearResolver ) {
			if (numArguments() != 1)
				throw new InvalidToolUsageException();
			return;
		}

		if (_list) {
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
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		if (_destroy) {
			return destroyReplica();
		}
		if (_list)
			return listReplicas();
		if (_clearResolver) {
			return clearResolver();
		}
		String sourcePath = getArgument(0);
		String containerPath = getArgument(1);
		String linkPath = (numArguments() < 3 ? null : getArgument(2));
		RNSPath current = RNSPath.getCurrent();
		RNSPath sourceRNS = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType sourceEPR = sourceRNS.getEndpoint();
		// 2020-12-16 by ASG. Before we go on we MUST ensure that sourceEPR has 
		// an X.509 SecurityTokenReference in the metadata. If it does not, we MUST add one first to the sourceEPR,
		// AND make sure that we update the RNS entry.
		X509Certificate []cert = EPRUtils.extractCertChain(sourceEPR);
		if (cert==null) {
			// There is no X509, we will have to acquire one
			//sourceRNS=current.acquireX509(sourcePath);
			sourceEPR = sourceRNS.getEndpoint();
		}
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

		CacheManager.removeItemFromCache(sourceEPR, RNSConstants.ELEMENT_COUNT_QNAME, MessageElement.class);
		CacheManager.removeItemFromCache(newEPR, RNSConstants.ELEMENT_COUNT_QNAME, MessageElement.class);

		/*
		 * if (ADD_RESOURCES_TO_ACLS) { // Allow the new resource to modify the old resource, and vice versa, // even if the user did not
		 * delegate his identity to the resource. newSP.addResource(oldSP); oldSP.addResource(newSP); }
		 */
		return 0;
	}

	private void addPolicy(RNSPath currentRNS, Stack<RNSPath> stack) throws RemoteException, RNSException
	{
		stdout.println("addPolicy " + currentRNS);
		GeniiCommon dirService = ClientUtils.createProxy(GeniiCommon.class, currentRNS.getEndpoint());
		MessageElement[] elementArr = new MessageElement[1];
		elementArr[0] = new MessageElement(GeniiDirPolicy.REPLICATION_POLICY_QNAME, "true");
		UpdateResourceProperties request = new UpdateResourceProperties(new UpdateType(elementArr));
		dirService.updateResourceProperties(request);

		Collection<RNSPath> contents = currentRNS.listContents();
		for (RNSPath child : contents) {
			TypeInformation type = new TypeInformation(child.getEndpoint());
			if (type.isRNS())
				stack.push(child);
		}
	}
	/*
	 * Wipe out the resolver info. This is only useful when the Resolver container no longer exists so we cannot find out the replica's,
	 * nor destroy them. It may leave orphans out there.
	 */
	private int clearResolver() throws RNSException, AuthZSecurityException, ResourceException, ToolException
	{

		Collection<GeniiPath.PathMixIn> paths = GeniiPath.pathExpander(getArgument(0));
		if (paths == null) {
			String msg = "Path does not exist or is not accessible: " + getArgument(0);
			stdout.println(msg);
			return 1;
		}
		for (GeniiPath.PathMixIn gpath : paths) {
			String replicaPath=null;
			if (gpath._rns != null) {
				// Do what needs doing
				replicaPath = gpath._rns.pwd();
				RNSPath current = RNSPath.getCurrent();
				RNSPath replicaRNS = current.lookup(replicaPath, RNSPathQueryFlags.MUST_EXIST);
				EndpointReferenceType replicaEPR = replicaRNS.getEndpoint();
				// Ok, now we have the wsname. Now we need to do three things: remove the resolver from the SW name, and replace the link
				WSName sourceName = new WSName(replicaEPR);
				sourceName.removeAllResolvers();
				replicaRNS.unlink();
				replicaRNS.link(sourceName.getEndpoint());
			} else {
				stdout.println("Resource " + replicaPath + " does not exist");
			}
		}

	// End ASG updates

		return 0;
	}

	public static RNSEntryResponseType[] listReplicas(EndpointReferenceType replicaEPR, PrintWriter stdout) {
		int[] list = null;
		RNSEntryResponseType[] response=null;
		// First get the vector or replica numbers
		list = ResolverUtils.getEndpoints(replicaEPR);
		// Now look them all up and get their EPRs
		LookupResponseType dir = ResolverUtils.getEndpointEntries(replicaEPR);
		if (dir != null && list != null) {
			response = dir.getEntryResponse();
			for (int j = 0; j < response.length; j++) {
				String temp = response[j].getEndpoint().getAddress().toString();
				int axisIndex = temp.indexOf("/axis");
				int containerID = temp.indexOf("container-id");
				if (axisIndex >= 0 && containerID >= 0)
					stdout.println("Replica " + list[j] + ": " + temp.substring(0, axisIndex) + ": " + temp.substring(containerID));
				WSName name= new WSName(response[j].getEndpoint());
				List<ResolverDescription> resolvers=name.getResolvers();
				if (resolvers.size() <=0) stdout.println("\tThe EPR returned had no resolvers.");
				for (ResolverDescription tR : resolvers) {
					stdout.println("\tResolver EPR address is " + tR.getEPR().getAddress().toString());
				}
				
			}
		} else {
			stdout.println("There are no replicas of resource");
		}
		return response;
	}
	
	public static EndpointReferenceType replicaPicker(EndpointReferenceType replicaEPR, PrintWriter stdout, BufferedReader stdin) {
		int result=-1;
		// List replicas and get back a vector of entries with EPRs
		RNSEntryResponseType[] replicants=listReplicas(replicaEPR, stdout);
		// If there were none, exit
		if (replicants==null || replicants.length<2) return null;
		stdout.println("Select replica in the range 0.." + (replicants.length-1));
		try {
			String inS;
			inS=stdin.readLine();
			if (inS.equalsIgnoreCase("q")) return null;
			result=Integer.parseInt(inS);
			stdout.println("You typed " + result);
			if (result <0 || result > replicants.length-1) {
				stdout.println("Selection out of range!");
				return null;
			}
			return replicants[result].getEndpoint();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			result=-1;
		}
		return null;
	
	}
	/*
	 * Print the list of replicas to the console.
	 */
	private int listReplicas() throws RNSException, AuthZSecurityException, ResourceException, ToolException
	{

		/*
		 * 2016-06-03 by ASG Updated to allow wildcards in arguments. Got tired of not having the capability
		 */
		Collection<GeniiPath.PathMixIn> paths = GeniiPath.pathExpander(getArgument(0));
		if (paths == null) {
			String msg = "Path does not exist or is not accessible: " + getArgument(0);
			stdout.println(msg);
			return 1;
		}
		for (GeniiPath.PathMixIn gpath : paths) {
			if (gpath._rns != null) {
				// Do what needs doing
				String replicaPath = gpath._rns.pwd();
				RNSPath current = RNSPath.getCurrent();
				RNSPath replicaRNS = current.lookup(replicaPath, RNSPathQueryFlags.MUST_EXIST);
				EndpointReferenceType replicaEPR = replicaRNS.getEndpoint();
				listReplicas(replicaEPR, stdout);
			}
		}
		// End ASG updates

		return 0;
	}

	/**
	 * Destroy a single replica without destroying the entire virtual resource. (Note -- "rm file" destroys all replicas.)
	 * 
	 * Be careful! If you specify a pathname that refers to an EPR with a resolver element, then this may failover and destroy the wrong
	 * replica.
	 * 
	 * Ideally, specify a pathname of an EPR with no resolver.
	 * 
	 * Recursive destroy replica is not supported because of the risk of destroying replicas on other containers or unreplicated resources.
	 * 
	 * @throws ToolException
	 */
	private int destroyReplica() throws RNSException, AuthZSecurityException, ResourceException, ToolException
	{
		String replicaPath = getArgument(0);
		int replicaNum = Integer.parseInt(getArgument(1));
		Boolean sameEPR = false;
		RNSPath current = RNSPath.getCurrent();
		RNSPath replicaRNS = current.lookup(replicaPath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType replicaEPR = replicaRNS.getEndpoint();
		// Now get the replica vector of replica numbers
		int[] list = null;
		// First get the vector or replica numbers
		list = ResolverUtils.getEndpoints(replicaEPR);
		// If there is only one copy, don't allow replica remove
		if (list.length == 1) {
			stdout.println("Only one copy of " + replicaPath + ". Use rm to delete it.");
			return 1;
		}
		// Now look them all up and get their EPRs
		LookupResponseType dir = ResolverUtils.getEndpointEntries(replicaEPR);
		if (dir != null && list != null) {
			// Now find the replica
			int index = -1;
			for (int j = 0; j < list.length; j++) {
				if (list[j] == replicaNum) {
					index = j;
					break;
				}
			}
			if (index >= 0) {
				/*
				 * stdout.println(":replicaEPR data:\n" + replicaEPR.getAddress().toString()+"\n" +
				 * replicaEPR.getReferenceParameters().get_any()[0].toString()); stdout.println(":selected entry data:\n" +
				 * dir.getEntryResponse(index).getEndpoint().getAddress().toString()+"\n" + dir.getEntryResponse
				 * (index).getEndpoint().getReferenceParameters().get_any()[0].toString());
				 */
				// To determine if the two replica instances are the same we check if their
				// container address and resource key are the same.
				// The EPR equals operator does not do it correctly.
				sameEPR = replicaEPR.getAddress().toString().compareTo(dir.getEntryResponse(index).getEndpoint().getAddress().toString()) == 0
					&& replicaEPR.getReferenceParameters().get_any()[0].toString()
						.compareTo(dir.getEntryResponse(index).getEndpoint().getReferenceParameters().get_any()[0].toString()) == 0;

				if (sameEPR) {
					// 2014-10-04 ASG. I had code to pick a different EPR, but the list of EPR's I
					// got back did not have resolvers embedded in them.
					// So instead i am going to call ResolverUtils.resolve(EPR) and let the server
					// pick one for me because it will properly embed
					// the resolver info in the EPR. I could alternatively, construct my own using
					// some notion of closeness, but i will not.
					// We must consider what might happen if the operation fails in the middle. If
					// we simply unlink the old and link in the new,
					// if a failure occurs after unlinking, and before linking, we could loose the
					// reference to the resource. Soooo, instead we
					// fist create a new link with the old, soon-to-be-removed-epr, then unlink,
					// link the new, unlink the old.
					RNSPath tempLink = current.lookup(replicaPath + "-warning-removal-replica-failed", RNSPathQueryFlags.MUST_NOT_EXIST);
					try {
						EndpointReferenceType replacementEPR = ResolverUtils.resolve(replicaEPR);
						tempLink.link(replicaEPR); // We will unlink this in just a moment, just
													// don't want to loose it.
						replicaRNS.unlink(); // unlink the old entry
						replicaRNS.link(replacementEPR);// link in the new
						// now when we destroy the replicaEPR we will not be removing the copy
						// pointed to by the directory entry
						// Next we unlink the temporary link
						tempLink.unlink();
					} catch (Throwable e) {
						stdout.println("Failed to get a new resolution EPR, this should NEVER happen.");
						throw new ToolException("Failure removing replicant: " + e.getLocalizedMessage(), e);
					}
				} else
					replicaEPR = dir.getEntryResponse(index).getEndpoint();
			} else {
				stdout.println(replicaNum + " is out of range");
				return 1;
			}
		} else {
			stdout.println("There are no replicas of " + replicaPath);
			return 1;
		}
		// Now destroy the replicant
		MessageElement[] elementArr = new MessageElement[1];
		elementArr[0] = new MessageElement(SyncProperty.UNLINKED_REPLICA_QNAME, "true");
		UpdateType update = new UpdateType(elementArr);
		UpdateResourceProperties request = new UpdateResourceProperties(update);

		try {
			GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, replicaEPR);
			common.updateResourceProperties(request);
			common.destroy(new Destroy());
		} catch (Throwable e) {
			throw new ToolException("Could no destroy the replicant: " + e.getLocalizedMessage(), e);
		}
		return 0;
	}
}
