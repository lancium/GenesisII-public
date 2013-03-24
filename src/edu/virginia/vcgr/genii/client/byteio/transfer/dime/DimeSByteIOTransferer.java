package edu.virginia.vcgr.genii.client.byteio.transfer.dime;

import java.nio.ByteBuffer;
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
import edu.virginia.vcgr.genii.client.byteio.transfer.AbstractByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.StreamableByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;

/**
 * This class implements the DIME transfer protocol for the Streamable ByteIO case.
 * 
 * @author mmm2a
 */
public class DimeSByteIOTransferer extends AbstractByteIOTransferer<StreamableByteIOPortType> implements
	StreamableByteIOTransferer, DimeByteIOTransferer
{
	/**
	 * Create a new DIMESByteIO transferer.
	 * 
	 * @param clientStub
	 *            The client stub to use for all out calls.
	 */
	public DimeSByteIOTransferer(StreamableByteIOPortType clientStub)
	{
		super(clientStub, TRANSFER_PROTOCOL, PREFERRED_READ_SIZE, MAXIMUM_READ_SIZE, PREFERRED_WRITE_SIZE, MAXIMUM_WRITE_SIZE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] seekRead(SeekOrigin origin, long offset, long numBytes) throws RemoteException
	{
		URI seekOrigin;

		if (origin.equals(SeekOrigin.SEEK_BEGINNING))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI;
		else if (origin.equals(SeekOrigin.SEEK_CURRENT))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_CURRENT_URI;
		else
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_END_URI;

		SeekRead seekReadRequest = new SeekRead(offset, seekOrigin, new UnsignedInt(numBytes), new TransferInformationType(
			null, ByteIOConstants.TRANSFER_TYPE_DIME_URI));
		_clientStub.seekRead(seekReadRequest);

		return receiveResponseAttachmentData(_clientStub);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void seekWrite(SeekOrigin origin, long offset, byte[] data) throws RemoteException
	{
		URI seekOrigin;

		if (origin.equals(SeekOrigin.SEEK_BEGINNING))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_BEGINNING_URI;
		else if (origin.equals(SeekOrigin.SEEK_CURRENT))
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_CURRENT_URI;
		else
			seekOrigin = ByteIOConstants.SEEK_ORIGIN_END_URI;

		sendRequestAttachmentData(_clientStub, data, AttachmentType.DIME);

		TransferInformationType transType = new TransferInformationType(new MessageElement[0],
			ByteIOConstants.TRANSFER_TYPE_DIME_URI);
		SeekWrite seekWriteRequest = new SeekWrite(offset, seekOrigin, transType);

		_clientStub.seekWrite(seekWriteRequest);
	}

	@Override
	public void seekRead(SeekOrigin origin, long offset, ByteBuffer destination) throws RemoteException
	{
		byte[] data = seekRead(origin, offset, destination.remaining());
		if (data != null)
			destination.put(data);
	}

	@Override
	public void seekWrite(SeekOrigin origin, long offset, ByteBuffer source) throws RemoteException
	{
		byte[] data = new byte[source.remaining()];
		source.get(data);
		seekWrite(origin, offset, data);
	}
}