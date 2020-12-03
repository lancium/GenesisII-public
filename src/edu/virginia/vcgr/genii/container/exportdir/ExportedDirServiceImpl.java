package edu.virginia.vcgr.genii.container.exportdir;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.LookupResponseType;
import org.ggf.rns.MetadataMappingType;
import org.ggf.rns.NameMappingType;
import org.ggf.rns.RNSEntryExistsFaultType;
import org.ggf.rns.RNSEntryResponseType;
import org.ggf.rns.RNSEntryType;
import org.ggf.rns.RNSMetadataType;
import org.ggf.rns.WriteNotPermittedFaultType;
import org.morgan.inject.MInject;
import org.morgan.util.GUID;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.exportdir.ExportedDirUtils;
import edu.virginia.vcgr.genii.client.exportdir.ExportedFileUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.ResourceLock;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.client.utils.StatsLogger;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.common.rfactory.ResourceCreationFaultType;
import edu.virginia.vcgr.genii.common.rfactory.VcgrCreate;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.invoker.timing.Timer;
import edu.virginia.vcgr.genii.container.invoker.timing.TimingSink;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rns.RNSContainerUtilities;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileRequestType;
import edu.virginia.vcgr.genii.enhancedrns.CreateFileResponseType;
import edu.virginia.vcgr.genii.exportdir.ExportedDirPortType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = ExportedDirDBResourceProvider.class)
public class ExportedDirServiceImpl extends GenesisIIBase implements ExportedDirPortType
{
	static private Log _logger = LogFactory.getLog(ExportedDirServiceImpl.class);

	@MInject(lazy = true)
	private IExportedDirResource _resource;

