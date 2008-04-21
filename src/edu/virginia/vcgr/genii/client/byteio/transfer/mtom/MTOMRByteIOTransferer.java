package edu.virginia.vcgr.genii.client.byteio.transfer.mtom;

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

/**
 * This class implements the MTOM transfer protocol for remote
 * random ByteIOs.
 * 
 * @author mmm2a
 */
public class MTOMRByteIOTransferer 
	extends	AbstractByteIOTransferer<RandomByteIOPortType>
	implements RandomByteIOTransferer, MTOMByteIOTransferer
{
	/**
	 * Construct a new MTOMRByteIO transferer.
	 * 
	 * @param clientStub The client stub to use for outgoing calls.
	 */
	public MTOMRByteIOTransferer(RandomByteIOPortType clientStub)
	{
		super(clientStub,
			TRANSFER_PROTOCOL, PREFERRED_READ_SIZE, PREFERRED_WRITE_SIZE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void append(byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_clientStub, data,
				AttachmentType.MTOM);
			
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[0], 
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI);
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
				ByteIOConstants.TRANSFER_TYPE_MTOM_URI);
		
		_clientStub.read(
			new Read(startOffset, bytesPerBlock, numBlocks, stride, holder));
		
		return receiveResponseAttachmentData(_clientStub);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void truncAppend(long offset, byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_clientStub, data,
			AttachmentType.MTOM);
		
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[0], 
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI);
		_clientStub.truncAppend(new TruncAppend(offset, transType));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(long startOffset, int bytesPerBlock, long stride,
			byte[] data) throws RemoteException
	{
		sendRequestAttachmentData(_clientStub, data,
			AttachmentType.MTOM);
		
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[0], 
				ByteIOConstants.TRANSFER_TYPE_MTOM_URI);
		_clientStub.write(new Write(startOffset, bytesPerBlock, stride, transType));
	}
}