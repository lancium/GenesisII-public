package edu.virginia.vcgr.genii.client.cmd.tools;

import java.rmi.RemoteException;
import java.util.HashSet;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ogf.ogsa.ticker.CreateTicker;
import org.ogf.ogsa.ticker.TickerFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSAQNameList;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.container.ticker.TickerConstants;

public class OGSAWSRFBPInteropTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Participates in the OGSA-WSRF-BP Interop Festival.";
	static private final String _USAGE_RESOURCE =
		"ogsa-bp-interop <ticker-factory-path> test1 ... test8";
	
	public OGSAWSRFBPInteropTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, true);
	}
	
	static private EndpointReferenceType lookupTickerFactory(String path)
		throws RNSException, ConfigurationException
	{
		return RNSPath.getCurrent().lookup(
			path, RNSPathQueryFlags.MUST_EXIST).getEndpoint();
	}
	
	static private TickerFactory createTicker(TickerFactory factory)
		throws RemoteException, ConfigurationException
	{
		EndpointReferenceType epr = factory.createTicker(new CreateTicker()).getTickerReference();
		return ClientUtils.createProxy(TickerFactory.class, epr);
	}
	
	static private void terminateTicker(TickerFactory ticker)
		throws RemoteException
	{
		ticker.destroy(new Destroy());
	}
	
	private void runTest1(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		MessageElement []any = factory.getResourceProperty(
			OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME).get_any();
		if (any == null)
			stderr.println("\t\tGetResourceProperty(" + 
				OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME + 
				") returned no properties.");
		else if (any.length != 1)
			stderr.println("\t\tGetResourceProperty(" + 
				OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME + 
				") did not return 1 property.");
		else
		{
			OGSAQNameList list = new OGSAQNameList(any[0]);
			
			HashSet<QName> expected = new HashSet<QName>();
			expected.add(OGSAWSRFBPConstants.CURRENT_TIME_ATTR_QNAME);
			expected.add(OGSAWSRFBPConstants.RESOURCE_ENDPOINT_REFERENCE_ATTR_QNAME);
			expected.add(new QName(TickerConstants.TICKER_NS, "Ticker"));
			expected.add(OGSAWSRFBPConstants.WS_FINAL_RESOURCE_INTERFACE_ATTR_QNAME);
			expected.add(OGSAWSRFBPConstants.TERMINATION_TIME_ATTR_QNAME);
			expected.add(OGSAWSRFBPConstants.WS_RESOURCE_INTERFACES_ATTR_QNAME);
			
			for (QName item : list)
			{
				expected.remove(item);
			}
			
			if (expected.size() == 0)
				stdout.println("\t\tGetResourceProperty("+ 
				OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME + 
				") worked!");
			else
			{
				stdout.println("\t\tThe following resource property names were not retrieved:");
				for (QName item : expected)
					stdout.println("\t\t\t" + item);
			}
		}
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	private void runTest2(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		// TODO
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	private void runTest3(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		// TODO
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	private void runTest4(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		// TODO
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	private void runTest5(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		// TODO
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	private void runTest6(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		// TODO
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	private void runTest7(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		// TODO
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	private void runTest8(TickerFactory factory) throws Throwable
	{
		stdout.print("\tCreating ticker...");
		TickerFactory ticker = createTicker(factory);
		stdout.println("Done");
		
		// TODO
		
		stdout.print("\tTerminating ticker...");
		terminateTicker(ticker);
		stdout.println("Done");
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		EndpointReferenceType tickerFactoryEPR = 
			lookupTickerFactory(getArgument(0));
		TickerFactory tickerFactory = ClientUtils.createProxy(
			TickerFactory.class, tickerFactoryEPR);
		
		for (int lcv = 1; lcv < numArguments(); lcv++)
		{
			String test = getArgument(lcv);
			try
			{
				stdout.println("Running test \"" + test + "\".");
				if (test.equals("test1"))
					runTest1(tickerFactory);
				else if (test.equals("test2"))
					runTest2(tickerFactory);
				else if (test.equals("test3"))
					runTest3(tickerFactory);
				else if (test.equals("test4"))
					runTest4(tickerFactory);
				else if (test.equals("test5"))
					runTest5(tickerFactory);
				else if (test.equals("test6"))
					runTest6(tickerFactory);
				else if (test.equals("test7"))
					runTest7(tickerFactory);
				else if (test.equals("test8"))
					runTest8(tickerFactory);
				else
				{
					stderr.println("Test \"" + test + "\" is not known.");
				}
			}
			catch (Throwable cause)
			{
				stderr.println("Test \"" + test + "\" threw exception.");
				cause.printStackTrace(stderr);
			}
			
			stdout.println();
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException("Too few arguments.");
	}
}
