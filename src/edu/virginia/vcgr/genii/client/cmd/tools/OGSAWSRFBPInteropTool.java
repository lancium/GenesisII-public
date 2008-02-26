package edu.virginia.vcgr.genii.client.cmd.tools;

import javax.xml.namespace.QName;

import org.ogf.ogsa.ticker.CreateTicker;
import org.ogf.ogsa.ticker.TickerFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSARP;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;

public class OGSAWSRFBPInteropTool extends BaseGridTool
{
	public OGSAWSRFBPInteropTool()
	{
		super("OGSA WSRF-BP Interop Tool", "ogsa-bp-interop", true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		/*
		TickerFactory factory = ClientUtils.createProxy(TickerFactory.class,
			EPRUtils.makeEPR("http://193.122.18.162:8080/ogsabp-interop/ticker-factory", false));
		*/
		/*
		TickerFactory factory = ClientUtils.createProxy(TickerFactory.class,
			EPRUtils.makeEPR("http://zam461.zam.kfa-juelich.de:9126/services/TickerFactoryService", false));
		*/
		TickerFactory factory = ClientUtils.createProxy(TickerFactory.class,
			EPRUtils.makeEPR("https://localhost:18080/axis/services/TickerFactory", false));
			
		EndpointReferenceType epr = factory.createTicker(new CreateTicker()).getTickerReference();
		OGSARP rp = (OGSARP)ResourcePropertyManager.createRPInterface(epr, OGSARP.class); 
		for (QName rpname : rp.getResourcePropertyNames())
		{
			System.err.println("ResourceProperty:  " + rpname);
		}
		
		/*
		TickerFactory ticker = ClientUtils.createProxy(TickerFactory.class, epr);
		
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		ObjectSerializer.serialize(writer,
			ticker.getResourceProperty(OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME),
			OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME);
		
		writer.flush();
		*/
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
}
