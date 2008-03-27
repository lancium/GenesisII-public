package edu.virginia.vcgr.genii.client.byteio.transfer.simple;

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

public class SimpleRByteIOTransferer 
	extends	AbstractByteIOTransferer<RandomByteIOPortType> 
	implements RandomByteIOTransferer, SimpleByteIOTransferer
{
	public SimpleRByteIOTransferer(RandomByteIOPortType clientStub)
	{
		super(clientStub, 
			TRANSFER_PROTOCOL, PREFERRED_READ_SIZE, PREFERRED_WRITE_SIZE);
	}
	
	@Override
	public void append(byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[] { createByteBundle(data) }, 
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_clientStub.append(new Append(transType));
	}

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

	@Override
	public void truncAppend(long offset, byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[] { createByteBundle(data) }, 
			ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_clientStub.truncAppend(new TruncAppend(offset, transType));
	}

	@Override
	public void write(long startOffset, int bytesPerBlock, long stride,
			byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[] { createByteBundle(data) }, 
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_clientStub.write(new Write(startOffset, bytesPerBlock, stride, transType));
	}
}