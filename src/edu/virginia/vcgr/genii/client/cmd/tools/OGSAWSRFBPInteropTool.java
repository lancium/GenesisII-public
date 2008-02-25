package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.OutputStreamWriter;

import org.ogf.ogsa.ticker.CreateTicker;
import org.ogf.ogsa.ticker.TickerFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.ogsa.OGSAWSRFBPConstants;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

public class OGSAWSRFBPInteropTool extends BaseGridTool
{
	public OGSAWSRFBPInteropTool()
	{
		super("OGSA WSRF-BP Interop Tool", "ogsa-bp-interop", true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		TickerFactory factory = ClientUtils.createProxy(TickerFactory.class,
			EPRUtils.makeEPR("http://193.122.18.162:8080/ogsabp-interop/ticker-factory", false));
		EndpointReferenceType epr = factory.createTicker(new CreateTicker()).getTickerReference();
		TickerFactory ticker = ClientUtils.createProxy(TickerFactory.class, epr);
		ticker.getResourceProperty(OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME);
		OutputStreamWriter writer = new OutputStreamWriter(System.out);
		ObjectSerializer.serialize(writer,
			ticker.getResourceProperty(OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME),
			OGSAWSRFBPConstants.RESOURCE_PROPERTY_NAMES_ATTR_QNAME);
		writer.flush();
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
}
