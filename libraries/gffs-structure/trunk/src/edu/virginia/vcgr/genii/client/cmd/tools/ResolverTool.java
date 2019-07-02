package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.rp_2.InsertResourceProperties;
import org.oasis_open.docs.wsrf.rp_2.InsertType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cache.unified.CacheManager;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.GeniiDirPolicy;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.resolver.GeniiResolverPortType;
import edu.virginia.vcgr.genii.resolver.UpdateResponseType;

public class ResolverTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dresolver";
	static final private String _USAGE = "config/tooldocs/usage/uresolver";
	static final private String _MANPAGE = "config/tooldocs/man/resolver";

	private boolean _query = false;
	private boolean _recursive = false;
	private String _link = null;
	private boolean _policy = false;

	public ResolverTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.ADMINISTRATION);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "query", "q" })
	public void setQ()
	{
		_query = true;
	}

	@Option({ "link", "l" })
	public void setL(String link)
	{
		_link = link;
	}

	@Option({ "recursive", "r" })
	public void setR()
	{
		_recursive = true;
	}

	@Option({ "policy", "p" })
	public void setP()
	{
		_policy = true;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_query) {
			if ((numArguments() != 1) || _policy || _recursive)
				throw new InvalidToolUsageException();
		} else {
			if ((numArguments() != 2) || (_link != null))
				throw new InvalidToolUsageException();
		}
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
		IOException, ResourcePropertyException
	{
		if (_query)
			return runQuery();

		RNSPath current = RNSPath.getCurrent();
		String sourcePath = getArgument(0);
		RNSPath sourceRNS = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);

		String targetPath = getArgument(1);
		RNSPath targetRNS = current.lookup(targetPath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType targetEPR = targetRNS.getEndpoint();
		TypeInformation targetType = new TypeInformation(targetEPR);
		EndpointReferenceType resolverEPR = null;
		if (targetType.isEpiResolver()) {
			resolverEPR = targetEPR;
		} else if (targetType.isContainer()) {
			String servicePath = targetPath + "/Services/GeniiResolverPortType";
			RNSPath serviceRNS = current.lookup(servicePath, RNSPathQueryFlags.MUST_EXIST);
			EndpointReferenceType serviceEPR = serviceRNS.getEndpoint();
			MessageElement[] params = new MessageElement[0];
			GeniiResolverPortType resolverService = ClientUtils.createProxy(GeniiResolverPortType.class, serviceEPR);
			VcgrCreateResponse response = resolverService.vcgrCreate(new VcgrCreate(params));
			resolverEPR = response.getEndpoint();
			stdout.println("ResolverTool: Created resolver resource");
		} else {
			stdout.println("ResolverTool: Failed to find or create resolver at " + targetPath);
			return (-1);
		}
		Stack<RNSPath> stack = new Stack<RNSPath>();
		stack.push(sourceRNS);
		while (stack.size() > 0) {
			sourceRNS = stack.pop();
			addResolver(sourceRNS, resolverEPR, stack);
		}
		return 0;
	}

	private void addResolver(RNSPath sourceRNS, EndpointReferenceType resolverEPR, Stack<RNSPath> stack) throws IOException, RNSException
	{
		EndpointReferenceType sourceEPR = sourceRNS.getEndpoint();
		WSName sourceName = new WSName(sourceEPR);
		if (!sourceName.isValidWSName()) {
			stdout.println(sourceRNS + ": no EPI");
			return;
		}
		if (sourceName.hasValidResolver()) {
			stdout.println(sourceRNS + ": already has resolver");
			stdout.println("Do you want to replace existing resolver? (y/n)  Note: Resolver will be replaced in EPR, BUT, the existing resolver will NOT be notified!");
			String inp = stdin.readLine();
			if (!inp.equalsIgnoreCase("y")) {
				stdout.println("Exiting without updating EPR");
				return;
			}
			sourceName.removeAllResolvers();
			sourceEPR=sourceName.getEndpoint();
			stdout.println("Resolver replaced for " + sourceRNS);
		}
		UpdateResponseType response = ResolverUtils.updateResolver(resolverEPR, sourceEPR);
		EndpointReferenceType finalEPR = response.getNew_EPR();
		TypeInformation type = new TypeInformation(sourceEPR);
		if (type.isRNS() && _policy) {
			GeniiCommon dirService = ClientUtils.createProxy(GeniiCommon.class, sourceEPR);
			MessageElement[] elementArr = new MessageElement[1];
			elementArr[0] = new MessageElement(GeniiDirPolicy.RESOLVER_POLICY_QNAME, resolverEPR);
			InsertResourceProperties insertReq = new InsertResourceProperties(new InsertType(elementArr));
			dirService.insertResourceProperties(insertReq);
		}
		CacheManager.removeItemFromCache(sourceRNS.pwd(), EndpointReferenceType.class);
		CacheManager.putItemInCache(sourceRNS.pwd(), finalEPR);
		if (sourceRNS.isRoot()) {
			stdout.println("Added resolver to root directory.");
			/*
			 * Store the new EPR in the client's calling context, so this client will see a root directory with a resolver element. Using the
			 * new EPR, the root directory can be replicated, and failover will work. Other existing clients will continue using the old root
			 * EPR, which still works as the root directory, but it does not support replication or failover.
			 */
			RNSPath rootPath = new RNSPath(finalEPR);
			String pwd = RNSPath.getCurrent().pwd();
			RNSPath currentPath = rootPath.lookup(pwd, RNSPathQueryFlags.MUST_EXIST);
			ICallingContext ctxt = ContextManager.getExistingContext();
			ctxt.setCurrentPath(currentPath);
			ContextManager.storeCurrentContext(ctxt);
		} else {
			sourceRNS.unlink();
			sourceRNS.link(finalEPR);
		}
		if (type.isRNS() && _recursive) {
			Collection<RNSPath> contents = sourceRNS.listContents();
			for (RNSPath child : contents) {
				stack.push(child);
			}
		}
	}

	private int runQuery() throws RNSException, ResourceException, GenesisIISecurityException, ToolException
	{
		String sourcePath = getArgument(0);
		RNSPath current = RNSPath.getCurrent();
		RNSPath sourceRNS = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);
		EndpointReferenceType sourceEPR = sourceRNS.getEndpoint();
		WSName sourceName = new WSName(sourceEPR);
		List<ResolverDescription> resolvers = sourceName.getResolvers();
		boolean inProgress = false;
		for (ResolverDescription resolver : resolvers) {
			if (inProgress)
				stdout.println();
			stdout.println("resolver: " + resolver.getEPR().getAddress());
			EndpointReferenceType epr;
			try {
				epr = ResolverUtils.resolve(resolver);
			} catch (Throwable e) {
				throw new ToolException("resolve method failed: " + e.getLocalizedMessage(), e);
			}
			stdout.println("address: " + epr.getAddress());
			AddressingParameters aps = new AddressingParameters(epr.getReferenceParameters());
			stdout.println("resource-key: " + aps.getResourceKey());
			WSName wsname = new WSName(epr);
			stdout.println("endpointIdentifier: " + wsname.getEndpointIdentifier());
			List<ResolverDescription> resolvers2 = wsname.getResolvers();
			if (resolvers2 != null) {
				for (ResolverDescription resolver2 : resolvers2) {
					stdout.println("resolver: " + resolver2.getEPR().getAddress());
				}
			}
			if ((!inProgress) && (_link != null)) {
				RNSPath linkPath = current.lookup(_link, RNSPathQueryFlags.MUST_NOT_EXIST);
				linkPath.link(epr);
			}
			inProgress = true;
		}
		return 0;
	}
}
