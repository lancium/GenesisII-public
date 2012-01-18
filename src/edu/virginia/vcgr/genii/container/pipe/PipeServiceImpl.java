package edu.virginia.vcgr.genii.container.pipe;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

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
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;

import edu.virginia.cs.vcgr.genii._2006._12.resource_simple.TryAgainFaultType;
import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.pipe.PipeConstants;
import edu.virginia.vcgr.genii.client.pipe.PipeConstructionParameters;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.byteio.TransferAgent;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.pipe.PipePortType;
import edu.virginia.vcgr.genii.security.RWXCategory;

@ConstructionParametersType(PipeConstructionParameters.class)
public class PipeServiceImpl extends GenesisIIBase implements PipePortType
{
	static final private long DEFAULT_TIMEOUT_MS = 1000 * 30;
	
	static private Map<String, PipeBuffer> _resource2BufferMap =
		new HashMap<String, PipeBuffer>();
	
	private PipeBuffer createPipeBuffer(ResourceKey rKey) throws ResourceException
	{
		IResource resource = rKey.dereference();
		PipeConstructionParameters consParms =
			(PipeConstructionParameters)resource.constructionParameters(
				PipeServiceImpl.class);
		return new PipeBuffer(consParms.pipeSize());
	}
	
	private PipeBuffer pipeBuffer() throws ResourceUnknownFaultType, ResourceException
	{
		PipeBuffer buffer;
		ResourceKey rKey = ResourceManager.getCurrentResource();
		
		synchronized(_resource2BufferMap)
		{
			buffer = _resource2BufferMap.get(rKey.getResourceKey());
			if (buffer == null)
				_resource2BufferMap.put(rKey.getResourceKey(),
					buffer = createPipeBuffer(rKey));
		}
		
		return buffer;
	}
	
	private long getTimeoutMS()
	{
		return DEFAULT_TIMEOUT_MS;
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
	protected void preDestroy() throws RemoteException, ResourceException
	{
		synchronized(_resource2BufferMap)
		{
			_resource2BufferMap.remove(ResourceManager.getCurrentResource().getResourceKey());
		}
		
		super.preDestroy();
	}

	protected void setAttributeHandlers() 
		throws NoSuchMethodException, ResourceException, 
			ResourceUnknownFaultType
	{
		super.setAttributeHandlers();
		
		new PipeAttributesHandler(getAttributePackage());
	}

	public PipeServiceImpl() throws RemoteException
	{
		super("PipePortType");
		
		addImplementedPortType(PipeConstants.PIPE_PORT_TYPE);
		addImplementedPortType(WellKnownPortTypes.SBYTEIO_SERVICE_PORT_TYPE);
	}
	
	@Override
	public PortType getFinalWSResourceInterface()
	{
		return PipeConstants.PIPE_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public SeekReadResponse seekRead(SeekRead seekReadRequest) throws RemoteException,
		ReadNotPermittedFaultType, SeekNotPermittedFaultType,
		ResourceUnknownFaultType, UnsupportedTransferFaultType,
		CustomFaultType
	{
		int numBytes = seekReadRequest.getNumBytes().intValue();
		TransferInformationType xType = 
			seekReadRequest.getTransferInformation();
		PipeBuffer buffer = null;
		ByteBuffer sink = ByteBuffer.allocate(numBytes);
		
		handleSeek(seekReadRequest.getSeekOrigin(), 
			seekReadRequest.getOffset());
	
		try
		{
			buffer = pipeBuffer();
			buffer.read(sink, getTimeoutMS());
			sink.flip();
			byte []data = new byte[sink.remaining()];
			sink.get(data);
			TransferAgent.sendData(data, xType);
			
			return new SeekReadResponse(xType);
		}
		catch (InterruptedException ie)
		{
			throw FaultManipulator.fillInFault(new TryAgainFaultType());
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public SeekWriteResponse seekWrite(SeekWrite seekWriteRequest) throws RemoteException,
		SeekNotPermittedFaultType, ResourceUnknownFaultType,
		WriteNotPermittedFaultType, UnsupportedTransferFaultType,
		CustomFaultType
	{
		PipeBuffer buffer;
		TransferInformationType xType = 
			seekWriteRequest.getTransferInformation();
		
		handleSeek(seekWriteRequest.getSeekOrigin(),
			seekWriteRequest.getOffset());
		byte []data = TransferAgent.receiveData(xType);
		ByteBuffer source = ByteBuffer.wrap(data);
		
		try
		{
			buffer = pipeBuffer();
			buffer.write(source, getTimeoutMS());
			return new SeekWriteResponse(xType);
		}
		catch (InterruptedException ie)
		{
			throw FaultManipulator.fillInFault(new TryAgainFaultType());
		}
	}
}