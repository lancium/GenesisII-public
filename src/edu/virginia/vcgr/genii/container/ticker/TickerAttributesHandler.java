package edu.virginia.vcgr.genii.container.ticker;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class TickerAttributesHandler 
	extends AbstractAttributeHandler implements TickerConstants
{
	public TickerAttributesHandler(AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(pkg);
	}
	
	private long getTicker() throws ResourceException, ResourceUnknownFaultType

	{
		IResource resource = null;
		resource = ResourceManager.getCurrentResource().dereference();
		
		if (resource.isServiceResource())
			throw FaultManipulator.fillInFault(
				new ResourceUnknownFaultType(null, null, null, null, 
					new BaseFaultTypeDescription[] { 
						new BaseFaultTypeDescription("Resource unknown.") },
					null));
		
		Long creation = (Long)resource.getProperty(TICKER_CREATION_PROPERTY);
		Long elapsed = System.currentTimeMillis() - creation.longValue();
		return elapsed / TICKER_RATE;
	}
	
	public MessageElement getTickerAttr()
		throws ResourceException, ResourceUnknownFaultType
	{
		return new MessageElement(
			new QName(TICKER_NS, "Ticker"), getTicker());
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(new QName(TICKER_NS, "Ticker"), "getTickerAttr");
	}
}