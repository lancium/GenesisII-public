package edu.virginia.vcgr.genii.client.byteio.xfer;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.axis.message.MessageElement;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.attachments.AttachmentType;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;

public class AbstractByteIOTransferer
{
	protected MessageElement createByteBundle(byte []data)
	{
		return new MessageElement(
			ByteIOConstants.SIMPLE_XFER_DATA_QNAME, data);
	}
	
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
			throw new RemoteException(ce.getLocalizedMessage(), ce);
		}
	}
	
	static protected void sendRequestAttachmentData(Object clientProxy,
		byte []data, AttachmentType attachmentType)
		throws RemoteException
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
			throw new RemoteException(ce.getLocalizedMessage(), ce);
		}
	}
}