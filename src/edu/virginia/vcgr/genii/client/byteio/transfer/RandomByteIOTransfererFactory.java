package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.byteio.transfer.dime.DimeRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.mtom.MTOMRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.simple.SimpleRByteIOTransferer;

public class RandomByteIOTransfererFactory extends TransfererFactory
{
	public RandomByteIOTransfererFactory(RandomByteIOPortType clientStub)
		throws ConfigurationException, RemoteException
	{
		super(clientStub);
	}

	@Override
	protected Object createDimeTransferer(Object clientStub)
	{
		return new DimeRByteIOTransferer((RandomByteIOPortType)clientStub);
	}

	@Override
	protected Object createMTOMTransferer(Object clientStub)
	{
		return new MTOMRByteIOTransferer((RandomByteIOPortType)clientStub);
	}

	@Override
	protected Object createSimpleTransferer(Object clientStub)
	{
		return new SimpleRByteIOTransferer((RandomByteIOPortType)clientStub);
	}
	
	public RandomByteIOTransferer createRandomByteIOTransferer(
		URI desiredTransferType)
			throws ConfigurationException
	{
		if (desiredTransferType == null)
			return createRandomByteIOTransferer();
		
		return (RandomByteIOTransferer)createTransferer(desiredTransferType.toString());
	}
	
	public RandomByteIOTransferer createRandomByteIOTransferer()
		throws ConfigurationException
	{
		return (RandomByteIOTransferer)createTransferer();
	}
	
	static public RandomByteIOTransferer createRandomByteIOTransferer(
		RandomByteIOPortType target)
			throws ConfigurationException, RemoteException
	{
		return (new RandomByteIOTransfererFactory(
			target)).createRandomByteIOTransferer();
	}
	
	static public RandomByteIOTransferer createRandomByteIOTransferer(
		RandomByteIOPortType target, URI desiredTransferType)
			throws ConfigurationException, RemoteException
	{
		return (new RandomByteIOTransfererFactory(
			target)).createRandomByteIOTransferer(desiredTransferType);
	}
}