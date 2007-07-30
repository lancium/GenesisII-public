package edu.virginia.vcgr.genii.client.byteio.xfer.simple;

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
import edu.virginia.vcgr.genii.client.byteio.xfer.AbstractSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.ISByteIOTransferer;

public class SimpleSByteIOTransferer 
	extends AbstractSByteIOTransferer implements ISByteIOTransferer
{
	private StreamableByteIOPortType _target;
	
	public SimpleSByteIOTransferer(StreamableByteIOPortType target)
	{
		super(target);
	}
	
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
		SeekReadResponse resp = _target.seekRead(seekReadRequest);
		
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
		_target.seekWrite(seekWriteRequest);
	}
}