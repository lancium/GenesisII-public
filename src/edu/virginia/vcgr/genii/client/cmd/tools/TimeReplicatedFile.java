package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.xml.namespace.QName;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.resolver.simple.SimpleResolverPortType;

public class TimeReplicatedFile extends BaseGridTool 
{
	static private final String _DESCRIPTION = "Whatever";
	static private final String _USAGE =
		"time-file <local-file> <target-base-name> <host0>...<hostn>";
	
	public TimeReplicatedFile()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable 
	{
		String []containers = getArguments().subList(
			2, getArguments().size()).toArray(
				new String[0]);
		
		ReplicatedFileTool.makeReplicatedFile(
			getArgument(0), true, getArgument(1), 
			containers);
		
		RNSPath path = RNSPath.getCurrent().lookup(
			getArgument(1), RNSPathQueryFlags.MUST_EXIST);
		
		GeniiCommon proxy = ClientUtils.createProxy(GeniiCommon.class, 
			path.getEndpoint());
		long startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			proxy.getAttributes(new QName[] {
				GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME
			});
		}
		long stopTime = System.currentTimeMillis();
		stdout.println("Elapsed millis for originalFile:  " + 
			(stopTime - startTime));
		
		RNSPath replica1Path = RNSPath.getCurrent().lookup(
				getArgument(1) + "-Replicas/Copy_1_on_" + getArgument(2), RNSPathQueryFlags.MUST_EXIST);
		GeniiCommon replica1Replica = ClientUtils.createProxy(GeniiCommon.class, 
				replica1Path.getEndpoint());

		CommandLineRunner runner = new CommandLineRunner();
		runner.runCommand(new String[] {
			"rm", getArgument(1) + "-Replicas/Copy_1_on_" + getArgument(2)
		}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
		
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			proxy.getAttributes(new QName[] {
				GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME
			});
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Elapsed millis for rep. File:  " + 
			(stopTime - startTime));
		
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			try
			{
				replica1Replica.getAttributes(new QName[] {
						GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME
				});
			}
			catch(Throwable t)
			{}
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Elapsed millis for failed call to deleted replica (resolve + failed call):  " + 
			(stopTime - startTime));
			
		// time for creating proxies.
		WSName replicatedFileWSName = new WSName(path.getEndpoint());
		EndpointReferenceType resolverEPR = replicatedFileWSName.getResolvers().get(0).getEPR();
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			SimpleResolverPortType resolver = ClientUtils.createProxy(SimpleResolverPortType.class, 
					resolverEPR);
			if (resolver == null)
				stdout.println("Tcreate resolver proxy failed"); 
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Time for 100 create proxy calls:  " + 
			(stopTime - startTime));

		// time pure resolve calls.
		SimpleResolverPortType resolver = ClientUtils.createProxy(SimpleResolverPortType.class, 
				resolverEPR);
		
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			EndpointReferenceType resolvedEPR = resolver.resolve(null);
			if (resolvedEPR == null)
				stdout.println("Got a bad resolved EPR back"); 
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Time for 100 resolves:  " + 
			(stopTime - startTime));

		
		return 0;
	}

	@Override
	protected void verify() throws ToolException 
	{
	}
}