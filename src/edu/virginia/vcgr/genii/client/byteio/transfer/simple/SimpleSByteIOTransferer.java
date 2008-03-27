package edu.virginia.vcgr.genii.client.byteio.transfer.simple;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedInt;
import org.ggf.byteio.TransferInformationType;
import org.ggf.sbyteio.SeekRead;
import org.ggf.sbyteio.SeekReadResponse;
import org.ggf.sbyteio.SeekWrite;
import org.ggf.sbyteio.StreamableByteIOPortType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.byteio.transfer.AbstractByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransferer;

public class SimpleSByteIOTransferer 
	extends AbstractByteIOTransferer<StreamableByteIOPortType> 
	implements StreamableByteIOTransferer, SimpleByteIOTransferer
{
	public SimpleSByteIOTransferer(StreamableByteIOPortType clientStub)
	{
		super(clientStub,
			TRANSFER_PROTOCOL, PREFERRED_READ_SIZE, PREFERRED_WRITE_SIZE);
	}
	
	@Override
	public byte[] seekRead(SeekOrigin origin, long offset, long numBytes)
			throws RemoteException
	{
		URI seekOrigin;
		
		if (origin.equals(SeekOrigin.SEEK_BEGINNING))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI;
		else if (origin.equals(SeekOrigin.SEEK_CURRENT))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_CURRENT_URI;
		else
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_END_URI;
		
		SeekRead seekReadRequest = new SeekRead(
			offset, seekOrigin, new UnsignedInt(numBytes), 
			new TransferInformationType(null, ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI));
		SeekReadResponse resp = _clientStub.seekRead(seekReadRequest);
		
		if (resp.getTransferInformation() == null)
			throw new RemoteException("Invalid read response.");
		
		MessageElement []any  = resp.getTransferInformation().get_any();
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
			throw new RemoteException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void seekWrite(SeekOrigin origin, long offset, byte[] data)
			throws RemoteException
	{
		URI seekOrigin;
		
		if (origin.equals(SeekOrigin.SEEK_BEGINNING))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI;
		else if (origin.equals(SeekOrigin.SEEK_CURRENT))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_CURRENT_URI;
		else
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_END_URI;
		
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[] { createByteBundle(data) },
			ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		SeekWrite seekWriteRequest = new SeekWrite(
			offset, seekOrigin, transType);
		_clientStub.seekWrite(seekWriteRequest);
	}
}