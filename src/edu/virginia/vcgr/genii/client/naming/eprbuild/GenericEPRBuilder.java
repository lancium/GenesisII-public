package edu.virginia.vcgr.genii.client.naming.eprbuild;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.ws.addressing.ReferenceParametersType;

public class GenericEPRBuilder implements EPRBuilder
{
	static private MessageElement[] convert(Collection<Element> list)
	{
		int lcv = 0;
		if (list == null || list.size() == 0)
			return null;

		MessageElement[] array = new MessageElement[list.size()];
		for (Element e : list) {
			MessageElement me;
			if (e instanceof MessageElement)
				me = (MessageElement) e;
			else
				me = new MessageElement(e);

			array[lcv++] = me;
		}

		return array;
	}

	private URI _address;
	private Collection<Element> _referenceParameters = new LinkedList<Element>();
	private Collection<Element> _metadata = new LinkedList<Element>();

	public GenericEPRBuilder(URI address)
	{
		address(address);
	}

	@Override
	final public URI address()
	{
		return _address;
	}

	@Override
	final public void address(URI address)
	{
		if (address == null)
			throw new IllegalArgumentException("Address cannot be null!");

		_address = address;
	}

	@Override
	final public void addReferenceParameters(Element... referenceParameters)
	{
		for (Element rp : referenceParameters)
			_referenceParameters.add(rp);
	}

	@Override
	public Collection<Element> referenceParameters()
	{
		return Collections.unmodifiableCollection(_referenceParameters);
	}

	@Override
	final public void addMetadata(Element... metadata)
	{
		for (Element m : metadata)
			_metadata.add(m);
	}

	@Override
	public Collection<Element> metadata()
	{
		return Collections.unmodifiableCollection(_metadata);
	}

	@Override
	final public EndpointReferenceType mint()
	{
		MessageElement[] rp = convert(referenceParameters());
		MessageElement[] m = convert(metadata());

		return new EndpointReferenceType(new AttributedURIType(_address.toString()), (rp == null) ? null
			: new ReferenceParametersType(rp), (m == null) ? null : new MetadataType(m), null);
	}
}