/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.client.naming;

import org.apache.axis.types.URI;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.MetadataType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.naming.ResolverDescription.ResolverType;

public class WSName implements Comparable<WSName>, Serializable
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(WSName.class);
	
	static public final String NAMING_NS =
		"http://schemas.ggf.org/naming/2006/03/naming";
	static public final String ENDPOINT_IDENTIFIER_LNAME = 
		"EndpointIdentifier";
	static public QName ENDPOINT_IDENTIFIER_QNAME =
		new QName(NAMING_NS, ENDPOINT_IDENTIFIER_LNAME);
	static public String REFERENCE_RESOLVER_LNAME = "ReferenceResolver";
	static public QName REFERENCE_RESOLVER_QNAME = new QName(NAMING_NS, REFERENCE_RESOLVER_LNAME);
	static public String ENDPOINT_IDENTIFIER_RESOLVER_LNAME = "EndpointIdentifierResolver";
	static public QName ENDPOINT_IDENTIFIER_RESOLVER_QNAME = new QName(NAMING_NS, ENDPOINT_IDENTIFIER_RESOLVER_LNAME);
	static public final String UNBOUND_ADDRESS = "http://10.0.0.1/UNBOUND_ADDRESS";

	private EndpointReferenceType _epr;
	private boolean _triedExtraction = false;
	private URI _endpointIdentifier = null;
	private List<ResolverDescription> _resolvers = new ArrayList<ResolverDescription>();
	
	public WSName(EndpointReferenceType epr)
	{
		_epr = epr;
	}
	
	public EndpointReferenceType getEndpoint()
	{
		return _epr;
	}
	
	public URI getEndpointIdentifier()
	{
		doExtraction();
		return _endpointIdentifier;
	}
	
	public List<ResolverDescription> getResolvers()
	{
		doExtraction();
		return _resolvers;
	}
	
	public boolean equals(WSName other)
	{
		URI me = getEndpointIdentifier();
		if (me == null)
			return false;
		
		URI you = other.getEndpointIdentifier();
		if (you == null)
			return false;
		
		return me.equals(you);
	}
	
	public boolean equals(Object other)
	{
		if (!(other instanceof WSName))
			return false;
		
		return equals((WSName)other);
	}
	
	public int hashCode()
	{
		URI me = getEndpointIdentifier();
		if (me == null)
			return 0;
		
		return me.hashCode();
	}
	
	public int compareTo(WSName other)
	{
		URI me = getEndpointIdentifier();
		URI you = other.getEndpointIdentifier();
		
		return me.toString().compareTo(you.toString());
	}
	
	public boolean isValidWSName()
	{
		return getEndpointIdentifier() != null;
	}
	
	public boolean hasValidResolver()
	{
		doExtraction();
		return((_resolvers != null) && (_resolvers.size() > 0));
	}
	
	public String toString()
	{
		return getEndpointIdentifier().toString();
	}
	
	static public URI generateNewEPI()
	{
		try
		{
			return new URI("urn:ws-naming:epi:" + new GUID().toString());
		}
		catch (URI.MalformedURIException use)
		{
			// Can't Happen
			_logger.fatal(use);
		}
		
		return null;
	}
	
	private void doExtraction()
	{
		synchronized(this)
		{
			if (_triedExtraction)
				return;

			_triedExtraction = true;
			EndpointReferenceType epr = getEndpoint();

			if (epr == null)
				return;
				
			/* try to extract EPI and resolvers */
			MetadataType mdt = epr.getMetadata();
			if (mdt == null)
				return;
			
			MessageElement []any = mdt.get_any();
			if (any == null || any.length == 0)
				return;
			
			// extact epi first
			for (MessageElement element : any)
			{
				if (element.getQName().equals(WSName.ENDPOINT_IDENTIFIER_QNAME))
				{
					String s = null;
					Node n = element.getFirstChild();
					if (n == null)
					{
						Object value = element.getObjectValue();
						if (value != null)
							s = value.toString();
					} else if (n instanceof Text)
						s = n.toString();
					
					if (s != null)
					{
						try
						{
							_endpointIdentifier = new URI(s);
							break;
						}
						catch (URI.MalformedURIException e)
						{
							_logger.warn("Found EPR with WSName \"" +
								s + "\" which isn't a URI.");
						}
					}
				}
			}

			for (MessageElement element : any)
			{
				if (element.getQName().equals(WSName.REFERENCE_RESOLVER_QNAME))
				{
					try 
					{
						EndpointReferenceType resolverEPR = (EndpointReferenceType) element.getObjectValue(EndpointReferenceType.class);
						if (resolverEPR != null)
						{
							ResolverDescription resolver = new ResolverDescription(
									_endpointIdentifier, 
									resolverEPR, 
									ResolverDescription.ResolverType.REFERENCE_RESOLVER);
							_resolvers.add(resolver);
						}
					}
					catch(Throwable t)
					{
						_logger.warn("Found reference resolver element that would not convert to a valid EPR.");
					}
				}
				else if (element.getQName().equals(WSName.ENDPOINT_IDENTIFIER_RESOLVER_QNAME))
				{
					try 
					{
						EndpointReferenceType resolverEPR = (EndpointReferenceType) element.getObjectValue(EndpointReferenceType.class);
						if (resolverEPR != null)
						{
							ResolverDescription resolver = new ResolverDescription(
									_endpointIdentifier, 
									resolverEPR, 
									ResolverDescription.ResolverType.EPI_RESOLVER);
							_resolvers.add(resolver);
						}
					}
					catch(Throwable t)
					{
						_logger.warn("Found endpoint identifier resolver element that would not convert to a valid EPR.");
					}
				}
			}
		}
	}
	
	public void addReferenceResolver(EndpointReferenceType resolverEPR)
	{
		doExtraction();
		ResolverDescription newResolverDesc = new ResolverDescription(
				_endpointIdentifier, resolverEPR, ResolverType.REFERENCE_RESOLVER);
		_epr = createEPRWithResolvers(_epr, resolverEPR, ResolverType.REFERENCE_RESOLVER, null);
		_resolvers.add(newResolverDesc);
	}
	
	public void addEndpointIdentifierReferenceResolver(EndpointReferenceType resolverEPR)
	{
		doExtraction();
		ResolverDescription newResolverDesc = new ResolverDescription(
				_endpointIdentifier, resolverEPR, ResolverType.EPI_RESOLVER);
		_epr = createEPRWithResolvers(_epr, resolverEPR, ResolverType.EPI_RESOLVER, null);
		_resolvers.add(newResolverDesc);
	}
	
	public void removeAllResolvers()
	{
		doExtraction();
		if (_resolvers.size() == 0)
			return;
		setResolvers(new ArrayList<ResolverDescription>());
	}
	
	public void setResolvers(List<ResolverDescription> resolvers)
	{
		doExtraction();
		_epr = createEPRWithResolvers(_epr, null, null, resolvers);
		_resolvers = resolvers;
	}

	private static EndpointReferenceType createEPRWithResolvers(EndpointReferenceType origEPR,
			EndpointReferenceType resolverEPR, ResolverType resolverType,
			List<ResolverDescription> resolvers)
	{
		AttributedURIType address = origEPR.getAddress();
		ReferenceParametersType referenceParameters = origEPR.getReferenceParameters();
		MetadataType metadata = origEPR.getMetadata();
		MessageElement[] any = origEPR.get_any();
		
		if (metadata == null)
		{
			metadata = new MetadataType();
		}
		MessageElement[] metadataElements = metadata.get_any();
		List<MessageElement> newMetadataElements = new ArrayList<MessageElement>();

		// Copy the metadata elements from the original array to the new list.
		// If we are adding a single new resolver, then include the original resolvers.
		// If we are replacing the list of resolvers, then skip the original resolvers.
		if (metadataElements != null)
		{
			for (MessageElement element : metadataElements)
			{
				if ((resolverEPR == null) && isResolverElement(element))
					continue;
				newMetadataElements.add(element);
			}
		}
		
		// Add the single new resolver.
		if (resolverEPR != null)
		{
			newMetadataElements.add(makeResolverMessageElement(resolverEPR, resolverType));
		}
		// Add the new list of resolvers.
		if (resolvers != null)
		{
			for (ResolverDescription resolver : resolvers)
			{
				newMetadataElements.add(makeResolverMessageElement(resolver.getEPR(), resolver.getType()));
			}
		}
		
		MetadataType newMetadata = new MetadataType(newMetadataElements.toArray(new MessageElement[0]));
		return new EndpointReferenceType(address, referenceParameters, newMetadata, any);
	}

	private static boolean isResolverElement(MessageElement element)
	{
		return(element.getQName().equals(WSName.ENDPOINT_IDENTIFIER_RESOLVER_QNAME) ||
			   element.getQName().equals(WSName.REFERENCE_RESOLVER_QNAME));
	}

	private static MessageElement makeResolverMessageElement(EndpointReferenceType resolverEPR, ResolverType resolverType)
	{
		if (resolverType == ResolverType.REFERENCE_RESOLVER)
		{
			return new MessageElement(WSName.REFERENCE_RESOLVER_QNAME, resolverEPR);
		}
		if (resolverType == ResolverType.EPI_RESOLVER)
		{
			return new MessageElement(WSName.ENDPOINT_IDENTIFIER_RESOLVER_QNAME, resolverEPR);
		}
		return null;
	}
	
	private void writeObject(ObjectOutputStream out)
    	throws IOException
	{
		EPRUtils.serializeEPR(out, _epr);
	}

	private void readObject(ObjectInputStream in)
    	throws IOException, ClassNotFoundException
	{
		_epr = EPRUtils.deserializeEPR(in);
		

		_triedExtraction = false;
		_endpointIdentifier = null;
		_resolvers = new ArrayList<ResolverDescription>();
	}

	@SuppressWarnings("unused")
	private void readObjectNoData() 
    	throws ObjectStreamException
	{
		throw new StreamCorruptedException("The input stream is corrupt.");
	}
}