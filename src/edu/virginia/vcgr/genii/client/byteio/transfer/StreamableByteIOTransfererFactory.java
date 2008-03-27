package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.sbyteio.StreamableByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.byteio.transfer.dime.DimeSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.mtom.MTOMSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.simple.SimpleSByteIOTransferer;

public class StreamableByteIOTransfererFactory extends TransfererFactory
{
	public StreamableByteIOTransfererFactory(
		StreamableByteIOPortType clientStub)
			throws ConfigurationException, RemoteException
	{
		super(clientStub);
	}

	@Override
	protected Object createDimeTransferer(Object clientStub)
	{
		return new DimeSByteIOTransferer((StreamableByteIOPortType)clientStub);
	}

	@Override
	protected Object createMTOMTransferer(Object clientStub)
	{
		return new MTOMSByteIOTransferer((StreamableByteIOPortType)clientStub);
	}

	@Override
	protected Object createSimpleTransferer(Object clientStub)
	{
		return new SimpleSByteIOTransferer(
			(StreamableByteIOPortType)clientStub);
	}
	
	public StreamableByteIOTransferer createStreamableByteIOTransferer(
		URI desiredTransferType)
			throws ConfigurationException
	{
		if (desiredTransferType == null)
			return createStreamableByteIOTransferer();
		
		return (StreamableByteIOTransferer)createTransferer(desiredTransferType.toString());
	}
	
	public StreamableByteIOTransferer createStreamableByteIOTransferer()
		throws ConfigurationException
	{
		return (StreamableByteIOTransferer)createTransferer();
	}
	
	static public StreamableByteIOTransferer createStreamableByteIOTransferer(
		StreamableByteIOPortType target)
			throws ConfigurationException, RemoteException
	{
		return (new StreamableByteIOTransfererFactory(
			target)).createStreamableByteIOTransferer();
	}
	
	static public StreamableByteIOTransferer createStreamableByteIOTransferer(
		StreamableByteIOPortType target, URI desiredTransferType)
			throws ConfigurationException, RemoteException
	{
		return (new StreamableByteIOTransfererFactory(
			target)).createStreamableByteIOTransferer(desiredTransferType);
	}
}