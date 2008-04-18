package edu.virginia.vcgr.genii.client.cmd.tools;
/*
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import javax.xml.namespace.QName;
*/
import org.ws.addressing.EndpointReferenceType;

//import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
//import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
//import edu.virginia.vcgr.genii.client.naming.WSName;
//import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.common.GeniiCommon;
//import edu.virginia.vcgr.genii.resolver.simple.SimpleResolverPortType;

public class ExportPerformanceTest extends BaseGridTool 
{
	static private final String _DESCRIPTION = "Whatever";
	static private final String _USAGE =
		"time-file [--new] [--kill] <local-file> <target-base-name> <host0>...<hostn>";
	
	//private boolean _kill = false;
	private boolean _new = false;
	
	public ExportPerformanceTest()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	
	public void setNew()
	{
		_new = true;
	}
	
	@Override
	protected int runCommand() throws Throwable 
	{
		
		if (_new){
			String []containers = getArguments().subList(
					2, getArguments().size()).toArray(
						new String[0]);
			
			//replicate file and add resolvers to file epr
			ReplicatedFileTool.makeReplicatedFile(
				getArgument(0), true, getArgument(1), 
				containers);
		}
		else {
			pingWarmUp();
			
			long result = -1;
			for (int lcv = 0; lcv < 1; lcv++){ 
				result = pingTest();
			}
			stdout.println("Elapsed millis for 1 ping:  " + 
					result);
			
			attrWarmUp();
			result = -1;
			for (int lcv = 0; lcv < 1; lcv++){ 
				result = attrTest();
			}
			stdout.println("Elapsed nano for 1 get-attrs:  " + 
					result);
		}
/*		
		//get rns path and proxy for first replica
		RNSPath replica1Path = RNSPath.getCurrent().lookup(
				getArgument(1) + "-Replicas/Copy_1_on_" + getArgument(2), RNSPathQueryFlags.MUST_EXIST);
		GeniiCommon replica1Replica = ClientUtils.createProxy(GeniiCommon.class, 
				replica1Path.getEndpoint());

		
		//delete first replica
		CommandLineRunner runner = new CommandLineRunner();
		runner.runCommand(new String[] {
			"rm", getArgument(1) + "-Replicas/Copy_1_on_" + getArgument(2)
		}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
		
		
		//time 100 'get attributes' for primary (why again - is resolution happening here?)
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 1000; lcv++)
		{
			try{
				GeniiCommon proxy2 = ClientUtils.createProxy(GeniiCommon.class, 
						path.getEndpoint());
				proxy2.getAttributes(new QName[] {
				GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME
			});
			}
			catch(Throwable t)
			{stdout.print("Primary time errors: " + t.toString());}
			
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Elapsed millis for rep. File (again):  " + 
			(stopTime - startTime));
		
		//time 100 'get attributes' for replica1
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 1000; lcv++)
		{
			try
			{
				replica1Replica.getResourceProperty(
						OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME
				);
			}
			catch(Throwable t)
			{stdout.print("Fail replica1 call errors: " + t.toString());}
		}
		stopTime = System.currentTimeMillis();
		
		//where does resolve call occur?
		stdout.println("Elapsed millis for failed call to deleted replica (resolve + failed call):  " + 
			(stopTime - startTime));
		
		// time for creating proxies
		//get resolver1 epr
		//WSName replicatedFileWSName = new WSName(path.getEndpoint());
		WSName replicatedFileWSName = new WSName(primaryEPR);
		//EndpointReferenceType resolverEPR = replicatedFileWSName.getResolvers().get(0).getEPR();
		List<ResolverDescription> resolvers = replicatedFileWSName.getResolvers();
		EndpointReferenceType resolverEPR = null;
		for (ResolverDescription nextResolver : resolvers){
			resolverEPR = nextResolver.getEPR();
		}
		
		//time creation of proxy 100 times to resolver1
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			try {
				SimpleResolverPortType resolver = ClientUtils.createProxy(SimpleResolverPortType.class, 
					resolverEPR);
				if (resolver == null) 
					stdout.println("Create resolver proxy failed"); 
			}
			catch(Throwable t)
			{stdout.print("Resolver1 proxy call errors: " + t.toString());}
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Time for 100 create proxy calls to resolver1:  " + 
			(stopTime - startTime));

		// time pure resolve calls.
		//SimpleResolverPortType resolver = ClientUtils.createProxy(SimpleResolverPortType.class, 
		//		resolverEPR);
		
		SimpleResolverPortType resolver = ClientUtils.createProxy(SimpleResolverPortType.class, 
				resolverEPR);
		
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			try{
				EndpointReferenceType resolvedEPR = resolver.resolve(null);
				if (resolvedEPR == null)
					stdout.println("Got a bad resolved EPR back"); 
			}
			catch(Throwable t)
			{stdout.print("Resolver1 resolve call errors: " + t.toString());}
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Time for 100 resolves via resolver1:  " + 
			(stopTime - startTime));
		
		
		//get attributes given resolved epr
		EndpointReferenceType resolvedEPR = resolver.resolve(null);
		if (resolvedEPR == null)
			stdout.println("Got a bad resolved EPR back"); 
		startTime = System.currentTimeMillis();
		for (int lcv = 0; lcv < 100; lcv++)
		{
			try
			{
				GeniiCommon resolvedEPRproxy = ClientUtils.createProxy(GeniiCommon.class, 
						resolvedEPR);
				GetAttributesResponse attrResp = resolvedEPRproxy.getAttributes(new QName[] {
						GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME
				});
				attrResp.toString();
			}
			catch(Throwable t)
			{stdout.print("Fail resolvedEPR call errors: " + t.toString());}
		}
		stopTime = System.currentTimeMillis();
		stdout.println("Time for 100 getAttrs for resolvedEPR:  " + 
			(stopTime - startTime));
	
		stdout.println("Finished with test; cleaning state");
		
		if (_kill)
		{
//		run test
		CommandLineRunner testRunner = new CommandLineRunner();
		testRunner.runCommand(new String[] {
			"unlink", getArgument(1)
		}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
		
		testRunner.runCommand(new String[] {
				"rm", getArgument(1) + "-Replicas/Copy_1_on_" + getArgument(2)
			}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
			
		testRunner.runCommand(new String[] {
				"rm", getArgument(1) + "-Replicas/Copy_2_on_" + getArgument(3)
			}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
			
		
		testRunner.runCommand(new String[] {
				"rm", getArgument(1) + "-Replicas"
			}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
		
		}
		
		
		int i = 0;
		for (i = 0; i < 10; i++)
		{
			
			testRunner.runCommand(new String[] {
					"time-file",  "C:\\\\Test\\\\new.txt new0 copy"
				}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)))
				;
			testRunner.runCommand(new String[] {
				"unlink", getArgument(1)
			}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
			testRunner.runCommand(new String[] {
					"rm", getArgument(1) + "-Replicas"
				}, System.out, System.err, new BufferedReader(new InputStreamReader(System.in)));
			
			
		}
		*/
		
		return 0;
	}

