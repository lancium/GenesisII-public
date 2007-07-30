package edu.virginia.vcgr.genii.client.byteio.xfer.mtom;

import java.rmi.RemoteException;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.apache.axis.types.UnsignedInt;
import org.ggf.byteio.TransferInformationType;
import org.ggf.sbyteio.SeekRead;
import org.ggf.sbyteio.SeekWrite;
import org.ggf.sbyteio.StreamableByteIOPortType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.SeekOrigin;
import edu.virginia.vcgr.genii.client.byteio.xfer.AbstractSByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.ISByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;

public class MTomSByteIOTransferer
	extends AbstractSByteIOTransferer implements ISByteIOTransferer
{
	public MTomSByteIOTransferer(StreamableByteIOPortType target)
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
			new TransferInformationType(
				null, ByteIOConstants.TRANSFER_TYPE_MTOM_URI));
		_target.seekRead(seekReadRequest);
		
		return receiveResponseAttachmentData(_target);
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
		
		sendRequestAttachmentData(_target, data, AttachmentType.MTOM);
		
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[0],
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI);
		SeekWrite seekWriteRequest = new SeekWrite(
			offset, seekOrigin, transType);
		
		_target.seekWrite(seekWriteRequest);
	}
}