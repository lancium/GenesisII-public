package edu.virginia.vcgr.genii.container.resource;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.common.resource.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class StringResourceKeyTranslater implements IResourceKeyTranslater
{
	static private final String _RESOURCE_KEY_NAME = "simple-string";
	static private QName _RESOURCE_KEY_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, _RESOURCE_KEY_NAME);
	
	public Object unwrap(ReferenceParametersType refParams)
			throws ResourceUnknownFaultType
	{
		if (refParams == null)
			return null;
		
		MessageElement []elements = refParams.get_any();
		if (elements == null || elements.length == 0)
			return null;
		
		if (elements.length != 1)
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType());
		
		try
		{
			MessageElement element = elements[0];
			
			QName name = element.getQName();
			if (!name.equals(getRefParamQName()))
				throw FaultManipulator.fillInFault(new ResourceUnknownFaultType());
			return fromString(element.getFirstChild().getNodeValue());
		}
		catch (ResourceException re)
		{
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(
				null, null, null, null, new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription(re.getLocalizedMessage())
				}, null));
		}
	}

	public ReferenceParametersType wrap(Object key) throws ResourceException
	{
		QName qname = getRefParamQName();
		
		if (key == null)
			return null;
		
		try
		{
			MessageElement []any = new MessageElement[]
              {
					new MessageElement(
						qname.getNamespaceURI(),
						qname.getLocalPart())
              };
			any[0].addTextNode(toString(key));
			return new ReferenceParametersType(any);
		}
		catch (SOAPException se)
		{
			throw new RuntimeException("This shouldn't have happened.", se);
		}
	}
	
	protected QName getRefParamQName()
	{
		return _RESOURCE_KEY_QNAME;
	}
	
	protected String toString(Object key) throws ResourceException
	{
		return (String)key;
	}
	
	protected Object fromString(String sKey) throws ResourceException
	{
		return sKey;
	}
}