	void pingWarmUp()throws Throwable {
		RNSPath path = RNSPath.getCurrent().lookup(
			"warmUper", RNSPathQueryFlags.MUST_EXIST);
		
		GeniiCommon proxy = ClientUtils.createProxy(GeniiCommon.class, 
			path.getEndpoint());
		
		for (int lcv = 0; lcv < 100; lcv++){
			try{
				proxy.ping("a");
			}
			catch(Throwable t){
				stdout.print("Ping warmup errors: " + t.toString());
			}
		}
		stdout.println("Ping warmup finished");
	}
	
	void attrWarmUp()throws Throwable {
		//RNSPath path = RNSPath.getCurrent().lookup(
		//	"warmUper", RNSPathQueryFlags.MUST_EXIST);
		/*
		GeniiCommon proxy = ClientUtils.createProxy(GeniiCommon.class, 
			path.getEndpoint());
	
		for (int lcv = 0; lcv < 100; lcv++){
			try{
				proxy.getAttributes(new QName[] {
						GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME
				});
			}
			catch(Throwable t){
				stdout.print("Attr warmup errors: " + t.toString());
			}
		}
		*/
		stdout.println("Attr warmup finished");
	}
	
	
	
	long pingTest()throws Throwable {
//		get rns path to replicated file
		RNSPath path = RNSPath.getCurrent().lookup(
			getArgument(1), RNSPathQueryFlags.MUST_EXIST);
		
		EndpointReferenceType thisEPR = path.getEndpoint();
		long startTime0 = System.currentTimeMillis();
			try{
				//get proxy to replicated file
				GeniiCommon proxy1 = ClientUtils.createProxy(GeniiCommon.class, 
					thisEPR);
				proxy1.ping("a");}
			catch(Throwable t){
				stdout.print("ping errors: " + t.toString());
			}
			long stopTime0 = System.currentTimeMillis();
		return (stopTime0 - startTime0);
		
	}
	
	
	long attrTest()throws Throwable {
//		get rns path to replicated file
		//RNSPath path = RNSPath.getCurrent().lookup(
		//	getArgument(1), RNSPathQueryFlags.MUST_EXIST);
		
		//EndpointReferenceType thisEPR = path.getEndpoint();
		long startTime0 = System.nanoTime();
		/*	
		try{
				//get proxy to replicated file
				GeniiCommon proxy1 = ClientUtils.createProxy(GeniiCommon.class, 
					thisEPR);
				proxy1.getAttributes(new QName[] {
					GenesisIIConstants.IMPLEMENTED_PORT_TYPES_ATTR_QNAME
			});}
			catch(Throwable t)
			{stdout.print("get-attr errors: " + t.toString());}
		*/	
		long stopTime0 = System.nanoTime();
		return (stopTime0 - startTime0);
		
	}
	
	@Override
	protected void verify() throws ToolException 
	{
	}
}