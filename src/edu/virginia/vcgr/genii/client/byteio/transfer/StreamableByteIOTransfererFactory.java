package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.sbyteio.StreamableByteIOPortType;

import edu.virginia.vcgr.genii.client.byteio.transfer.dime.DimeSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.mtom.MTOMSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.simple.SimpleSByteIOTransferer;

/**
 * Similar to the RandomByteIO transferer factory, this class is a convenient
 * single point from which streamable byteio transferers can be created.
 * 
 * @author mmm2a
 */
public class StreamableByteIOTransfererFactory extends TransfererFactory
{
	/**
	 * Construct a new streamable byteio transferer factory for a given
	 * remote stub.
	 * 
	 * @param clientStub The remote client stub to use for outcoing calls.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public StreamableByteIOTransfererFactory(
		StreamableByteIOPortType clientStub)
			throws RemoteException, IOException
	{
		super(clientStub);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object createDimeTransferer(Object clientStub)
	{
		return new DimeSByteIOTransferer((StreamableByteIOPortType)clientStub);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object createMTOMTransferer(Object clientStub)
	{
		return new MTOMSByteIOTransferer((StreamableByteIOPortType)clientStub);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object createSimpleTransferer(Object clientStub)
	{
		return new SimpleSByteIOTransferer(
			(StreamableByteIOPortType)clientStub);
	}
	
	/**
	 * A convenience method to create new StreamableByteIO transferers given
	 * the URI description of the transfer protocol to use.
	 * 
	 * @param desiredTransferType The desired transfer protocol to use (if
	 * available).  By default, this factory will create an instance of a
	 * "preferred" transfer type (one that is deemed to be the most 
	 * efficient). 
	 * 
	 * @return A newly created streamable byteio transferer.
	 * 
	 * @throws ConfigurationException
	 */
	
	public StreamableByteIOTransferer createStreamableByteIOTransferer(
		URI desiredTransferType) throws IOException
	{
		if (desiredTransferType == null)
			return createStreamableByteIOTransferer();
		
		return (StreamableByteIOTransferer)createTransferer(desiredTransferType.toString());
	}
	
	/**
	 * A convenience method to create a new Streamable transferer which
	 * implements a "preferred" transfer type.
	 * 
	 * @return A newly created streamable byteio transferer.
	 * 
	 * @throws ConfigurationException
	 */
	public StreamableByteIOTransferer createStreamableByteIOTransferer()
		throws IOException
	{
		return (StreamableByteIOTransferer)createTransferer();
	}
	
	/**
	 * A convenience method to create a new streamable byteio transferer 
	 * based off of a given target streamablebyteio.
	 * 
	 * @param target The target for which to create a new StreamableByteIO
	 * transferer agent.
	 * 
	 * @return A newly create streamable byteio transferer.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	
	static public StreamableByteIOTransferer createStreamableByteIOTransferer(
		StreamableByteIOPortType target)
			throws RemoteException, IOException
	{
		return (new StreamableByteIOTransfererFactory(
			target)).createStreamableByteIOTransferer();
	}
	
	/**
	 * A convenience method to create a new streamable byteio transferer 
	 * based off of a given target streamablebyteio.
	 * 
	 * @param target target The target for which to create a new 
	 * StreamableByteIO transferer agent.
	 * @param desiredTransferType The transfer type (if any) that the caller
	 * wants to use.  If null, a "preferred" transfer type will be created.
	 * 
	 * @return A newly create transferer.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	static public StreamableByteIOTransferer createStreamableByteIOTransferer(
		StreamableByteIOPortType target, URI desiredTransferType)
			throws RemoteException, IOException
	{
		return (new StreamableByteIOTransfererFactory(
			target)).createStreamableByteIOTransferer(desiredTransferType);
	}
}