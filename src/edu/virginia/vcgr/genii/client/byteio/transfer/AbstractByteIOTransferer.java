package edu.virginia.vcgr.genii.client.byteio.transfer;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;

/**
 * An abstract base class that implements much of the common functionallity
 * of a ByteIO transferer.
 * 
 * @author mmm2a
 *
 * @param <Type> The client stub type to use when doing remote communication.
 */
public abstract class AbstractByteIOTransferer<Type> implements ByteIOTransferer
{
	private URI _transferProtocol;
	private int _preferredReadSize;
	private int _preferredWriteSize;

	/**
	 * {@inheritDoc}
	 */
	public URI getTransferProtocol()
	{
		return _transferProtocol;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getPreferredReadSize()
	{
		return _preferredReadSize;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getPreferredWriteSize()
	{
		return _preferredWriteSize;
	}
	
	/**
	 * A base class method which takes a client proxy object and retrieves
	 * the attachment data that came back in with the call (obviously, this
	 * method is only useful for transfer mechanisms which use attachments,
	 * but that is actually the common case for known ByteIO transfer
	 * mechanisms).
	 * 
	 * @param clientProxy The remote proxy stub used to call and retrieve
	 * the data.
	 * 
	 * @return The block of data (if any) contained in the attachment.
	 * 
	 * @throws RemoteException
	 */
	static protected byte[] receiveResponseAttachmentData(Object clientProxy) 
		throws RemoteException
	{
		try
		{
			Collection<GeniiAttachment> attachments =
				ClientUtils.getAttachments(clientProxy);
			if (attachments == null || attachments.size() != 1)
				throw new RemoteException("Invalid attachment data received.");
			
			return attachments.iterator().next().getData();
		}
		catch (ConfigurationException ce)
		{
			throw new RemoteException(
				"Unable to get attachments from client proxy.", ce);
		}
	}
	
	/**
	 * Similar to the retrieveResponseAttachmentData method above, this
	 * method is used for the rather common use case of attaching data
	 * to a SOAP message as an outgoing SOAP attachment.
	 * 
	 * @param clientProxy clientProxy The remote proxy stub used to call
	 * and send the data.
	 * @param data The data to attach to the outcoing call.
	 * @param attachmentType The type of attachment to use (DIME and 
	 * MTOM right now).
	 * 
	 * @throws RemoteException
	 */
	static protected void sendRequestAttachmentData(Object clientProxy,
		byte []data, AttachmentType attachmentType) throws RemoteException
	{
		try
		{
			LinkedList<GeniiAttachment> attachments =
				new LinkedList<GeniiAttachment>();
			attachments.add(new GeniiAttachment(data));
			ClientUtils.setAttachments(
				clientProxy, attachments, attachmentType);
		}
		catch (ConfigurationException ce)
		{
			throw new RemoteException(
				"Unable to set attachments for client proxy.", ce);
		}
	}
	
	protected Type _clientStub;
	
	/**
	 * Protected constructor that derived classes will use to initialize
	 * the common transfer information.
	 * 
	 * @param clientStub THe remote proxy stub used to call the ByteIO
	 * resource.
	 * @param transferProcotol The transfer protocol that this instance
	 * (it's derived version) will support.
	 * @param preferredReadSize The preferred read size to use for
	 * this transferer.
	 * @param preferredWriteSize The preferred write size to use for
	 * this transferer.
	 */
	protected AbstractByteIOTransferer(Type clientStub,
		URI transferProcotol, int preferredReadSize, int preferredWriteSize)
	{
		_clientStub = clientStub;
		
		_transferProtocol = transferProcotol;
		_preferredReadSize = preferredReadSize;
		_preferredWriteSize = preferredWriteSize;
	}
	
	/**
	 * While not common, a type of byte io transfer we support is simple
	 * which effectively means that the bytes are Base64 encoded into the
	 * SOAP message itself.  This method wraps those bytes with the
	 * correct XML.
	 * 
	 * @param data The data block to wrap.
	 * 
	 * @return A new XML message element to insert into the SOAP message.
	 */
	static protected MessageElement createByteBundle(byte []data)
	{
		return new MessageElement(
			ByteIOConstants.SIMPLE_XFER_DATA_QNAME, data);
	}
}