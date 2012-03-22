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
package edu.virginia.vcgr.genii.container.rns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryDoesNotExistFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.RNSMetadataType;
import org.ggf.rns.WriteNotPermittedFaultType;
import org.morgan.inject.MInject;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSEntryAddedContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.DefaultRandomByteIOAttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.RandomByteIOServiceImpl;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.common.DefaultGenesisIIAttributesPreFetcher;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.invoker.timing.Timer;
import edu.virginia.vcgr.genii.container.invoker.timing.TimingSink;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceLock;
import edu.virginia.vcgr.genii.container.serializer.MessageElementSerializer;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;

@GeniiServiceConfiguration(
	resourceProvider=RNSDBResourceProvider.class)
public class EnhancedRNSServiceImpl extends GenesisIIBase
	implements EnhancedRNSPortType, RNSTopics
{	
	static private Log _logger = LogFactory.getLog(EnhancedRNSServiceImpl.class);
	
	@MInject(lazy = true)
	private IRNSResource _resource;
	
	@MInject
	private ResourceLock _resourceLock;
	
	public EnhancedRNSServiceImpl() throws RemoteException
	{
		super("EnhancedRNSPortType");
		
		addImplementedPortType(RNSConstants.RNS_PORT_TYPE);
		addImplementedPortType(RNSConstants.ENHANCED_RNS_PORT_TYPE);
	}
	
	protected EnhancedRNSServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		
		addImplementedPortType(RNSConstants.RNS_PORT_TYPE);
		addImplementedPortType(RNSConstants.ENHANCED_RNS_PORT_TYPE);
	}
	
	public PortType getFinalWSResourceInterface()
	{
		return RNSConstants.ENHANCED_RNS_PORT_TYPE;
	}
	
	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponseType createFile(CreateFileRequestType createFile)
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType
	{
		_logger.trace(String.format("createFile(%s)",
			(createFile == null) ? "null" : createFile.getFilename()));
		
		return createFile(createFile, null);
	}
	
	protected CreateFileResponseType createFile(
		CreateFileRequestType createFile, MessageElement []attributes) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType
	{
		String filename = createFile.getFilename();
		
		try
		{
			_resourceLock.lock();
			Collection<String> entries = _resource.listEntries(filename);
			_resource.commit();
			
			if (entries.contains(filename))
				throw FaultManipulator.fillInFault(
					new RNSEntryExistsFaultType(null, null, null, null,
						null, null, filename));
			/* August 15, ASG, modified to make a direct create in the current container */
			EndpointReferenceType entryReference = 
				new RandomByteIOServiceImpl().CreateEPR(null,Container.getServiceURL("RandomByteIOPortType"));
		
			/* if entry has a resolver, set address to unbound */
			EndpointReferenceType eprToStore = prepareEPRToStore(entryReference);
			_resource.addEntry(new InternalEntry(filename, eprToStore, 
				attributes));
			_resource.commit();
			return new CreateFileResponseType(entryReference);
		}
		finally
		{
			_resourceLock.unlock();
		}
	}
	
	static private String toString(RNSEntryType entry)
	{
		return String.format("RNSEntry[name=%s, epr=%s,...]",
			entry.getEntryName(), (entry.getEndpoint() == null) ? "no" : "yes");
	}
	
	static private String toString(RNSEntryType []entries)
	{
		StringBuilder builder = new StringBuilder();
		if (entries == null)
			return "null";
		
		for (int lcv = 0; lcv < entries.length; lcv++)
		{
			if (lcv != 0)
				builder.append(", ");
			builder.append(toString(entries[lcv]));
		}
		
		return builder.toString();
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest)
		throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		_logger.trace(String.format("add(%s)", toString(addRequest)));
		
		if (addRequest == null || addRequest.length == 0)
			addRequest = new RNSEntryType[] { null };
		
		RNSEntryResponseType []ret = new RNSEntryResponseType[addRequest.length];
		for (int lcv = 0; lcv < ret.length; lcv++)
		{
			try
			{
				ret[lcv] = add(addRequest[lcv]);
			}
			catch (BaseFaultType bft)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					bft, addRequest[lcv].getEntryName());
			}
			catch (Throwable cause)
			{
				ret[lcv] = new RNSEntryResponseType(null, null,
					FaultManipulator.fillInFault(
						new BaseFaultType(null, null, null, null, 
							new BaseFaultTypeDescription[] { 
								new BaseFaultTypeDescription("Unable to add entry!") 
							}, null)), addRequest[lcv].getEntryName());
			}
		}
		
		return ret;
	}
	
	protected RNSEntryResponseType add(RNSEntryType entry)
		throws RemoteException
	{
		EndpointReferenceType entryReference;
		
		if (entry == null || entry.getEntryName() == null)
		{
			// Pure factory operation
			return new RNSEntryResponseType(
				vcgrCreate(new VcgrCreate()).getEndpoint(), 
				null, null, "/");
		}
		
		String name = entry.getEntryName();
		entryReference = entry.getEndpoint();
		RNSMetadataType mdt = entry.getMetadata();
		MessageElement []attrs = (mdt == null) ? null : mdt.get_any();
		
		if (entryReference == null)
			entryReference = vcgrCreate(new VcgrCreate()).getEndpoint();
		EndpointReferenceType eprToStore = prepareEPRToStore(entryReference);
		
		try
		{
			_resourceLock.lock();
			_resource.addEntry(new InternalEntry(name, eprToStore, attrs));
			_resource.commit();
		}
		finally
		{
			_resourceLock.unlock();
		}
		
		fireRNSEntryAdded(name, entryReference);
		return new RNSEntryResponseType(
			entryReference, mdt, null, name);
	}
	
	static private class AttributesPreFetcherFactoryImpl 
		implements AttributesPreFetcherFactory
	{
		@Override
		public AttributePreFetcher getPreFetcher(EndpointReferenceType target)
				throws Throwable
		{
			if (Container.getServiceURL(
				"RandomByteIOPortType").equalsIgnoreCase(
					target.getAddress().toString()))
						return new DefaultRandomByteIOAttributePreFetcher(
							target);
			else if (Container.onThisServer(target))
				return new DefaultGenesisIIAttributesPreFetcher<IResource>(
					target);
			
			return null;
		}
	}
	
	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String[] lookupRequest)
		throws RemoteException, org.ggf.rns.ReadNotPermittedFaultType
    {
		_logger.trace(String.format("fast lookup(%s)", Arrays.toString(lookupRequest)));
		
    	TimingSink tSink = TimingSink.sink();
    	Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
    	List<InMemoryIteratorEntry> indices = new LinkedList<InMemoryIteratorEntry>();
    	boolean isIndexedIterate = false;
    	int batchLimit = RNSConstants.PREFERRED_BATCH_SIZE;	
    	
    	try
    	{
    		_resourceLock.lock();
    		Timer rTimer = tSink.getTimer("Retrieve Entries");
    		
    		if (lookupRequest == null || lookupRequest.length == 0) //A batch lookup
        	{
    			lookupRequest = new String[] { null };
    			if(_resource.retrieveOccurrenceCount() > batchLimit)	//we will be building an iterator as number of entries > threshold
    				isIndexedIterate = true;       			   			
        	}
    		
    		else
    		{
    			if(lookupRequest.length > batchLimit) //Identify the number of responses by looking at the number of requests. There is a 1-1 correspondance between the two
    				isIndexedIterate = true;
    		}
    		
    		if(isIndexedIterate)
    		{
    			for(String request : lookupRequest)
    				indices.addAll(_resource.retrieveIdOfEntry(request));
    			
    			for(int lcv=0; lcv<batchLimit; ++lcv)
    			{
    				InMemoryIteratorEntry imie = indices.remove(0); 
    				
    				if(imie.isExistent())
    				{
    					InternalEntry ie = _resource.retrieveInternalEntryFromID(imie.getId());
    					
    					if(ie == null)
    						entries.add(new InternalEntry(imie.getEntryName(), null, null, false)); //this shouldn't happen as isExists wouldn't have been set
    					else
    						entries.add(ie);
    				}
    				
    				else
    				{
    					entries.add(new InternalEntry(imie.getEntryName(), null, null, false));
    				}
    				
    			}
    			
    		}
    		
    		else
    		{
    			for (String request : lookupRequest)
    				entries.addAll(_resource.retrieveEntries(request));
    		}
    		
    		rTimer.noteTime();
    		_resource.commit();
    	}
    	finally
    	{
    		_resourceLock.unlock();
    	}

    	AttributesPreFetcherFactory factory = 
    		new AttributesPreFetcherFactoryImpl();
    	
    	Collection<RNSEntryResponseType> resultEntries = 
			new LinkedList<RNSEntryResponseType>();
		Timer prepTimer = tSink.getTimer("Prepare Entries");
		for (InternalEntry internalEntry : entries)
    	{
			if(!internalEntry.isExistent()) //the looked-up entry does not exist . Only for non-batch
			{
				String name = internalEntry.getName();
				RNSEntryResponseType entry = new RNSEntryResponseType(null, null, 
						FaultManipulator.fillInFault(
								new RNSEntryDoesNotExistFaultType(
								null, null, null, null, 
								new BaseFaultTypeDescription[] 
								{
										new BaseFaultTypeDescription(String.format("Entry" +
										" %s does not exist!", name))
							    },null, name)), name);
				resultEntries.add(entry);
			}
			else
			{
				EndpointReferenceType epr = internalEntry.getEntryReference();
				RNSEntryResponseType entry = new RNSEntryResponseType(
		    			epr, RNSUtilities.createMetadata(epr, 
		    				Prefetcher.preFetch(epr, internalEntry.getAttributes(), factory)),
		    			null, internalEntry.getName());
				resultEntries.add(entry);
			}			
		}
    		
    	prepTimer.noteTime();
		
    	Timer createTimer = tSink.getTimer("Create Iterator");
		try
		{
		
			return RNSContainerUtilities.indexedTranslate(
		    		resultEntries, iteratorBuilder(
		    			RNSEntryResponseType.getTypeDesc().getXmlType()), indices);
		}
		finally
		{
			createTimer.noteTime();
		}
    }	
    
	@Override
	@RWXMapping(RWXCategory.WRITE)
	final public RNSEntryResponseType[] rename(NameMappingType[] renameRequest)
		throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException(
			"Rename not supported in Resource forks!");
	}
    
	@RWXMapping(RWXCategory.WRITE)
    public RNSEntryResponseType[] remove(String[] removeRequest)
		throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
    {
		_logger.trace(String.format("remove(%s)", Arrays.toString(removeRequest)));
		
    	Collection<RNSEntryResponseType> ret = 
    		new LinkedList<RNSEntryResponseType>();
    	
    	try
    	{
    		_resourceLock.lock();
    		for (String request : removeRequest)
    		{
    			_resource.removeEntries(request);
    			ret.add(new RNSEntryResponseType(
    				null, null, null, request));
    		}
		    _resource.commit();
    	}
    	finally
    	{
    		_resourceLock.unlock();
    	}
    	
	    return ret.toArray(new RNSEntryResponseType[ret.size()]);
    }
    
	@Override
	@RWXMapping(RWXCategory.WRITE)
	final public RNSEntryResponseType[] setMetadata(
		MetadataMappingType[] setMetadataRequest) throws RemoteException,
			org.ggf.rns.WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException(
			"setMetadata operation not supported!");
	}

    private void fireRNSEntryAdded(String name, EndpointReferenceType entry)
    	throws ResourceUnknownFaultType, ResourceException
    {
    	TopicSet space = TopicSet.forPublisher(getClass());
    	PublisherTopic topic = space.createPublisherTopic(
    		RNS_ENTRY_ADDED_TOPIC);
    	topic.publish(new RNSEntryAddedContents(name, entry));
    }
    
	static EndpointReferenceType prepareEPRToStore(EndpointReferenceType origEPR)
		throws WriteNotPermittedFaultType, RNSEntryDoesNotExistFaultType
	{
		String resolvedEntryUnboundProperty = null;
		
		// check if calling context overrides default behavior (which is to make
		// EPRs with resolvers have unbound addresses)

		try
		{
			ICallingContext ctx = ContextManager.getCurrentContext();
			resolvedEntryUnboundProperty = (String) 
				ctx.getSingleValueProperty(RNSConstants.RESOLVED_ENTRY_UNBOUND_PROPERTY);
		}
		catch(FileNotFoundException fe)
		{
    		_logger.warn(fe.getLocalizedMessage(), fe);
			throw FaultManipulator.fillInFault(
				new RNSEntryDoesNotExistFaultType());
		}
		catch(IOException ie)
		{
    		_logger.warn(ie.getLocalizedMessage(), ie);
			throw FaultManipulator.fillInFault(new WriteNotPermittedFaultType());
		}
		
		if (resolvedEntryUnboundProperty != null && resolvedEntryUnboundProperty.equals(RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE))
			return origEPR;
		
		WSName wsName = new WSName(origEPR);
		
		if (!wsName.hasValidResolver())
			return origEPR;

		return EPRUtils.makeUnboundEPR(origEPR);
	}

	public static MessageElement getIndexedContent(Connection connection,
			InMemoryIteratorEntry entry) throws ResourceException
	{
	
		RNSEntryResponseType resp = null;
		
		if(!entry.isExistent())
		{
			String name = entry.getEntryName();
			resp = new RNSEntryResponseType(null, null, 
					FaultManipulator.fillInFault(
							new RNSEntryDoesNotExistFaultType(
							null, null, null, null, 
							new BaseFaultTypeDescription[] 
							{
									new BaseFaultTypeDescription(String.format("Entry" +
									" %s does not exist!", name))
						    },null, name)), name);
		}
		
		else
		{	
			InternalEntry ie = RNSDBResource.retrieveByIndex(connection, entry.getId());
		
			if(ie==null)
			{
				String name = entry.getEntryName();
				resp = new RNSEntryResponseType(null, null, 
					FaultManipulator.fillInFault(
							new RNSEntryDoesNotExistFaultType(
							null, null, null, null, 
							new BaseFaultTypeDescription[] 
							{
									new BaseFaultTypeDescription(String.format("Entry" +
									" %s does not exist!", name))
						    },null, name)), name);
			}
		
			else
			{
				AttributesPreFetcherFactory factory = 
					new AttributesPreFetcherFactoryImpl();
			
				EndpointReferenceType epr = ie.getEntryReference();
				resp = new RNSEntryResponseType(
						epr, RNSUtilities.createMetadata(epr, 
	    				Prefetcher.preFetch(epr, ie.getAttributes(), factory)),
	    				null, ie.getName());					
			}
		}
		
		return(MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), resp));
	}
}