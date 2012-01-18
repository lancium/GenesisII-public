package edu.virginia.vcgr.genii.container.tty;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ggf.byteio.CustomFaultType;
import org.ggf.byteio.ReadNotPermittedFaultType;
import org.ggf.byteio.TransferInformationType;
import org.ggf.byteio.UnsupportedTransferFaultType;
import org.ggf.byteio.WriteNotPermittedFaultType;
import org.ggf.sbyteio.SeekNotPermittedFaultType;
import org.ggf.sbyteio.SeekRead;
import org.ggf.sbyteio.SeekReadResponse;
import org.ggf.sbyteio.SeekWrite;
import org.ggf.sbyteio.SeekWriteResponse;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.container.byteio.TransferAgent;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.tty.TTYPortType;

public class TTYServiceImpl extends GenesisIIBase implements TTYPortType
{
	static private HashMap<Object, TTYBuffer> _buffers =
		new HashMap<Object, TTYBuffer>();
	
	public TTYServiceImpl() throws RemoteException
	{
		super("TTYPortType");
		
		addImplementedPortType(TTYConstants.TTY_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE);
	}
	
	protected void setAttributeHandlers() 
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		new TTYAttributesHandlers(getAttributePackage());
	}
	
	@Override
	public PortType getFinalWSResourceInterface()
	{
		return TTYConstants.TTY_PORT_TYPE;
	}

	private void handleSeek(URI seekOrigin, long offset)
		throws SeekNotPermittedFaultType
	{
		if (seekOrigin == null || seekOrigin.equals(ByteIOConstants.SEEK_ORIGIN_CURRENT_URI))
			if (offset == 0)
				return;
		
		throw FaultManipulator.fillInFault(
			new SeekNotPermittedFaultType(null, null, null, null,
				new BaseFaultTypeDescription[] {
					new BaseFaultTypeDescription("Seek not permitted.")
			}, null));
	}
	
	@Override
	@RWXMapping(RWXCategory.READ)
	public SeekReadResponse seekRead(SeekRead seekReadRequest)
			throws RemoteException, ReadNotPermittedFaultType,
			SeekNotPermittedFaultType, ResourceUnknownFaultType,
			UnsupportedTransferFaultType, CustomFaultType
	{
		int numBytes = seekReadRequest.getNumBytes().intValue();
		TransferInformationType xType = 
			seekReadRequest.getTransferInformation();
		TTYBuffer buffer = null;
		byte []data = null;
		
		handleSeek(seekReadRequest.getSeekOrigin(), 
			seekReadRequest.getOffset());
		
		synchronized(_buffers)
		{
			buffer = _buffers.get(
				ResourceManager.getCurrentResource().getResourceKey());
		}
		
		if (buffer == null)
			data = new byte[0];
		else
		{
			data = buffer.read(numBytes);
		}
		
		TransferAgent.sendData(data, xType);
		
		return new SeekReadResponse(xType);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public SeekWriteResponse seekWrite(SeekWrite seekWriteRequest)
			throws RemoteException, SeekNotPermittedFaultType,
			ResourceUnknownFaultType, WriteNotPermittedFaultType,
			UnsupportedTransferFaultType, CustomFaultType
	{
		TTYBuffer buffer;
		TransferInformationType xType = 
			seekWriteRequest.getTransferInformation();
		
		handleSeek(seekWriteRequest.getSeekOrigin(),
			seekWriteRequest.getOffset());
		byte []data = TransferAgent.receiveData(xType);
		
		synchronized(_buffers)
		{
			String key =
				ResourceManager.getCurrentResource().getResourceKey();
			
			buffer = _buffers.get(key);
			if (buffer == null)
				_buffers.put(key, buffer = new TTYBuffer(1024 * 1024));
		}
		
		buffer.write(data, 0, data.length);
		
		return new SeekWriteResponse(xType);
	}
	
	@Override
	protected void postCreate(ResourceKey key, EndpointReferenceType newEPR,
		ConstructionParameters cParams, HashMap<QName, Object> constructionParameters,
		Collection<MessageElement> resolverCreationParams)
			throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(key, newEPR, cParams, constructionParameters, resolverCreationParams);
	}

	@Override
	protected void preDestroy() throws RemoteException, ResourceException
	{
		super.preDestroy();
		
		synchronized(_buffers)
		{
			_buffers.remove(
				ResourceManager.getCurrentResource().getResourceKey());
		}
	}
}