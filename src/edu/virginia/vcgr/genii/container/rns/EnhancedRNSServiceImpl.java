/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.genii.container.rns;

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
import org.morgan.inject.MInject;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverUtils;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.ResourceLock;
import edu.virginia.vcgr.genii.client.rns.GeniiDirPolicy;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.client.rns.RNSOperation;
import edu.virginia.vcgr.genii.client.rns.RNSOperations;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.sync.SyncProperty;
import edu.virginia.vcgr.genii.client.sync.VersionVector;
import edu.virginia.vcgr.genii.client.utils.StatsLogger;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSContentChangeNotification;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSOperationContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreateResponse;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.axis.ServerAuthorizationManagement;
import edu.virginia.vcgr.genii.container.byteio.DefaultRandomByteIOAttributePreFetcher;
import edu.virginia.vcgr.genii.container.byteio.RandomByteIOServiceImpl;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.common.DefaultGenesisIIAttributesPreFetcher;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.invoker.timing.Timer;
import edu.virginia.vcgr.genii.container.invoker.timing.TimingSink;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorEntry;
import edu.virginia.vcgr.genii.container.iterator.InMemoryIteratorWrapper;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclTopics;
import edu.virginia.vcgr.genii.container.serializer.MessageElementSerializer;
import edu.virginia.vcgr.genii.container.sync.AclChangeNotificationHandler;
import edu.virginia.vcgr.genii.container.sync.DestroyFlags;
import edu.virginia.vcgr.genii.container.sync.MessageFlags;
import edu.virginia.vcgr.genii.container.sync.ReplicationItem;
import edu.virginia.vcgr.genii.container.sync.ReplicationThread;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceAttributeHandlers;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceUtils;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.PublisherTopic;
import edu.virginia.vcgr.genii.container.wsrf.wsn.topic.TopicSet;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;
import edu.virginia.vcgr.genii.resolver.UpdateResponseType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = RNSDBResourceProvider.class)
public class EnhancedRNSServiceImpl extends GenesisIIBase implements EnhancedRNSPortType, RNSTopics, AclTopics
{
	static private Log _logger = LogFactory.getLog(EnhancedRNSServiceImpl.class);

	@MInject(lazy = true)
	private IRNSResource _resource;

	@MInject
	private ResourceLock _resourceLock;