	@MInject
	private ResourceLock _resourceLock;

	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return false;
	}
	
	public ExportedDirServiceImpl() throws RemoteException
	{
		this("ExportedDirPortType");
	}

	protected ExportedDirServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(WellKnownPortTypes.EXPORTED_DIR_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.RNS_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.ENHANCED_RNS_PORT_TYPE());
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.EXPORTED_DIR_SERVICE_PORT_TYPE();
	}

	@Override
	protected ResourceKey createResource(GenesisHashMap constructionParameters) throws ResourceException, BaseFaultType
	{
		if (_logger.isDebugEnabled())
			_logger.debug("Creating new ExportedDir Resource.");

		if (constructionParameters == null) {
			ResourceCreationFaultType rcft = new ResourceCreationFaultType(null, null, null, null, new BaseFaultTypeDescription[] {
				new BaseFaultTypeDescription("Could not create ExportedDir resource without cerationProperties") }, null);
			throw FaultManipulator.fillInFault(rcft);
		}

		ExportedDirUtils.ExportedDirInitInfo initInfo = null;
		initInfo = ExportedDirUtils.extractCreationProperties(constructionParameters);

		constructionParameters.put(IExportedDirResource.PATH_CONSTRUCTION_PARAM, initInfo.getPath());
		constructionParameters.put(IExportedDirResource.PARENT_IDS_CONSTRUCTION_PARAM, initInfo.getParentIds());
		constructionParameters.put(IExportedDirResource.REPLICATION_INDICATOR, initInfo.getReplicationState());
		constructionParameters.put(IExportedDirResource.LAST_MODIFIED_TIME, initInfo.getLastModifiedTime());

		return super.createResource(constructionParameters);
	}

	/*
	 * Looks like this is dead code -- mmm2a protected void fillIn(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters
	 * cParams, GenesisHashMap creationParameters, Collection<MessageElement> resolverCreationParams) throws ResourceException, BaseFaultType,
	 * RemoteException { super.postCreate(rKey, newEPR, cParams, creationParameters, resolverCreationParams);
	 * 
	 * Date d = new Date(); Calendar c = Calendar.getInstance(); c.setTime(d);
	 * 
	 * IExportedDirResource resource = (IExportedDirResource)rKey.dereference(); resource.setCreateTime(c); resource.setModTime(c);
	 * resource.setAccessTime(c); }
	 */

	@RWXMapping(RWXCategory.EXECUTE)
	public CreateFileResponseType createFile(CreateFileRequestType createFileRequest)
		throws RemoteException, RNSEntryExistsFaultType, ResourceUnknownFaultType
	{
		String filename = null;
		EndpointReferenceType entryReference = null;
		filename = createFileRequest.getFilename();

		try {
			_resourceLock.lock();

			String fullPath = ExportedFileUtils.createFullPath(_resource.getLocalPath(), filename);
			String parentIds = ExportedDirUtils.createParentIdsString(_resource.getParentIds(), _resource.getId());

			// 2014-11-05 ASG - adding logging

			String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
			StatsLogger.logStats("LightWeightExport: Create file from " + caller);
			// End logging

			try {
				if (!(new File(fullPath).createNewFile())) {
					throw FaultManipulator.fillInFault(new RNSEntryExistsFaultType(null, null, null, null, null, null, filename));
				}
			} catch (IOException ioe) {
				throw new RemoteException(String.format("Unable to create new file on disk (%s).", fullPath), ioe);
			}

			try {
				WorkingContext.temporarilyAssumeNewIdentity(EPRUtils.makeEPR(Container.getServiceURL("ExportedFilePortType"), false));

				entryReference = new ExportedFileServiceImpl()
					.vcgrCreate(
						new VcgrCreate(ExportedFileUtils.createCreationProperties(fullPath, parentIds, _resource.getReplicationState())))
					.getEndpoint();

				String newEntryId = (new GUID()).toString();
				ExportedDirEntry newEntry =
					new ExportedDirEntry(_resource.getId(), filename, entryReference, newEntryId, ExportedDirEntry._FILE_TYPE, null);
				_resource.addEntry(newEntry, false);
				_resource.commit();
			} catch (Throwable t) {
				_logger.warn("Cleaning up state from a failed export file create.", t);

				if (entryReference != null) {
					try {
						GeniiCommon common = ClientUtils.createProxy(GeniiCommon.class, entryReference);
						common.destroy(new Destroy());
					} catch (Throwable tt) {
						_logger.error("Unable to clean up bogus file object.", tt);
					}
				}

				new File(fullPath).delete();

				if (t instanceof RemoteException)
					throw ((RemoteException) t);
				else if (t instanceof RuntimeException)
					throw (RuntimeException) t;
				else
					throw new RemoteException("" + "Exception occurred while creating exported file.", t);
			} finally {
				WorkingContext.releaseAssumedIdentity();
			}
		} finally {
			_resourceLock.unlock();
		}

		return new CreateFileResponseType(entryReference);
	}

	protected RNSEntryResponseType add(RNSEntryType addRequest) throws RemoteException
	{
		// add request missing
		if (addRequest == null) {
			// Pure factory operation
			throw FaultManipulator.fillInFault(new BaseFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Pure factory version of add not allowed in export dir.") },
				null));
		}

		// decipher add request
		// get name of file
		String name = addRequest.getEntryName();
		EndpointReferenceType entryReference = addRequest.getEndpoint();
		RNSMetadataType mdt = addRequest.getMetadata();
		MessageElement[] attrs = (mdt == null) ? null : mdt.get_any();

		if (entryReference != null) {
			throw FaultManipulator
				.fillInFault(new BaseFaultType(null, null, null, null,
					new BaseFaultTypeDescription[] {
						new BaseFaultTypeDescription("Add not allowed in ExportDirs (unless you are creating " + "a new directory.") },
					null));
		}

		if (_logger.isDebugEnabled())
			_logger.debug("ExportDir asked to add \"" + name + "\".");

		EndpointReferenceType newRef;

		try {
			_resourceLock.lock();
			String fullPath = ExportedFileUtils.createFullPath(_resource.getLocalPath(), name);
			String parentIds = ExportedDirUtils.createParentIdsString(_resource.getParentIds(), _resource.getId());
			String isReplicated = _resource.getReplicationState();

			// find out the owner of the export from the preferred id.
			PreferredIdentity current = PreferredIdentity.getCurrent();
			String owner = current != null ? current.getIdentityString() : null;
			if (_logger.isDebugEnabled())
				_logger.debug("got preferred identity for new export: '" + owner + "'");

			newRef = vcgrCreate(
				new VcgrCreate(ExportedDirUtils.createCreationProperties(null, fullPath, null, null, null, parentIds, isReplicated, owner)))
					.getEndpoint();

			String newEntryId = (new GUID()).toString();
			ExportedDirEntry newEntry = new ExportedDirEntry(_resource.getId(), name, newRef, newEntryId, ExportedDirEntry._DIR_TYPE, attrs);
			_resource.addEntry(newEntry, true);
			_resource.commit();
		} finally {
			_resourceLock.unlock();
		}

		// get and set modify time for newly created Dir
		ResourceKey newRefKey = ResourceManager.getTargetResource(newRef);
		IExportedDirResource newResource = (IExportedDirResource) newRefKey.dereference();
		newResource.getAndSetModifyTime();

		return new RNSEntryResponseType(newRef, mdt, null, name);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] add(RNSEntryType[] addRequest) throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		if (addRequest == null || addRequest.length == 0)
			addRequest = new RNSEntryType[] { null };

		RNSEntryResponseType[] ret = new RNSEntryResponseType[addRequest.length];
		for (int lcv = 0; lcv < ret.length; lcv++) {
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

	@RWXMapping(RWXCategory.READ)
	public LookupResponseType lookup(String[] lookupRequest) throws RemoteException, org.ggf.rns.ReadNotPermittedFaultType
	{
		TimingSink tSink = TimingSink.sink();
		Timer timer = null;
		Collection<RNSEntryResponseType> entryCollection;
		Collection<ExportedDirEntry> entries = new LinkedList<ExportedDirEntry>();

		if (lookupRequest == null || lookupRequest.length == 0)
			lookupRequest = new String[] { null };

		try {
			_resourceLock.lock();

			timer = tSink.getTimer("Retrieve Entries");
			for (String request : lookupRequest)
				entries.addAll(_resource.retrieveEntries(request));
			timer.noteTime();
		} finally {
			_resourceLock.unlock();
		}

		// create collection of MessageElement entries
		entryCollection = new LinkedList<RNSEntryResponseType>();
		timer = tSink.getTimer("Prepare Entries");
		for (ExportedDirEntry exportDirEntry : entries) {
			RNSEntryResponseType entry = new RNSEntryResponseType(exportDirEntry.getEntryReference(),
				RNSUtilities.createMetadata(exportDirEntry.getEntryReference(), exportDirEntry.getAttributes()), null,
				exportDirEntry.getName());

			entryCollection.add(entry);
		}
		timer.noteTime();

		try {
			timer = tSink.getTimer("Create Iterator");
			return RNSContainerUtilities.translate(entryCollection, iteratorBuilder(RNSEntryResponseType.getTypeDesc().getXmlType()));
		} finally {
			if (timer != null)
				timer.noteTime();
		}
	}

	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] rename(NameMappingType[] renameRequest) throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException("Rename not supported in Resource forks!");
	}

	@RWXMapping(RWXCategory.WRITE)
	public RNSEntryResponseType[] remove(String[] removeRequest) throws RemoteException, org.ggf.rns.WriteNotPermittedFaultType
	{
		RNSEntryResponseType[] ret = new RNSEntryResponseType[removeRequest.length];

		try {
			_resourceLock.lock();
			for (int lcv = 0; lcv < removeRequest.length; lcv++) {
				_resource.removeEntries(removeRequest[lcv], true);
				ret[lcv] = new RNSEntryResponseType(null, null, null, removeRequest[lcv]);
			}

			_resource.commit();
		} finally {
			_resourceLock.unlock();
		}

		return ret;
	}

	@Override
	public RNSEntryResponseType[] setMetadata(MetadataMappingType[] setMetadataRequest) throws RemoteException, WriteNotPermittedFaultType
	{
		throw new UnsupportedOperationException("setMetadata not supported!");
	}
}