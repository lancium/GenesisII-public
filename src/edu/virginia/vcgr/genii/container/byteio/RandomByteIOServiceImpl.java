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
package edu.virginia.vcgr.genii.container.byteio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.xml.namespace.QName;

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
import org.morgan.util.io.StreamUtils;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.notification.WellknownTopics;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.client.notification.InvalidTopicException;
import edu.virginia.vcgr.genii.client.notification.UnknownTopicException;

import org.apache.axis.message.MessageElement;

public class RandomByteIOServiceImpl extends GenesisIIBase
	implements RandomByteIOPortType
{
	static private Log _logger = LogFactory.getLog(RandomByteIOServiceImpl.class);
	
	protected void setAttributeHandlers() throws NoSuchMethodException
	{
		super.setAttributeHandlers();
		
		new RandomByteIOAttributeHandlers(getAttributePackage());
	}
	
	public RandomByteIOServiceImpl() throws RemoteException
	{
		super("RandomByteIOPortType");
		
		addImplementedPortType(
			WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
	}
	
	protected RandomByteIOServiceImpl(String serviceName) throws RemoteException
	{
		super(serviceName);

		addImplementedPortType(
				WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE);
	}
	
	public QName getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.RBYTEIO_SERVICE_PORT_TYPE;
	}
	
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR,
		HashMap<QName, Object> creationParameters,
		Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{
		_logger.debug("Creating new RandomByteIO Resource.");
		
		super.postCreate(rKey, newEPR, creationParameters, resolverCreationParams);
		
		IRByteIOResource resource = null;
		
		resource = (IRByteIOResource)rKey.dereference();
		resource.chooseFile(creationParameters);
		
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		
		resource.setCreateTime(c);
		resource.setModTime(c);
		resource.setAccessTime(c);
	}
	
	static private int readFully(RandomAccessFile raf,
		byte []data, int off, int len) throws IOException
	{
		int r;
		int totalRead = 0;
		
		while ( (r = raf.read(data, off, len)) >= 0)
		{
			off += r;
			len -= r;
			totalRead += r;
			
			if (len <= 0)
				return totalRead;
		}
		
		return totalRead;
	}
	
	@RWXMapping(RWXCategory.READ)
	public ReadResponse read(Read read) 
		throws RemoteException, CustomFaultType, 
			ReadNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		int bytesPerBlock = read.getBytesPerBlock();
		int numBlocks = read.getNumBlocks();
		long startOffset = read.getStartOffset();
		long stride = read.getStride();
		TransferInformationType transferInformation = read.getTransferInformation();
		
		byte []data = new byte[bytesPerBlock * numBlocks];
		int off = 0;
		int r;
		File myFile = null;
		RandomAccessFile raf = null;
		IRByteIOResource resource = null;
		
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRByteIOResource)rKey.dereference();
		myFile = resource.getCurrentFile();
		synchronized(rKey.getLockObject())
		{
			try
			{
				raf = new RandomAccessFile(myFile, "r");
				
				for (int block = 0; block < numBlocks; block++)
				{
					raf.seek(startOffset);
					r = readFully(raf, data, off, bytesPerBlock);
					if (r <= 0)
						break;
					else if (r < bytesPerBlock)
					{
						off += r;
						break;
					}
					
					off += r;
					startOffset += stride;
				}
					
				if (off < data.length)
				{
					byte []tmp = data;
					data = new byte[off];
					System.arraycopy(tmp, 0, data, 0, off);
				}
					
				TransferAgent.sendData(data, transferInformation);
			}
			catch(ResourceUnknownFaultType ruft){
				throw FaultManipulator.fillInFault(ruft);
			}
			catch (IOException ioe)
			{
				throw FaultManipulator.fillInFault(
					new CustomFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(ioe.toString())
					}, null));
			}
			finally
			{
				StreamUtils.close(raf);
			}
		}
		
		return new ReadResponse(transferInformation);
	}

	@RWXMapping(RWXCategory.WRITE)
	public WriteResponse write(Write write) 
		throws RemoteException, CustomFaultType, 
			WriteNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		long startOffset = write.getStartOffset();
		int bytesPerBlock = write.getBytesPerBlock();
		long stride = write.getStride();
		TransferInformationType transferInformation = write.getTransferInformation();
		
		byte []data = TransferAgent.receiveData(
			transferInformation);
		int off = 0;
		File myFile = null;
		RandomAccessFile raf = null;
		IRByteIOResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		resource = (IRByteIOResource)rKey.dereference();
		myFile = resource.getCurrentFile();
		synchronized(rKey.getLockObject())
		{
			try
			{
				raf = new RandomAccessFile(myFile, "rw");
				while (off < data.length)
				{
					int toWrite = (data.length - off);
					if (toWrite > bytesPerBlock)
						toWrite = bytesPerBlock;
							
					raf.seek(startOffset);
					raf.write(data, off, toWrite);
					startOffset += stride;
					off += toWrite;
				}
				
				//notify of rbyteio write event
				try{
					MessageElement []payload = new MessageElement[1];
			    	
					payload[0] = new MessageElement(
			    		new QName(GenesisIIConstants.GENESISII_NS, "operation"),
			    		"write");
			    	
			    	getTopicSpace().getTopic(WellknownTopics.RANDOM_BYTEIO_OP).notifyAll(
			    		payload);
			    	
			    	_logger.info("RandomByteIO write notification sent");
				}
				catch (InvalidTopicException ite){
					_logger.warn(ite.getLocalizedMessage(), ite);
				}
				catch (UnknownTopicException ute){
					_logger.warn(ute.getLocalizedMessage(), ute);
				}
			}
			catch (IOException ioe)
			{
				throw FaultManipulator.fillInFault(
					new CustomFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(ioe.toString())
					}, null));
			}
			finally
			{
				StreamUtils.close(raf);
			}
		}
		
		return new WriteResponse(new TransferInformationType(null,
				transferInformation.getTransferMechanism()));
	}

	@RWXMapping(RWXCategory.WRITE)
	public AppendResponse append(Append append) 
		throws RemoteException, CustomFaultType, 
			WriteNotPermittedFaultType, UnsupportedTransferFaultType, 
			ResourceUnknownFaultType
	{
		byte []data = TransferAgent.receiveData(append.getTransferInformation());
		File myFile = null;
		RandomAccessFile raf = null;
		IRByteIOResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		resource = (IRByteIOResource)rKey.dereference();
		myFile = resource.getCurrentFile();
		synchronized(rKey.getLockObject())
		{
			try
			{
				raf = new RandomAccessFile(myFile, "rw");
				raf.seek(myFile.length());
				raf.write(data);
				
				//notify of append
				try{
					MessageElement []payload = new MessageElement[1];
			    	
					payload[0] = new MessageElement(
			    		new QName(GenesisIIConstants.GENESISII_NS, "operation"),
			    		"append");
			    	
			    	getTopicSpace().getTopic(WellknownTopics.RANDOM_BYTEIO_OP).notifyAll(
			    		payload);
			    	
			    	_logger.info("RandomByteIO append notification sent");
				}
				catch (InvalidTopicException ite){
					_logger.warn(ite.getLocalizedMessage(), ite);
				}
				catch (UnknownTopicException ute){
					_logger.warn(ute.getLocalizedMessage(), ute);
				}
				
				return new AppendResponse(new TransferInformationType(null,
					append.getTransferInformation().getTransferMechanism()));
			}
			catch (IOException ioe)
			{
				throw FaultManipulator.fillInFault(
					new CustomFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(ioe.toString())
					}, null));
			}
			finally
			{
				StreamUtils.close(raf);
			}
		}
	}

	@RWXMapping(RWXCategory.WRITE)
	public TruncAppendResponse truncAppend(TruncAppend truncAppend) 
		throws RemoteException, CustomFaultType, 
			WriteNotPermittedFaultType, TruncateNotPermittedFaultType, 
			UnsupportedTransferFaultType, ResourceUnknownFaultType
	{
		byte []data = TransferAgent.receiveData(
			truncAppend.getTransferInformation());
		File myFile = null;
		RandomAccessFile raf = null;
		IRByteIOResource resource = null;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		resource = (IRByteIOResource)rKey.dereference();
		myFile = resource.getCurrentFile();
		synchronized(rKey.getLockObject())
		{
			try
			{
				raf = new RandomAccessFile(myFile, "rw");
				raf.setLength(truncAppend.getOffset());
				raf.seek(truncAppend.getOffset());
				raf.write(data);
				
				//notify rbyteio truncappend event
				try{
					MessageElement []payload = new MessageElement[1];
			    	
					payload[0] = new MessageElement(
			    		new QName(GenesisIIConstants.GENESISII_NS, "operation"),
			    		"truncappend");
			    	
			    	getTopicSpace().getTopic(WellknownTopics.RANDOM_BYTEIO_OP).notifyAll(
			    		payload);
			    	
			    	_logger.info("RandomByteIO truncAppend notification sent");
				}
				catch (InvalidTopicException ite){
					_logger.warn(ite.getLocalizedMessage(), ite);
				}
				catch (UnknownTopicException ute){
					_logger.warn(ute.getLocalizedMessage(), ute);
				}
				
			}
			catch (IOException ioe)
			{
				throw FaultManipulator.fillInFault(
					new CustomFaultType(null, null, null, null,
						new BaseFaultTypeDescription[] {
							new BaseFaultTypeDescription(ioe.toString())
					}, null));
			}
			finally
			{
				StreamUtils.close(raf);
			}
		}
		
		return new TruncAppendResponse(new TransferInformationType(null,
				truncAppend.getTransferInformation().getTransferMechanism()));
	}
}