	@Override
	protected void setAttributeHandlers() throws NoSuchMethodException, ResourceException, ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		new VersionedResourceAttributeHandlers(getAttributePackage());
		new GeniiDirAttributeHandlers(getAttributePackage());
		new RNSAttributesHandler(getAttributePackage());
	}

	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return false;
	}
	
	public EnhancedRNSServiceImpl() throws RemoteException
	{
		this("EnhancedRNSPortType");
	}

	protected EnhancedRNSServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.RNS_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.ENHANCED_RNS_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE());
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.ENHANCED_RNS_PORT_TYPE();
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		GenesisHashMap constructionParameters, Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParams);

		EndpointReferenceType primaryEPR = (EndpointReferenceType) constructionParameters.get(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM);
		if (primaryEPR != null) {
			IResource resource = rKey.dereference();
			VersionedResourceUtils.initializeReplica(resource, primaryEPR, 0);
			WorkingContext context = WorkingContext.getCurrentWorkingContext();
			ReplicationThread thread = new ReplicationThread(context);
			thread.add(new ReplicationItem(new GeniiDirSyncRunner(), newEPR));
			thread.start();
		}
	}

	@Override
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();

		DestroyFlags flags = VersionedResourceUtils.preDestroy(_resource);
		if (flags != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("EnhancedRNSServiceImpl: publish destroy notification");
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic topic = space.createPublisherTopic(RNS_OPERATION_TOPIC);
			EndpointReferenceType myEPR =
				(EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.EPR_PROPERTY_NAME);
			RNSOperations operation = (flags.isUnlinked ? RNSOperations.Unlink : RNSOperations.Destroy);
			topic.publish(new RNSOperationContents(operation, ".", myEPR, flags.vvr));
		}
	}

	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponseType createFile(CreateFileRequestType createFile)
		throws RemoteException, RNSEntryExistsFaultType, ResourceUnknownFaultType
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("createFile(%s)", (createFile == null) ? "null" : createFile.getFilename()));

		return createFile(createFile, null);
	}

	protected CreateFileResponseType createFile(CreateFileRequestType createFile, MessageElement[] attributes)
		throws RemoteException, RNSEntryExistsFaultType, ResourceUnknownFaultType
	{
		String filename = createFile.getFilename();
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("EnhancedRNSPortType: File Create \"" + filename + "\" from " + caller);
		// End logging
		try {
			/*
			 * hmmm: rather than building a list of all the files in the directory and seeing if this one is there - just check if is there.
			 */
			_resourceLock.lock();
			Collection<String> entries = _resource.listEntries(filename);
			_resource.commit();

			if (entries.contains(filename)) {
				RNSEntryExistsFaultType fault = new RNSEntryExistsFaultType();
				fault.setEntryName(filename);
				throw FaultManipulator.fillInFault(fault);
			}
			// ASG modified to make a direct create in the current container
			// System.err.println("About to create the file " + createFile.getFilename());
			RandomByteIOServiceImpl service = new RandomByteIOServiceImpl();
			// System.err.println("created the file");
			String serviceURL = Container.getServiceURL("RandomByteIOPortType");

			/*
			 * October 15, 2015 ASG and CAK. Push creation mask into calling context - default to rw
			 */
			ICallingContext context;
			try {
				context = ContextManager.getCurrentContext();
				context.setSingleValueProperty(GenesisIIConstants.CREATION_MASK, "rw");
			} catch (Exception e) {
				_logger.debug("Could not acquire calling context to set creation mask within.");
			}

			EndpointReferenceType entryReference = service.CreateEPR(null, serviceURL);

			entryReference = prepareEPRToStore(entryReference);
			_resource.addEntry(new InternalEntry(filename, entryReference, attributes));
			VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(_resource);
			fireRNSEntryAdded(vvr, filename, entryReference);

			MessageElement[] prefetchedAttributes = null;
			AttributesPreFetcherFactory factory = new AttributesPreFetcherFactoryImpl();
			prefetchedAttributes = Prefetcher.preFetch(entryReference, new MessageElement[] {}, factory, null, null, false);
			RNSOperation operation = new RNSOperation(RNSOperation.OperationType.ENTRY_CREATE, filename);
			notifyChangeInContent(operation, entryReference, prefetchedAttributes);

			return new CreateFileResponseType(entryReference);
		} finally {
			_resourceLock.unlock();
		}
	}

	static private String toString(RNSEntryType entry)
	{
		return String.format("RNSEntry[name=%s, epr=%s,...]", entry.getEntryName(), (entry.getEndpoint() == null) ? "no" : "yes");
	}

	static private String toString(RNSEntryType[] entries)
	{
		StringBuilder builder = new StringBuilder();
		if (entries == null)
			return "null";

		for (int lcv = 0; lcv < entries.length; lcv++) {
			if (lcv != 0)
				builder.append(", ");
			builder.append(toString(entries[lcv]));
		}
		return builder.toString();
	}

	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest) throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("add(%s)", toString(addRequest)));

		if (addRequest == null || addRequest.length == 0)
			addRequest = new RNSEntryType[] { null };

		RNSEntryResponseType[] ret = new RNSEntryResponseType[addRequest.length];
		for (int lcv = 0; lcv < ret.length; lcv++) {
			// 2014-11-05 ASG - adding logging
			String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
			StatsLogger.logStats("EnhancedRNSPortType: Dir link \"" + addRequest[lcv].getEntryName() + "\" from " + caller);
			// End logging
			try {
				ret[lcv] = add(addRequest[lcv]);
			} catch (BaseFaultType bft) {
				ret[lcv] = new RNSEntryResponseType(null, null, bft, addRequest[lcv].getEntryName());
			} catch (Throwable cause) {
				_logger.error("failure during add request", cause);
				ret[lcv] = new RNSEntryResponseType(null, null,
					FaultManipulator.fillInFault(new BaseFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Unable to add entry: " + cause.getMessage()) }, null)),
					addRequest[lcv].getEntryName());
			}
		}
		return ret;
	}

	protected RNSEntryResponseType add(RNSEntryType entry) throws RemoteException
	{
		EndpointReferenceType entryReference;
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("EnhancedRNSPortType: Create link " + "\"" + entry.getEntryName() + "\"" + " from " + caller);
		// End logging
		if (entry == null || entry.getEntryName() == null) {
			// Pure factory operation
			VcgrCreateResponse response = vcgrCreate(new VcgrCreate());
			entryReference = response.getEndpoint();
			return new RNSEntryResponseType(entryReference, null, null, "/");
		}

		String name = entry.getEntryName();
		entryReference = entry.getEndpoint();
		RNSMetadataType mdt = entry.getMetadata();
		MessageElement[] attrs = (mdt == null) ? null : mdt.get_any();
		VersionVector vvr = null;

		if (entryReference == null) {
			// Create a new directory (RNS resource).
			VcgrCreateResponse response = vcgrCreate(new VcgrCreate());
			entryReference = response.getEndpoint();
			entryReference = prepareEPRToStore(entryReference);
		}
		try {
			_resourceLock.lock();
			_resource.addEntry(new InternalEntry(name, entryReference, attrs));
			vvr = VersionedResourceUtils.incrementResourceVersion(_resource);
		} finally {
			_resourceLock.unlock();
		}
		MessageElement[] attributes = null;
		AttributesPreFetcherFactory factory = new AttributesPreFetcherFactoryImpl();
		attributes = Prefetcher.preFetch(entryReference, new MessageElement[] {}, factory, null, null, false);
		RNSMetadataType returnedMetadata = RNSUtilities.createMetadata(entryReference, attributes);

		fireRNSEntryAdded(vvr, name, entryReference);

		RNSOperation operation = new RNSOperation(RNSOperation.OperationType.ENTRY_ADD, name);
		notifyChangeInContent(operation, entryReference, attributes);

		return new RNSEntryResponseType(entryReference, returnedMetadata, null, name);
	}

	static private class AttributesPreFetcherFactoryImpl implements AttributesPreFetcherFactory
	{
		@Override
		// service is unused
		// forkPath is unused
		public AttributePreFetcher getPreFetcher(EndpointReferenceType target, ResourceKey rKey, ResourceForkService service) throws Throwable
		{
			String targetAddress = target.getAddress().toString();
			String rbyteioAddress = Container.getServiceURL("RandomByteIOPortType");
			if (rbyteioAddress.equalsIgnoreCase(targetAddress)) {
				return new DefaultRandomByteIOAttributePreFetcher(target);
			} else if (Container.getServiceURL("EnhancedRNSPortType").equalsIgnoreCase(targetAddress)) {
				return new RNSAttributesPrefetcher(target);
			} else if (Container.onThisServer(target)) {
				return new DefaultGenesisIIAttributesPreFetcher<IResource>(target);
			}
			return null;
		}
	}

	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String[] lookupRequest) throws RemoteException, org.ggf.rns.ReadNotPermittedFaultType
	{
		if (_resource.getProperty(SyncProperty.ERROR_STATE_PROP_NAME) != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("lookup: resource in error state");
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(), "bad replica");
		}

		if (_logger.isTraceEnabled())
			_logger.trace(String.format("fast lookup(%s)", Arrays.toString(lookupRequest)));

		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("EnhancedRNSPortType: Dir List from " + caller);
		// End logging
		// ASG 2014-03-25 Here is where we will put the code to check if an implicit
		// parameter has been passed in the SOAP header, and if so, not include the EPR
		// in the response entry, but include the EPI as Metadata.
		// This same change will need to go into all lookup calls, for example in resource
		// forks and exports.

		TimingSink tSink = TimingSink.sink();
		Collection<InternalEntry> entries = new LinkedList<InternalEntry>();
		List<InMemoryIteratorEntry> indices = new LinkedList<InMemoryIteratorEntry>();
		boolean isIndexedIterate = false;
		int batchLimit = RNSConstants.PREFERRED_BATCH_SIZE;
		boolean batchRequest = false;

		try {
			_resourceLock.lock();
			Timer rTimer = tSink.getTimer("Retrieve Entries");

			if (lookupRequest == null || lookupRequest.length == 0) {
				// this is a batch lookup
				lookupRequest = new String[] { null };
				// we will be building an iterator as number of entries > threshold.
				if (_resource.retrieveOccurrenceCount() > batchLimit)
					isIndexedIterate = true;
				batchRequest = true;
			}

			else {
				// Identify the number of responses by looking at the number of requests. There is a
				// 1-1 correspondence between the two.
				if (lookupRequest.length > batchLimit)
					isIndexedIterate = true;
			}

			if (isIndexedIterate) {
				for (String request : lookupRequest) {
					indices.addAll(_resource.retrieveIdOfEntry(request));
				}

				for (int lcv = 0; lcv < batchLimit; ++lcv) {
					InMemoryIteratorEntry imie = indices.remove(0);

					if (imie.isExistent()) {
						InternalEntry ie = _resource.retrieveInternalEntryFromID(imie.getId());

						if (ie == null) {
							// this shouldn't happen as isExists wouldn't have been set.
							entries.add(new InternalEntry(imie.getEntryName(), null, null, false));
						} else {
							entries.add(ie);
						}
					} else {
						entries.add(new InternalEntry(imie.getEntryName(), null, null, false));
					}
				}
			} else {
				for (String request : lookupRequest)
					entries.addAll(_resource.retrieveEntries(request));
			}

			rTimer.noteTime();
			_resource.commit();
		} finally {
			_resourceLock.unlock();
		}

		// ---------------------------------------------------------------------------------------------------
		// Checking if short form is requested by the client. Properly handling this attributes is
		// Prof.'s
		// responsibility.
		// ---------------------------------------------------------------------------------------------------
		boolean requestedShortForm = false;
		try {
			ICallingContext context = ContextManager.getCurrentContext();
			Object form = context.getSingleValueProperty(GenesisIIConstants.RNS_SHORT_FORM_TOKEN);
			if (form != null && Boolean.TRUE.equals(form) && batchRequest) {
				requestedShortForm = true;
				_logger.info("Short form attribute found to be set to true in the calling context");
			}
		} catch (Exception e) {
			_logger.trace("could not get information about the short form");
		}
		// ---------------------------------------------------------------------------------------------------

		AttributesPreFetcherFactory factory = new AttributesPreFetcherFactoryImpl();

		Collection<RNSEntryResponseType> resultEntries = new LinkedList<RNSEntryResponseType>();
		Timer prepTimer = tSink.getTimer("Prepare Entries");

		for (InternalEntry internalEntry : entries) {
			if (!internalEntry.isExistent()) {
				// the looked-up entry does not exist . Only for non-batch
				String name = internalEntry.getName();
				RNSEntryResponseType entry = new RNSEntryResponseType(null, null,
					FaultManipulator.fillInFault(new RNSEntryDoesNotExistFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(String.format("Entry" + " %s does not exist!", name)) },
						null, name)),
					name);
				resultEntries.add(entry);
			} else {
				// long start2;
				EndpointReferenceType epr = internalEntry.getEntryReference();
				RNSEntryResponseType entry = null;
				// System.out.println("\tTime to getentry reference is " + (System.currentTimeMillis()-start2));
				// start2 = System.currentTimeMillis();
				MessageElement[] me = Prefetcher.preFetch(epr, internalEntry.getAttributes(), factory, null, null, requestedShortForm);
				// System.out.println("\ttime to prefetch is " + (System.currentTimeMillis()-start2));
				RNSMetadataType met = RNSUtilities.createMetadata(epr, me);
				// System.out.println("\ttime to produce metatdata is " + (System.currentTimeMillis()-start2));

				entry = new RNSEntryResponseType(epr, met, null, internalEntry.getName());
				// System.out.println("\tTime to getentry reference and build entry response is " + (System.currentTimeMillis()-start2));

				// ---------------------------------------------------------------------------------------------
				// Removing EPR from entry when short form is requested
				if (requestedShortForm)
					entry.setEndpoint(null);
				// ---------------------------------------------------------------------------------------------

				resultEntries.add(entry);
				// System.out.println("Time to prepare an entry is " + (System.currentTimeMillis()-start2));
			}
		}

		// System.out.println("Time to prepare entries is " + (System.currentTimeMillis()-start));
		prepTimer.noteTime();

		Timer createTimer = tSink.getTimer("Create Iterator");
		try {
			InMemoryIteratorWrapper imiw = new InMemoryIteratorWrapper(this.getClass().getName(), indices, null);
			return RNSContainerUtilities.indexedTranslate(resultEntries, iteratorBuilder(RNSEntryResponseType.getTypeDesc().getXmlType()),
				imiw);
		} finally {
			createTimer.noteTime();
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	final public RNSEntryResponseType[] rename(NameMappingType[] renameRequest) throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException("Rename not supported in Resource forks!");
	}
	/*
	 * private boolean destroyEntry(String entry) {
	 * 
	 * EndpointRefere String serviceName = EPRUtils.extractServiceName(entryReference); EndpointReferenceType resolverEPR =
	 * EPRUtils.fromBytes(data); UpdateResponseType response = ResolverUtils.updateResolver(resolverEPR, entryReference); entryReference =
	 * response.getNew_EPR(); if ((serviceName != null) && serviceName.equals(_serviceName)) { AddressingParameters aParams = new
	 * AddressingParameters(entryReference.getReferenceParameters()); String resourceKey = aParams.getResourceKey(); ResourceKey rkKey =
	 * ResourceManager.getTargetResource(serviceName, resourceKey); IResource childResource = rkKey.dereference();
	 * childResource.setProperty(GeniiDirPolicy.RESOLVER_POLICY_PROP_NAME, data); String value = (String)
	 * _resource.getProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME); if (value != null) {
	 * childResource.setProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME, value); } childResource.commit();
	 * 
	 * return true; }
	 */

	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest) throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		if (_logger.isTraceEnabled())
			_logger.trace(String.format("remove(%s)", Arrays.toString(removeRequest)));
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);

		// End logging
		List<String> removed = new LinkedList<String>();
		Collection<String> tmp;
		VersionVector vvr;
		try {
			_resourceLock.lock();

			for (String request : removeRequest) {
				// 2014-11-05 ASG - adding logging
				StatsLogger.logStats("EnhancedRNSPortType: Dir Remove " + "\"" + request + "\"" + " from " + caller);
				// End logging
				tmp = _resource.removeEntries(request);
				if (tmp != null)
					removed.addAll(tmp);
			}
			vvr = VersionedResourceUtils.incrementResourceVersion(_resource);
			_resource.commit();
		} finally {
			_resourceLock.unlock();
		}
		RNSEntryResponseType[] ret = new RNSEntryResponseType[removed.size()];
		for (int idx = 0; idx < ret.length; idx++) {
			String name = removed.get(idx);
			ret[idx] = new RNSEntryResponseType(null, null, null, name);
			fireRNSEntryRemoved(vvr, name);
		}
		RNSOperation operation = new RNSOperation(RNSOperation.OperationType.ENTRY_REMOVE, removeRequest);
		notifyChangeInContent(operation, null, null);

		if (_logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			for (RNSEntryResponseType ent : ret) {
				sb.append(ent.getEntryName());
				sb.append(" ");
			}
			_logger.debug("providing a response with " + ret.length + " elements, containing: " + sb);
		}
		return ret;
	}

	@RWXMapping(RWXCategory.WRITE)
	final public RNSEntryResponseType[] setMetadata(MetadataMappingType[] setMetadataRequest)
		throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException("setMetadata operation not supported!");
	}

	private void fireRNSEntryAdded(VersionVector vvr, String name, EndpointReferenceType entry)
		throws ResourceUnknownFaultType, ResourceException
	{
		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic topic = space.createPublisherTopic(RNS_OPERATION_TOPIC);
		topic.publish(new RNSOperationContents(RNSOperations.Add, name, entry, vvr));
	}

	private void fireRNSEntryRemoved(VersionVector vvr, String name) throws ResourceUnknownFaultType, ResourceException
	{
		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic topic = space.createPublisherTopic(RNS_OPERATION_TOPIC);
		topic.publish(new RNSOperationContents(RNSOperations.Remove, name, null, vvr));
	}

	// future: setResourceProperties() does not send "policy" update.

	/**
	 * Given a new resource which was just created and which is about to be added to this directory - if this directory has a policy that new
	 * resources must be registered with a resolver, then register the resource and return an EPR that includes the resolver reference.
	 */
	private EndpointReferenceType prepareEPRToStore(EndpointReferenceType entryReference) throws RemoteException
	{
		byte[] data = (byte[]) _resource.getProperty(GeniiDirPolicy.RESOLVER_POLICY_PROP_NAME);
		if (data != null) {
			String serviceName = EPRUtils.extractServiceName(entryReference);
			EndpointReferenceType resolverEPR = EPRUtils.fromBytes(data);
			UpdateResponseType response = ResolverUtils.updateResolver(resolverEPR, entryReference);
			entryReference = response.getNew_EPR();
			if ((serviceName != null) && serviceName.equals(_serviceName)) {
				AddressingParameters aParams = new AddressingParameters(entryReference.getReferenceParameters());
				String resourceKey = aParams.getResourceKey();
				ResourceKey rkKey = ResourceManager.getTargetResource(serviceName, resourceKey);
				IResource childResource = rkKey.dereference();
				childResource.setProperty(GeniiDirPolicy.RESOLVER_POLICY_PROP_NAME, data);
				String value = (String) _resource.getProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME);
				if (value != null) {
					childResource.setProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME, value);
				}
				childResource.commit();
			}
		}
		return entryReference;
	}

	/*
	 * Do not change the name or signature of the below method. It is used in WSIteratorDBResource using java-reflection.
	 * 
	 * If modifying: edit in WSIteratorDBResource.java and QueueServiceImpl.java and LightWeightExportDirFork.java.
	 */
	public static MessageElement getIndexedContent(Connection connection, InMemoryIteratorEntry entry, Object[] obj, boolean shortForm)
		throws ResourceException
	{
		if (connection == null || entry == null) // obj will be null as it is unused
			throw new ResourceException("Unable to list directory contents");

		RNSEntryResponseType resp = null;

		if (!entry.isExistent()) {
			String name = entry.getEntryName();
			resp = new RNSEntryResponseType(null, null,
				FaultManipulator.fillInFault(new RNSEntryDoesNotExistFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(String.format("Entry" + " %s does not exist!", name)) },
					null, name)),
				name);
		}

		else {
			InternalEntry ie = RNSDBResource.retrieveByIndex(connection, entry.getId());

			if (ie == null) {
				String name = entry.getEntryName();
				resp = new RNSEntryResponseType(null, null,
					FaultManipulator.fillInFault(new RNSEntryDoesNotExistFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] { new BaseFaultTypeDescription(String.format("Entry" + " %s does not exist!", name)) },
						null, name)),
					name);
			}

			else {
				AttributesPreFetcherFactory factory = new AttributesPreFetcherFactoryImpl();

				EndpointReferenceType epr = ie.getEntryReference();
				// ASG 4/30/2014, we need to come back and fix this so that the caller passes in
				// whether shortForm
				resp = new RNSEntryResponseType(shortForm ? null : epr,
					RNSUtilities.createMetadata(epr, Prefetcher.preFetch(epr, ie.getAttributes(), factory, null, null, shortForm)), null,
					ie.getName());
			}
		}

		return MessageElementSerializer.serialize(RNSEntryResponseType.getTypeDesc().getXmlType(), resp);
	}

	@Override
	public ResourceSyncRunner getClassResourceSyncRunner()
	{
		return new GeniiDirSyncRunner();
	}

	@Override
	protected void registerNotificationHandlers(NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		multiplexer.registerNotificationHandler(RNSTopics.RNS_OPERATION_TOPIC.asConcreteQueryExpression(),
			new RNSOperationNotificationHandler());
		multiplexer.registerNotificationHandler(AclTopics.GENII_ACL_CHANGE_TOPIC.asConcreteQueryExpression(),
			new AclChangeNotificationHandler());
	}

	private void notifyChangeInContent(RNSOperation operation, EndpointReferenceType entry, MessageElement[] attributes)
		throws ResourceUnknownFaultType, ResourceException
	{
		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic topic = space.createPublisherTopic(RNS_CONTENT_CHANGE_TOPIC);
		Integer elementCount = (Integer) _resource.getProperty(IRNSResource.ELEMENT_COUNT_PROPERTY);
		topic.publish(new RNSContentChangeNotification(operation, entry, elementCount, attributes));
	}

	private class RNSOperationNotificationHandler extends AbstractNotificationHandler<RNSOperationContents>
	{
		private RNSOperationNotificationHandler()
		{
			super(RNSOperationContents.class);
		}

		@Override
		public String handleNotification(TopicPath topicPath, EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference, RNSOperationContents contents) throws Exception
		{
			boolean validParams = false;
			RNSOperations operation = contents.operation();
			String entryName = contents.entryName();
			EndpointReferenceType entryReference = contents.entryReference();
			VersionVector remoteVector = contents.versionVector();
			if (operation != null) {
				if (operation.equals(RNSOperations.Add) && (entryName != null) && (entryReference != null))
					validParams = true;
				if (operation.equals(RNSOperations.Remove) && (entryName != null))
					validParams = true;
				if (operation.equals(RNSOperations.Destroy) || operation.equals(RNSOperations.Unlink))
					validParams = true;
			}
			if (!validParams) {
				if (_logger.isDebugEnabled())
					_logger.debug("GeniiDirServiceImpl.notify: invalid parameters");
				return NotificationConstants.FAIL;
			}
			IRNSResource resource = _resource;
			// The notification contains the identity of the user who modified the resource.
			// The identity is delegated to the resource that sent the notification.
			// The notification is signed by the resource.
			if (!ServerAuthorizationManagement.checkAccess(resource, RWXCategory.WRITE)) {
				if (_logger.isDebugEnabled())
					_logger.debug("GeniiDirServiceImpl.notify: permission denied");
				return NotificationConstants.FAIL;
			}
			ReplicationItem item = null;
			boolean replay = false;
			try {
				_resourceLock.lock();
				if (operation.equals(RNSOperations.Destroy)) {
					resource.setProperty(SyncProperty.IS_DESTROYED_PROP_NAME, "true");
					destroy(new Destroy());
					return NotificationConstants.OK;
				}
				VersionVector localVector = (VersionVector) resource.getProperty(SyncProperty.VERSION_VECTOR_PROP_NAME);
				MessageFlags flags = VersionedResourceUtils.validateNotification(resource, localVector, remoteVector);
				if (flags.status != null) {
					return flags.status;
				}
				if (operation.equals(RNSOperations.Add)) {
					entryName = processAddEntryName(entryName, flags.replay, resource);
					try {
						item = AutoReplicate.autoReplicate(resource, entryReference);
					} catch (Exception exception) {
						_logger.warn("GeniiDir: failed to create local replica", exception);
					}
					InternalEntry entry = new InternalEntry(entryName, (item != null ? item.localEPR : entryReference), null);
					resource.addEntry(entry);
				} else if (operation.equals(RNSOperations.Remove)) {
					resource.removeEntries(entryName);
				} else if (operation.equals(RNSOperations.Unlink)) {
					VersionedResourceUtils.destroySubscription(resource, producerReference);
					return NotificationConstants.OK;
				}
				VersionedResourceUtils.updateVersionVector(resource, localVector, remoteVector);
				replay = flags.replay;
			} finally {
				_resourceLock.unlock();
			}
			if ((item != null) && (item.runner != null)) {
				ReplicationThread thread = new ReplicationThread(WorkingContext.getCurrentWorkingContext());
				thread.add(item);
				thread.start();
			}
			if (replay) {
				VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(resource);
				if (_logger.isDebugEnabled())
					_logger.debug("GeniiDirServiceImpl.notify: replay message");
				TopicSet space = TopicSet.forPublisher(EnhancedRNSServiceImpl.class);
				PublisherTopic topic = space.createPublisherTopic(topicPath);
				topic.publish(new RNSOperationContents(operation, entryName, entryReference, vvr));
			}
			return NotificationConstants.OK;
		}

		private String processAddEntryName(String entryName, boolean replay, IRNSResource resource) throws ResourceException
		{
			Collection<String> conflicts = resource.listEntries(entryName);
			if (conflicts.size() == 0)
				return entryName;
			if (!replay) {
				_logger.warn("GeniiDir: overwrite " + entryName);
				resource.removeEntries(entryName);
				return entryName;
			} else {
				_logger.warn("GeniiDir: rename entry from replica: " + entryName);
				String prefix = entryName;
				for (int count = 2; true; count++) {
					String suffix = "." + count;
					int maxLength = 256 - suffix.length();
					if (prefix.length() > maxLength)
						prefix = prefix.substring(0, maxLength);
					entryName = prefix + suffix;
					if (resource.listEntries(entryName).size() == 0)
						return entryName;
				}
			}
		}
	}
}
