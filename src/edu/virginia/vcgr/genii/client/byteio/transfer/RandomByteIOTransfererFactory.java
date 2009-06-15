package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.io.IOException;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;

import edu.virginia.vcgr.genii.client.byteio.transfer.dime.DimeRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.mtom.MTOMRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.simple.SimpleRByteIOTransferer;

/**
 * A simple factory class for creating new RandomByteIO transferers.
 * This class merely serves as a convenient single point from which
 * transferers can be created.
 * 
 * @author mmm2a
 */
public class RandomByteIOTransfererFactory extends TransfererFactory
{
	/**
	 * Create a new random byteio transferer factory.
	 * 
	 * @param clientStub The RandomByteIO client stub to use for outcalls
	 * from created transferers.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public RandomByteIOTransfererFactory(RandomByteIOPortType clientStub)
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
		return new DimeRByteIOTransferer((RandomByteIOPortType)clientStub);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object createMTOMTransferer(Object clientStub)
	{
		return new MTOMRByteIOTransferer((RandomByteIOPortType)clientStub);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Object createSimpleTransferer(Object clientStub)
	{
		return new SimpleRByteIOTransferer((RandomByteIOPortType)clientStub);
	}
	
	/**
	 * A convenience method to create new RandomByteIO transferers given
	 * the URI description of the transfer protocol to use.
	 * 
	 * @param desiredTransferType The desired transfer protocol to use (if
	 * available).  By default, this factory will create an instance of a
	 * "preferred" transfer type (one that is deemed to be the most 
	 * efficient). 
	 * 
	 * @return A newly created random byteio transferer.
	 * 
	 * @throws ConfigurationException
	 */
	public RandomByteIOTransferer createRandomByteIOTransferer(
		URI desiredTransferType) throws IOException
	{
		if (desiredTransferType == null)
			return createRandomByteIOTransferer();
		
		return (RandomByteIOTransferer)createTransferer(desiredTransferType.toString());
	}
	
	/**
	 * A convenience method to create a new RandomByteIO transferer which
	 * implements a "preferred" transfer type.
	 * 
	 * @return A newly created random byteio transferer.
	 * 
	 * @throws ConfigurationException
	 */
	public RandomByteIOTransferer createRandomByteIOTransferer()
		throws IOException
	{
		return (RandomByteIOTransferer)createTransferer();
	}
	
	/**
	 * A convenience method to create a new random byteio transferer based off
	 * of a given target randombyteio.
	 * 
	 * @param target The target for which to create a new RandomByteIO
	 * transferer agent.
	 * 
	 * @return A newly create random byteio transferer.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	static public RandomByteIOTransferer createRandomByteIOTransferer(
		RandomByteIOPortType target)
			throws RemoteException, IOException
	{
		return (new RandomByteIOTransfererFactory(
			target)).createRandomByteIOTransferer();
	}
	
	/**
	 * A convenience method to create a new random byteio transferer based off
	 * of a given target randombyteio.
	 * 
	 * @param target target The target for which to create a new RandomByteIO
	 * transferer agent.
	 * @param desiredTransferType The transfer type (if any) that the caller
	 * wants to use.  If null, a "preferred" transfer type will be created.
	 * 
	 * @return A newly create transferer.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	static public RandomByteIOTransferer createRandomByteIOTransferer(
		RandomByteIOPortType target, URI desiredTransferType)
			throws RemoteException, IOException
	{
		return (new RandomByteIOTransfererFactory(
			target)).createRandomByteIOTransferer(desiredTransferType);
	}
}