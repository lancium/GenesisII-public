package edu.virginia.vcgr.genii.client.byteio.transfer.dime;

import java.rmi.RemoteException;

import org.apache.axis.message.MessageElement;
import org.ggf.byteio.TransferInformationType;
import org.ggf.rbyteio.Append;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ggf.rbyteio.Read;
import org.ggf.rbyteio.TruncAppend;
import org.ggf.rbyteio.Write;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.transfer.AbstractByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;

public class DimeRByteIOTransferer 
	extends AbstractByteIOTransferer<RandomByteIOPortType>
	implements RandomByteIOTransferer, DimeByteIOTransferer
{
	public DimeRByteIOTransferer(RandomByteIOPortType clientStub)
	{
		super(clientStub,
			TRANSFER_PROTOCOL, PREFERRED_READ_SIZE, PREFERRED_WRITE_SIZE);
	}
	
	@Override
	public void append(byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_clientStub, data,
				AttachmentType.DIME);
			
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[0], 
			ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		_clientStub.append(new Append(transType));
	}

	@Override
	public byte[] read(long startOffset, int bytesPerBlock, int numBlocks,
			long stride) throws RemoteException
	{
		TransferInformationType holder =
			new TransferInformationType(null,
				ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		
		_clientStub.read(
			new Read(startOffset, bytesPerBlock, numBlocks, stride, holder));
		
		return receiveResponseAttachmentData(_clientStub);
	}

	@Override
	public void truncAppend(long offset, byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_clientStub, data,
			AttachmentType.DIME);
		
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[0], 
			ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		_clientStub.truncAppend(new TruncAppend(offset, transType));
	}

	@Override
	public void write(long startOffset, int bytesPerBlock, long stride,
			byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_clientStub, data,
			AttachmentType.DIME);
		
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[0], 
				ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		_clientStub.write(new Write(startOffset, bytesPerBlock, stride, transType));
	}
}