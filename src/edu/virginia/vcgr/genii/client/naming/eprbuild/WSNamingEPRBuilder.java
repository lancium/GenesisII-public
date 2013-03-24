package edu.virginia.vcgr.genii.client.naming.eprbuild;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;
import org.w3c.dom.Element;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;

public class WSNamingEPRBuilder extends GenericEPRBuilder
{
	private URI _epi = null;
	private Collection<EndpointReferenceType> _referenceResolvers = new LinkedList<EndpointReferenceType>();
	private Collection<EndpointReferenceType> _epiResolvers = new LinkedList<EndpointReferenceType>();

	public WSNamingEPRBuilder(URI address)
	{
		super(address);
	}

	final public void epi(URI uri)
	{
		_epi = uri;
	}

	final public URI epi()
	{
		return _epi;
	}

	final public void addReferenceResolvers(EndpointReferenceType... eprs)
	{
		for (EndpointReferenceType epr : eprs)
			_referenceResolvers.add(epr);
	}

	final public Collection<EndpointReferenceType> referenceResolvers()
	{
		return Collections.unmodifiableCollection(_referenceResolvers);
	}

	final public void addEndpointIdentifierResolvers(EndpointReferenceType... eprs)
	{
		for (EndpointReferenceType epr : eprs)
			_epiResolvers.add(epr);
	}

	final public Collection<EndpointReferenceType> endpointIdentifierResolvers()
	{
		return Collections.unmodifiableCollection(_epiResolvers);
	}

	@Override
	public Collection<Element> metadata()
	{
		Collection<Element> ret = new LinkedList<Element>(super.metadata());

		/* Add EPI */
		if (_epi != null)
			ret.add(new MessageElement(WSName.ENDPOINT_IDENTIFIER_QNAME, _epi.toString()));

		/* Add EPI Resolvers */
		if (!_epiResolvers.isEmpty()) {
			for (EndpointReferenceType epr : _epiResolvers) {
				try {
					ret.add(ObjectSerializer.toElement(epr, WSName.ENDPOINT_IDENTIFIER_RESOLVER_QNAME));
				} catch (ResourceException re) {
					throw new ConfigurationException("Error trying to serializer EPR!", re);
				}
			}
		}

		/* Add Reference Resolvers */
		if (!_referenceResolvers.isEmpty()) {
			for (EndpointReferenceType epr : _referenceResolvers)
				try {
					ret.add(ObjectSerializer.toElement(epr, WSName.REFERENCE_RESOLVER_QNAME));
				} catch (ResourceException re) {
					throw new ConfigurationException("Error trying to serializer EPR!", re);
				}
		}

		return ret;
	}
}
