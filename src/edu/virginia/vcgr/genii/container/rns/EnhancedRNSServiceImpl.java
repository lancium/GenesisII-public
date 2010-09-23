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
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.Add;
import org.ggf.rns.AddResponse;
import org.ggf.rns.CreateFile;
import org.ggf.rns.CreateFileResponse;
import org.ggf.rns.EntryPropertiesType;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.Move;
import org.ggf.rns.MoveResponse;
import org.ggf.rns.Query;
import org.ggf.rns.QueryResponse;
import org.ggf.rns.RNSDirectoryNotEmptyFaultType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryNotDirectoryFaultType;
import org.ggf.rns.RNSFaultType;
import org.ggf.rns.Remove;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSEntryAddedContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;

import edu.virginia.vcgr.genii.enhancedrns.*;

import org.morgan.inject.MInject;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.DefaultRandomByteIOAttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.RandomByteIOServiceImpl;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.common.DefaultGenesisIIAttributesPreFetcher;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.invoker.timing.Timer;
import edu.virginia.vcgr.genii.container.invoker.timing.TimingSink;

import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceLock;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;

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
	public CreateFileResponse createFile(CreateFile createFile)
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		return createFile(createFile, null);
	}
	
	protected CreateFileResponse createFile(
		CreateFile createFile, MessageElement []attributes) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
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
			return new CreateFileResponse(entryReference);
		}
		finally
		{
			_resourceLock.unlock();
		}
	}
	
	@RWXMapping(RWXCategory.WRITE)
	public AddResponse add(Add addRequest) 
		throws RemoteException, RNSEntryExistsFaultType, 
			ResourceUnknownFaultType, 
			RNSEntryNotDirectoryFaultType, RNSFaultType
	{
		EndpointReferenceType entryReference;
		
		if (addRequest == null)
		{
			// Pure factory operation
			return new AddResponse(vcgrCreate(new VcgrCreate()).getEndpoint());
		}
		
		String name = addRequest.getEntry_name();
		entryReference = addRequest.getEntry_reference();
		MessageElement []attrs = addRequest.get_any();
		
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
		return new AddResponse(entryReference);
	}
	
	@RWXMapping(RWXCategory.READ)
    public ListResponse list(List list) 
    	throws RemoteException, ResourceUnknownFaultType, 
    		RNSEntryNotDirectoryFaultType, RNSFaultType
    {
		_logger.debug("Entered list method.");
    	
    	Collection<InternalEntry> entries;
    	
    	try
    	{
    		_resourceLock.lock();
	    	entries = _resource.retrieveEntries(list.getEntryName());
		    _resource.commit();
    	}
    	finally
    	{
    		_resourceLock.unlock();
    	}
    	
    	EntryType []ret = new EntryType[entries.size()];
    	int lcv = 0;
    	for (InternalEntry entry : entries)
    	{
    		ret[lcv++] = new EntryType(
    			entry.getName(), entry.getAttributes(), entry.getEntryReference());
    	}
    	
    	return new ListResponse(ret);
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
    public IterateListResponseType iterateList(IterateListRequestType list) 
    	throws RemoteException, ResourceUnknownFaultType, 
    		RNSEntryNotDirectoryFaultType, RNSFaultType
    {
		_logger.debug("Entered iterate list method.");
    	TimingSink tSink = TimingSink.sink();
    	Collection<InternalEntry> entries;
    	
    	try
    	{
    		_resourceLock.lock();
    		Timer rTimer = tSink.getTimer("Retrieve Entries");
    		entries = _resource.retrieveEntries(null);
    		rTimer.noteTime();
    		_resource.commit();
    	}
    	finally
    	{
    		_resourceLock.unlock();
    	}

    	AttributesPreFetcherFactory factory = 
    		new AttributesPreFetcherFactoryImpl();
    	
    	Timer prepTimer = tSink.getTimer("Prepare Entries");
		Collection<MessageElement> col = new LinkedList<MessageElement>();
    	for (InternalEntry internalEntry : entries)
    	{
    		EndpointReferenceType epr = internalEntry.getEntryReference();
    		
    		EntryType entry = new EntryType(
				internalEntry.getName(), 
				preFetch(epr, internalEntry.getAttributes(), factory),
				epr);

    		col.add(AnyHelper.toAny(entry));
    	}
    	prepTimer.noteTime();
		
    	Timer createTimer = tSink.getTimer("Create Iterator");
		try
		{
			return new IterateListResponseType(
				super.createWSIterator(col.iterator(), 100));
		}
		catch (SQLException sqe)
		{
			throw new RemoteException("Unable to create iterator.", sqe);
		}
		finally
		{
			createTimer.noteTime();
		}
    }	
    
	@RWXMapping(RWXCategory.WRITE)
    public MoveResponse move(Move move) 
    	throws RemoteException, ResourceUnknownFaultType, RNSFaultType
    {
    	throw FaultManipulator.fillInFault(new RNSFaultType());
    }
    
	@RWXMapping(RWXCategory.READ)
    public QueryResponse query(Query query) 
    	throws RemoteException, ResourceUnknownFaultType, RNSFaultType
    {
    	String entryPattern = query.getEntryPattern();
    	EntryType []tmp = list(new List(entryPattern)).getEntryList();
    	EntryPropertiesType []ret = new EntryPropertiesType[tmp.length];
    	EndpointReferenceType myEPR = 
    		(EndpointReferenceType)WorkingContext.getCurrentWorkingContext().getProperty(
    				WorkingContext.EPR_PROPERTY_NAME);
    	
    	for (int lcv = 0; lcv < tmp.length; lcv++)
    	{
    		ret[lcv] = new EntryPropertiesType(myEPR,
    			tmp[lcv].getEntry_name(), tmp[lcv].get_any(), 
    			tmp[lcv].getEntry_reference());
    	}
    	
    	return new QueryResponse(ret[0]);
    }
    
	@RWXMapping(RWXCategory.WRITE)
    public String[] remove(Remove remove) 
    	throws RemoteException, ResourceUnknownFaultType, 
    		RNSDirectoryNotEmptyFaultType, RNSFaultType
    {
    	String []ret;
    	Collection<String> removed;
    	
    	try
    	{
    		_resourceLock.lock();
	    	removed = _resource.removeEntries(remove.getEntryName());
		    _resource.commit();
    	}
    	finally
    	{
    		_resourceLock.unlock();
    	}
    	
	    ret = new String[removed.size()];
	    removed.toArray(ret);
    
	    return ret;
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
		throws RNSFaultType
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
			throw FaultManipulator.fillInFault(new RNSFaultType());
		}
		catch(IOException ie)
		{
    		_logger.warn(ie.getLocalizedMessage(), ie);
			throw FaultManipulator.fillInFault(new RNSFaultType());
		}
		
		if (resolvedEntryUnboundProperty != null && resolvedEntryUnboundProperty.equals(RNSConstants.RESOLVED_ENTRY_UNBOUND_FALSE))
			return origEPR;
		
		WSName wsName = new WSName(origEPR);
		
		if (!wsName.hasValidResolver())
			return origEPR;

		return EPRUtils.makeUnboundEPR(origEPR);
	}
}