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
package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.byteio.CustomFaultType;
import org.ggf.byteio.ReadNotPermittedFaultType;
import org.ggf.byteio.TransferInformationType;
import org.ggf.byteio.UnsupportedTransferFaultType;
import org.ggf.byteio.WriteNotPermittedFaultType;
import org.ggf.rbyteio.Append;
import org.ggf.rbyteio.AppendResponse;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ggf.rbyteio.Read;
import org.ggf.rbyteio.ReadResponse;
import org.ggf.rbyteio.TruncAppend;
import org.ggf.rbyteio.TruncAppendResponse;
import org.ggf.rbyteio.TruncateNotPermittedFaultType;
import org.ggf.rbyteio.Write;
import org.ggf.rbyteio.WriteResponse;
import org.morgan.inject.MInject;
import org.morgan.util.io.StreamUtils;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOperations;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.GenesisHashMap;
import edu.virginia.vcgr.genii.client.context.WorkingContext;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.ResourceLock;
import edu.virginia.vcgr.genii.client.sync.SyncProperty;
import edu.virginia.vcgr.genii.client.sync.VersionVector;
import edu.virginia.vcgr.genii.client.utils.StatsLogger;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOAttributesUpdateNotification;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOContentsChangedContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.ByteIOTopics;
import edu.virginia.vcgr.genii.container.axis.ServerAuthorizationManagement;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.security.authz.providers.AclTopics;
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
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = RByteIOResourceProvider.class)
public class RandomByteIOServiceImpl extends GenesisIIBase implements RandomByteIOPortType, ByteIOTopics, AclTopics
{
	static private Log _logger = LogFactory.getLog(RandomByteIOServiceImpl.class);
	static private final int VALID_BLOCK_SIZE = 1024;
	
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return true;
	}

	@MInject(lazy = true)
	private IRByteIOResource _resource;

	@MInject
	private ResourceLock _resourceLock;

	@Override
	protected void setAttributeHandlers() throws NoSuchMethodException, ResourceException, ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		new VersionedResourceAttributeHandlers(getAttributePackage());
		new RandomByteIOAttributeHandlers(getAttributePackage());
	}

	public RandomByteIOServiceImpl() throws RemoteException
	{
		this("RandomByteIOPortType");
	}

	protected RandomByteIOServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);
		addImplementedPortType(WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE());
		addImplementedPortType(WellKnownPortTypes.GENII_NOTIFICATION_CONSUMER_PORT_TYPE());
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE();
	}

	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		GenesisHashMap creationParameters, Collection<MessageElement> resolverCreationParams)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, newEPR, cParams, creationParameters, resolverCreationParams);

		IRByteIOResource resource = (IRByteIOResource) rKey.dereference();
		resource.chooseFile(creationParameters);

		EndpointReferenceType primaryEPR = (EndpointReferenceType) creationParameters.get(IResource.PRIMARY_EPR_CONSTRUCTION_PARAM);
		if (primaryEPR != null) {
			VersionedResourceUtils.initializeReplica(resource, primaryEPR, 0);
			WorkingContext context = WorkingContext.getCurrentWorkingContext();
			ReplicationThread thread = new ReplicationThread(context);
			thread.add(new ReplicationItem(new GeniiFileSyncRunner(), newEPR));
			thread.start();
		} else {
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			resource.setCreateTime(c);
			resource.setModTime(c);
			resource.setAccessTime(c);
		}
	}

	@Override
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();

		DestroyFlags flags = VersionedResourceUtils.preDestroy(_resource);
		if (flags != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("RandomByteIOServiceImpl: publish destroy notification");
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic publisherTopic = space.createPublisherTopic(BYTEIO_CONTENTS_CHANGED_TOPIC);
			ByteIOOperations operation = (flags.isUnlinked ? ByteIOOperations.Unlink : ByteIOOperations.Destroy);
			publisherTopic.publish(new ByteIOContentsChangedContents(operation, 0, 0, 0, 0, flags.vvr));
		}
	}

	@RWXMapping(RWXCategory.READ)
	public ReadResponse read(Read read)
		throws RemoteException, CustomFaultType, ReadNotPermittedFaultType, UnsupportedTransferFaultType, ResourceUnknownFaultType
	{
		if (_resource.getProperty(SyncProperty.ERROR_STATE_PROP_NAME) != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("read: resource in error state");
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(), "bad replica");
		}
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("RandomByteIO: Read from " + caller);
		// End logging
		int bytesPerBlock = read.getBytesPerBlock();
		int numBlocks = read.getNumBlocks();
		long startOffset = read.getStartOffset();
		long stride = read.getStride();
		TransferInformationType transferInformation = read.getTransferInformation();

		RandomAccessFile raf = null;
		try {
			_resourceLock.lock();
			byte[] data = new byte[bytesPerBlock * numBlocks];
			int off = 0;
			File myFile = _resource.getCurrentFile();
			for (int block = 0; block < numBlocks; block++) {
				downloadIfNecessary(startOffset, bytesPerBlock, myFile);
				raf = new RandomAccessFile(myFile, "r");
				raf.seek(startOffset);
				int r = readFully(raf, data, off, bytesPerBlock);
				raf.close();
				raf = null;
				if (r <= 0)
					break;
				off += r;
				startOffset += stride;
				if (r < bytesPerBlock)
					break;
			}
			if (off < data.length) {
				byte[] tmp = data;
				data = new byte[off];
				System.arraycopy(tmp, 0, data, 0, off);
			}
			TransferAgent.sendData(data, transferInformation);
		} catch (ResourceUnknownFaultType ruft) {
			throw FaultManipulator.fillInFault(ruft);
		} catch (IOException ioe) {
			throw FaultManipulator.fillInFault(new CustomFaultType(), ioe.toString());
		} finally {
			StreamUtils.close(raf);
			_resourceLock.unlock();
		}
		return new ReadResponse(transferInformation);
	}

	static private int readFully(RandomAccessFile raf, byte[] data, int off, int len) throws IOException
	{
		int totalRead = 0;
		int r;
		while ((r = raf.read(data, off, len)) >= 0) {
			off += r;
			len -= r;
			totalRead += r;
			if (len <= 0)
				break;
		}
		return totalRead;
	}

	/**
	 * Ensure that the local cache contains valid data from the given start offset.
	 * 
	 * This function always begins and ends with the resource locked, but the resource may be unlocked in the middle.
	 */
	private void downloadIfNecessary(long startOffset, int validSize, File cacheFile) throws IOException
	{
		File file = _resource.getBitmapFile();
		if (file == null)
			return;
		long fileSize = cacheFile.length();
		long maxSize = fileSize - startOffset;
		if (validSize > maxSize)
			validSize = (int) maxSize;
		long firstBlock = (long) (startOffset / VALID_BLOCK_SIZE);
		long lastBlock = (long) ((startOffset + validSize - 1) / VALID_BLOCK_SIZE);
		BitmapFile bitmapFile = null;
		List<PrimaryFileSegment> segmentList = new ArrayList<PrimaryFileSegment>();
		try {
			bitmapFile = new BitmapFile(file, false);
			bitmapFile.seekBit(firstBlock);
			while (firstBlock <= lastBlock) {
				int isValid = bitmapFile.readBit();
				if (isValid == 1) {
					firstBlock++;
					continue;
				}
				int blockCount = 1;
				while (firstBlock + blockCount <= lastBlock) {
					isValid = bitmapFile.readBit();
					if (isValid == 1)
						break;
					blockCount++;
					if (isValid == -1)
						blockCount = (int) (lastBlock - firstBlock) + 1;
				}
				segmentList.add(new PrimaryFileSegment(firstBlock, blockCount));
				firstBlock += blockCount + 1;
			}
		} finally {
			StreamUtils.close(bitmapFile);
		}
		if (segmentList.size() == 0) {
			return;
		}
		// future: why wasn't this done? incrementVersionLockCount(_resource);
		_resourceLock.unlock();
		try {
			EndpointReferenceType primaryEPR = EPRUtils.fromBytes((byte[]) _resource.getProperty(SyncProperty.PRIMARY_EPR_PROP_NAME));
			RandomByteIOPortType clientStub = ClientUtils.createProxy(RandomByteIOPortType.class, primaryEPR);
			RandomByteIOTransfererFactory factory = new RandomByteIOTransfererFactory(clientStub);
			RandomByteIOTransferer transferer = factory.createRandomByteIOTransferer();
			for (PrimaryFileSegment segment : segmentList) {
				segment.download(transferer, VALID_BLOCK_SIZE);
			}
		} finally {
			_resourceLock.lock();
			// future: why wasn't this here? decrementVersionLockCount(_resource);
		}
		RandomAccessFile raf = null;
		bitmapFile = null;
		try {
			raf = new RandomAccessFile(cacheFile, "rw");
			bitmapFile = new BitmapFile(file, true);
			for (PrimaryFileSegment segment : segmentList) {
				segment.write(bitmapFile, raf, VALID_BLOCK_SIZE);
			}
		} finally {
			StreamUtils.close(bitmapFile);
			StreamUtils.close(raf);
		}
	}

	@RWXMapping(RWXCategory.WRITE)
	public WriteResponse write(Write write)
		throws RemoteException, CustomFaultType, WriteNotPermittedFaultType, UnsupportedTransferFaultType, ResourceUnknownFaultType
	{
		if (_resource.getProperty(SyncProperty.ERROR_STATE_PROP_NAME) != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("write: resource in error state");
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(), "bad replica");
		}
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("RandomByteIO: Write from " + caller);
		// End logging
		long startOffset = write.getStartOffset();
		int bytesPerBlock = write.getBytesPerBlock();
		long stride = write.getStride();
		TransferInformationType transferInformation = write.getTransferInformation();
		byte[] data = TransferAgent.receiveData(transferInformation);
		RandomAccessFile raf = null;
		MessageElement[] byteIOAttrs = null;

		try {
			_resourceLock.lock();
			File myFile = _resource.getCurrentFile();
			long fileOffset = startOffset;
			int dataOffset = 0;
			while (dataOffset < data.length) {
				int toWrite = (data.length - dataOffset);
				if (toWrite > bytesPerBlock)
					toWrite = bytesPerBlock;
				raf = new RandomAccessFile(myFile, "rw");
				raf.seek(fileOffset);
				raf.write(data, dataOffset, toWrite);
				raf.close();
				raf = null;
				updateBitmap(fileOffset, toWrite, myFile.length());
				fileOffset += stride;
				dataOffset += toWrite;
			}

			VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(_resource);
			GeniiAttachment attachment = new GeniiAttachment(data);
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic publisherTopic = space.createPublisherTopic(BYTEIO_CONTENTS_CHANGED_TOPIC);
			publisherTopic.publish(
				new ByteIOContentsChangedContents(ByteIOOperations.Write, startOffset, bytesPerBlock, stride, data.length, vvr), attachment);
			byteIOAttrs = notifyAttributesUpdateAndGetMetadata(myFile, _resource);
		} catch (IOException ioe) {
			throw FaultManipulator.fillInFault(new CustomFaultType(), ioe.toString());
		} finally {
			StreamUtils.close(raf);
			_resourceLock.unlock();
		}
		return new WriteResponse(new TransferInformationType(byteIOAttrs, transferInformation.getTransferMechanism()));
	}

	@RWXMapping(RWXCategory.WRITE)
	public AppendResponse append(Append append)
		throws RemoteException, CustomFaultType, WriteNotPermittedFaultType, UnsupportedTransferFaultType, ResourceUnknownFaultType
	{
		if (_resource.getProperty(SyncProperty.ERROR_STATE_PROP_NAME) != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("append: resource in error state");
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(), "bad replica");
		}
		long startOffset = 0;
		TransferInformationType transferInformation = append.getTransferInformation();
		byte[] data = TransferAgent.receiveData(transferInformation);
		RandomAccessFile raf = null;
		MessageElement[] byteIOAttrs = null;

		try {
			_resourceLock.lock();
			File myFile = _resource.getCurrentFile();
			raf = new RandomAccessFile(myFile, "rw");
			startOffset = myFile.length();
			raf.seek(startOffset);
			raf.write(data);
			raf.close();
			raf = null;
			updateBitmap(startOffset, data.length, myFile.length());

			VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(_resource);
			GeniiAttachment attachment = new GeniiAttachment(data);
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic publisherTopic = space.createPublisherTopic(BYTEIO_CONTENTS_CHANGED_TOPIC);
			publisherTopic.publish(new ByteIOContentsChangedContents(ByteIOOperations.Append, startOffset, data.length, 0, data.length, vvr),
				attachment);
			byteIOAttrs = notifyAttributesUpdateAndGetMetadata(myFile, _resource);
		} catch (IOException ioe) {
			throw FaultManipulator.fillInFault(new CustomFaultType(), ioe.toString());
		} finally {
			StreamUtils.close(raf);
			_resourceLock.unlock();
		}
		return new AppendResponse(new TransferInformationType(byteIOAttrs, append.getTransferInformation().getTransferMechanism()));
	}

	@RWXMapping(RWXCategory.WRITE)
	public TruncAppendResponse truncAppend(TruncAppend truncAppend) throws RemoteException, CustomFaultType, WriteNotPermittedFaultType,
		TruncateNotPermittedFaultType, UnsupportedTransferFaultType, ResourceUnknownFaultType
	{
		if (_resource.getProperty(SyncProperty.ERROR_STATE_PROP_NAME) != null) {
			if (_logger.isDebugEnabled())
				_logger.debug("truncAppend: resource in error state");
			throw FaultManipulator.fillInFault(new ResourceUnknownFaultType(), "bad replica");
		}
		// 2014-11-05 ASG - adding logging
		String caller = (String) WorkingContext.getCurrentWorkingContext().getProperty(WorkingContext.CALLING_HOST);
		StatsLogger.logStats("RandomByteIO: TruncAppend from " + caller);
		// End logging
		long startOffset = truncAppend.getOffset();
		TransferInformationType transferInformation = truncAppend.getTransferInformation();
		byte[] data = TransferAgent.receiveData(transferInformation);
		RandomAccessFile raf = null;
		MessageElement[] byteIOAttrs = null;

		try {
			_resourceLock.lock();
			File myFile = _resource.getCurrentFile();
			raf = new RandomAccessFile(myFile, "rw");
			raf.setLength(startOffset);
			raf.seek(startOffset);
			raf.write(data);
			raf.close();
			raf = null;
			updateBitmap(startOffset, data.length, myFile.length());

			VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(_resource);
			GeniiAttachment attachment = new GeniiAttachment(data);
			TopicSet space = TopicSet.forPublisher(getClass());
			PublisherTopic publisherTopic = space.createPublisherTopic(BYTEIO_CONTENTS_CHANGED_TOPIC);
			publisherTopic.publish(
				new ByteIOContentsChangedContents(ByteIOOperations.TruncAppend, startOffset, data.length, 0, data.length, vvr), attachment);
			byteIOAttrs = notifyAttributesUpdateAndGetMetadata(myFile, _resource);
		} catch (IOException ioe) {
			throw FaultManipulator.fillInFault(new CustomFaultType(), ioe.toString());
		} finally {
			StreamUtils.close(raf);
			_resourceLock.unlock();
		}
		return new TruncAppendResponse(new TransferInformationType(byteIOAttrs, truncAppend.getTransferInformation().getTransferMechanism()));
	}

	/**
	 * For each block that was completely overwritten with valid data, mark the block as valid.
	 */
	private void updateBitmap(long firstByte, int size, long fileSize) throws ResourceException, IOException
	{
		File file = _resource.getBitmapFile();
		if (file == null)
			return;
		long firstBlock = (long) (firstByte / VALID_BLOCK_SIZE);
		long firstByteOfBlock = firstBlock * VALID_BLOCK_SIZE;
		if (firstByteOfBlock < firstByte)
			firstBlock++;
		long lastByte = firstByte + size - 1;
		long lastBlock = (long) (lastByte / VALID_BLOCK_SIZE);
		long lastByteOfBlock = (lastBlock + 1) * VALID_BLOCK_SIZE - 1;
		if ((lastByte < lastByteOfBlock) && (lastByte < fileSize))
			lastBlock--;
		if (_logger.isDebugEnabled())
			_logger.debug("write (" + firstByte + ", " + size + "): blocks " + firstBlock + " - " + lastBlock);
		if (lastBlock < firstBlock)
			return;
		BitmapFile bitmapFile = null;
		try {
			bitmapFile = new BitmapFile(file, true);
			bitmapFile.seekBit(firstBlock);
			for (long bnum = firstBlock; bnum <= lastBlock; bnum++)
				bitmapFile.writeBit(1);
		} finally {
			StreamUtils.close(bitmapFile);
		}
	}

	@Override
	public ResourceSyncRunner getClassResourceSyncRunner()
	{
		return new GeniiFileSyncRunner();
	}

	@Override
	protected void registerNotificationHandlers(NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);
		multiplexer.registerNotificationHandler(ByteIOTopics.BYTEIO_CONTENTS_CHANGED_TOPIC.asConcreteQueryExpression(),
			new ByteIOContentsChangedNotificationHandler());
		multiplexer.registerNotificationHandler(AclTopics.GENII_ACL_CHANGE_TOPIC.asConcreteQueryExpression(),
			new AclChangeNotificationHandler());
	}

	private class ByteIOContentsChangedNotificationHandler extends AbstractNotificationHandler<ByteIOContentsChangedContents>
	{
		private ByteIOContentsChangedNotificationHandler()
		{
			super(ByteIOContentsChangedContents.class);
		}

		@Override
		public String handleNotification(TopicPath topicPath, EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference, ByteIOContentsChangedContents contents) throws Exception
		{
			ByteIOOperations operation = contents.operation();
			long offset = contents.offset();
			int bytesPerBlock = contents.bytesPerBlock();
			long stride = contents.stride();
			int size = contents.size();
			VersionVector remoteVector = contents.versionVector();
			if (!(operation.equals(ByteIOOperations.Write) || operation.equals(ByteIOOperations.Append)
				|| operation.equals(ByteIOOperations.TruncAppend) || operation.equals(ByteIOOperations.Destroy)
				|| operation.equals(ByteIOOperations.Unlink))) {
				if (_logger.isDebugEnabled())
					_logger.debug("RandomByteIOServiceImpl.notify: invalid parameters");
				return NotificationConstants.FAIL;
			}
			IRByteIOResource resource = _resource;
			// The notification contains the identity of the user who modified the resource.
			// The identity is delegated to the resource that sent the notification.
			// The notification is signed by the resource.
			if (!ServerAuthorizationManagement.checkAccess(resource, RWXCategory.WRITE)) {
				if (_logger.isDebugEnabled())
					_logger.debug("RandomByteIOServiceImpl.notify: permission denied");
				return NotificationConstants.FAIL;
			}
			byte[] data = null;
			try {
				data = TransferAgent.extractAttachmentData();
				if (_logger.isDebugEnabled())
					_logger.debug("RandomByteIOServiceImpl.notify: data.length=" + (data == null ? 0 : data.length));
			} catch (Exception exception) {
				// Should we ask the caller to try again?
				if (_logger.isDebugEnabled())
					_logger.debug("RandomByteIOServiceImpl.notify: attachment failure", exception);
				return NotificationConstants.FAIL;
			}
			RandomAccessFile raf = null;
			boolean replay;
			try {
				_resourceLock.lock();
				if (operation.equals(ByteIOOperations.Destroy)) {
					resource.setProperty(SyncProperty.IS_DESTROYED_PROP_NAME, "true");
					destroy(new Destroy());
					return NotificationConstants.OK;
				}
				VersionVector localVector = (VersionVector) resource.getProperty(SyncProperty.VERSION_VECTOR_PROP_NAME);
				MessageFlags flags = VersionedResourceUtils.validateNotification(resource, localVector, remoteVector);
				if (flags.status != null)
					return flags.status;
				if (operation.equals(ByteIOOperations.Unlink)) {
					// future: Destroy subscription from sender.
					// future: Also, remove outcalls to sender from persistent queue?
					return NotificationConstants.OK;
				}
				File bitmapFile = resource.getBitmapFile();
				if (size > 0) {
					if (((data == null) && (bitmapFile == null)) || ((data != null) && (data.length != size))) {
						if (_logger.isDebugEnabled())
							_logger.debug("RandomByteIOServiceImpl.notify: attachment size failure, " + "expected " + size);
						return NotificationConstants.FAIL;
					}
				}
				File myFile = resource.getCurrentFile();
				raf = new RandomAccessFile(myFile, "rw");
				if (operation.equals(ByteIOOperations.Append) || operation.equals(ByteIOOperations.TruncAppend))
					raf.setLength(offset);
				if (bitmapFile != null)
					updateBitmap(bitmapFile, raf, offset, bytesPerBlock, stride, size);
				if (data != null) {
					int dataOffset = 0;
					while (dataOffset < data.length) {
						int toWrite = (data.length - dataOffset);
						if (toWrite > bytesPerBlock)
							toWrite = bytesPerBlock;
						raf.seek(offset);
						raf.write(data, dataOffset, toWrite);
						offset += stride;
						dataOffset += toWrite;
					}
				}
				raf.close();
				raf = null;
				VersionedResourceUtils.updateVersionVector(resource, localVector, remoteVector);
				replay = flags.replay;
			} catch (Exception ioe) {
				_logger.error("RandomByteIOServiceImpl.notify: error writing data", ioe);
				resource.setProperty(SyncProperty.ERROR_STATE_PROP_NAME, "error");
				return NotificationConstants.FAIL;
			} finally {
				StreamUtils.close(raf);
				_resourceLock.unlock();
			}
			if (replay) {
				VersionVector vvr = VersionedResourceUtils.incrementResourceVersion(resource);
				GeniiAttachment attachment = new GeniiAttachment(data);
				if (_logger.isDebugEnabled())
					_logger.debug("RandomByteIOServiceImpl.notify: replay message");
				TopicSet space = TopicSet.forPublisher(RandomByteIOServiceImpl.class);
				PublisherTopic publisherTopic = space.createPublisherTopic(topicPath);
				publisherTopic.publish(new ByteIOContentsChangedContents(operation, offset, bytesPerBlock, stride, size, vvr), attachment);
			}
			return NotificationConstants.OK;
		}

		/**
		 * For each block that would be written, clear that block in the bitmap. If this would write past the end of the file, then pad the
		 * file to its new length.
		 */
		private void updateBitmap(File file, RandomAccessFile raf, long offset, int bytesPerBlock, long stride, int dataSize)
			throws IOException
		{
			BitmapFile bitmapFile = null;
			long fileSize = 0;
			try {
				bitmapFile = new BitmapFile(file, true);
				int dataOffset = 0;
				while (dataOffset < dataSize) {
					int toWrite = (dataSize - dataOffset);
					if (toWrite > bytesPerBlock)
						toWrite = bytesPerBlock;
					long firstBlock = (long) (offset / VALID_BLOCK_SIZE);
					long lastByte = offset + toWrite - 1;
					long lastBlock = (long) (lastByte / VALID_BLOCK_SIZE);
					bitmapFile.seekBit(firstBlock);
					for (long bnum = firstBlock; bnum <= lastBlock; bnum++)
						bitmapFile.writeBit(0);
					fileSize = offset + toWrite;
					offset = fileSize + stride;
					dataOffset += toWrite;
				}
			} finally {
				StreamUtils.close(bitmapFile);
			}
			if (raf.length() < fileSize)
				raf.setLength(fileSize);
		}
	}

	/*
	 * This method publish attributes update notification message and also return the updated attributes as set of MessageElements. We use a
	 * single function instead of two for this two operations in order to reduce the number of database access.
	 */
	private MessageElement[] notifyAttributesUpdateAndGetMetadata(File currentFile, IRByteIOResource resource) throws ResourceException
	{

		MessageElement[] attributes = new MessageElement[4];

		TopicSet space = TopicSet.forPublisher(getClass());
		PublisherTopic publisherTopic = space.createPublisherTopic(BYTEIO_ATTRIBUTES_UPDATE_TOPIC);
		ByteIOAttributesUpdateNotification message = new ByteIOAttributesUpdateNotification();

		long fileSize = currentFile.length();
		message.setSize(fileSize);
		attributes[0] = new MessageElement(ByteIOConstants.rsize, fileSize);

		Calendar createTime = resource.getCreateTime();
		message.setCreateTime(createTime);
		attributes[1] = new MessageElement(ByteIOConstants.rcreatTime, createTime);

		Calendar modTime = resource.getModTime();
		message.setModificationTime(modTime);
		attributes[2] = new MessageElement(ByteIOConstants.rmodTime, modTime);

		Calendar accessTime = resource.getAccessTime();
		message.setAccessTime(accessTime);
		attributes[3] = new MessageElement(ByteIOConstants.raccessTime, accessTime);

		publisherTopic.publish(message);
		return attributes;
	}
}