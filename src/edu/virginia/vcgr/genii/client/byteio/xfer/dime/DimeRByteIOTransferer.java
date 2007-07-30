package edu.virginia.vcgr.genii.client.byteio.xfer.dime;

import java.rmi.RemoteException;

import org.apache.axis.message.MessageElement;
import org.ggf.byteio.TransferInformationType;
import org.ggf.rbyteio.Append;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ggf.rbyteio.Read;
import org.ggf.rbyteio.TruncAppend;
import org.ggf.rbyteio.Write;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.xfer.AbstractRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.IRByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;

public class DimeRByteIOTransferer 
	extends AbstractRByteIOTransferer implements IRByteIOTransferer
{
	public DimeRByteIOTransferer(RandomByteIOPortType target)
	{
		super(target);
	}
	
	public void append(byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_target, data,
				AttachmentType.DIME);
			
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[0], 
			ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		_target.append(new Append(transType));
	}

	public byte[] read(
		long startOffset, int bytesPerBlock, 
		int numBlocks, long stride) 
		throws RemoteException
	{
		TransferInformationType holder =
			new TransferInformationType(null,
				ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		
		_target.read(
			new Read(startOffset, bytesPerBlock, numBlocks, stride, holder));
		
		return receiveResponseAttachmentData(_target);
	}

	public void truncAppend(long offset, byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_target, data,
			AttachmentType.DIME);
		
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[0], 
			ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		_target.truncAppend(new TruncAppend(offset, transType));
	}

	public void write(long startOffset, int bytesPerBlock, long stride, byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_target, data,
			AttachmentType.DIME);
		
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[0], 
				ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		_target.write(new Write(startOffset, bytesPerBlock, stride, transType));
	}
}