package edu.virginia.vcgr.genii.client.byteio.transfer.simple;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.byteio.TransferInformationType;
import org.ggf.rbyteio.Append;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ggf.rbyteio.Read;
import org.ggf.rbyteio.ReadResponse;
import org.ggf.rbyteio.TruncAppend;
import org.ggf.rbyteio.Write;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.transfer.AbstractByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;

/**
 * This class implements the Simple transfer protocol for remote
 * random ByteIOs.
 * 
 * @author mmm2a
 */
public class SimpleRByteIOTransferer 
	extends	AbstractByteIOTransferer<RandomByteIOPortType> 
	implements RandomByteIOTransferer, SimpleByteIOTransferer
{
	/**
	 * Construct a new SimpleRByteIO transferer.
	 * 
	 * @param clientStub The client stub to use for outgoing calls.
	 */
	public SimpleRByteIOTransferer(RandomByteIOPortType clientStub)
	{
		super(clientStub, 
			TRANSFER_PROTOCOL, PREFERRED_READ_SIZE, MAXIMUM_READ_SIZE,
			PREFERRED_WRITE_SIZE, MAXIMUM_WRITE_SIZE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void append(byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[] { createByteBundle(data) }, 
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_clientStub.append(new Append(transType));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] read(long startOffset, int bytesPerBlock, int numBlocks,
			long stride) throws RemoteException
	{
		TransferInformationType holder =
			new TransferInformationType(null,
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		
		ReadResponse resp = _clientStub.read(
			new Read(startOffset, bytesPerBlock, numBlocks, stride, holder));
		
		if (resp.getTransferInformation() == null)
			throw new RemoteException("Invalid read response.");
		
		MessageElement []any = resp.getTransferInformation().get_any();
		if (any == null || any.length != 1)
			throw new RemoteException("Invalid read response.");
		
		try
		{
			return (byte[])any[0].getValueAsType(new QName(
				"http://www.w3.org/2001/XMLSchema", "base64Binary"));
		}
		catch (RemoteException re)
		{
			throw re;
		}
		catch (Exception e)
		{
			throw new RemoteException(e.toString(), e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void truncAppend(long offset, byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[] { createByteBundle(data) }, 
			ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_clientStub.truncAppend(new TruncAppend(offset, transType));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(long startOffset, int bytesPerBlock, long stride,
			byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[] { createByteBundle(data) }, 
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_clientStub.write(new Write(startOffset, bytesPerBlock, stride, transType));
	}

	@Override
	public void append(ByteBuffer source) throws RemoteException
	{
		byte []data = new byte[source.remaining()];
		source.get(data);
		append(data);
	}

	@Override
	public void read(long startOffset, ByteBuffer destination)
			throws RemoteException
	{
		byte []data = read(startOffset, destination.remaining(), 1, 0);
		if (data != null)
			destination.put(data);
	}

	@Override
	public void truncAppend(long offset, ByteBuffer source)
			throws RemoteException
	{
		byte []data = new byte[source.remaining()];
		source.get(data);
		truncAppend(offset, data);
	}

	@Override
	public void write(long startOffset, ByteBuffer source)
			throws RemoteException
	{
		byte []data = new byte[source.remaining()];
		source.get(data);
		write(startOffset, data.length, 0, data);
	}
}