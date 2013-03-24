/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.container.byteio;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collection;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.attachments.Attachments;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.ggf.byteio.TransferInformationType;
import org.ggf.byteio.UnsupportedTransferFaultType;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachmentException;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

public class TransferAgent
{
	/**
	 * Return an array of the transfer mechanisms supported by receiveData() and sendData().
	 */
	static public URI[] getTransferMechs()
	{
		return new URI[] { ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI, ByteIOConstants.TRANSFER_TYPE_DIME_URI,
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI };
	}

	static public byte[] receiveData(TransferInformationType transType) throws RemoteException
	{
		URI transMech = transType.getTransferMechanism();
		MessageElement[] any = transType.get_any();

		if (transMech.equals(ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI)) {
			if (any == null || any.length != 1)
				throw new RemoteException("Invalid transfer data.");
			try {
				return (byte[]) (any[0].getValueAsType(new QName("http://www.w3.org/2001/XMLSchema", "base64Binary")));
			} catch (Exception e) {
				throw new RemoteException(e.toString(), e);
			}
		} else if (transMech.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI)
			|| transMech.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI)) {
			return receiveIncomingAttachmentData();
		} else
			throw FaultManipulator.fillInFault(new UnsupportedTransferFaultType());
	}

	static public void sendData(byte[] data, TransferInformationType transType) throws RemoteException
	{
		URI transMech = transType.getTransferMechanism();

		if (transMech.equals(ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI)) {
			transType.set_any(new MessageElement[] { new MessageElement(ByteIOConstants.SIMPLE_XFER_DATA_QNAME, data) });
		} else if (transMech.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI)
			|| transMech.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI)) {
			int sendType;
			if (transMech.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
				sendType = Attachments.SEND_TYPE_DIME;
			else
				sendType = Attachments.SEND_TYPE_MTOM;

			sendOutgoingAttachmentData(data, sendType);
		} else
			throw FaultManipulator.fillInFault(new UnsupportedTransferFaultType());
	}

	static private byte[] receiveIncomingAttachmentData() throws RemoteException
	{
		InputStream in = null;

		try {
			Message msg = MessageContext.getCurrentContext().getRequestMessage();
			Attachments attachments = msg.getAttachmentsImpl();
			if (attachments == null)
				throw new RemoteException("Error in contained attachment.");
			Collection<?> coll = attachments.getAttachments();
			if (coll == null || coll.size() != 1)
				throw new RemoteException("Error in contained attachment.");
			AttachmentPart part = (AttachmentPart) coll.iterator().next();
			return GeniiAttachment.extractData(part);
		} catch (SOAPException se) {
			throw new RemoteException(se.getLocalizedMessage(), se);
		} catch (IOException ioe) {
			throw new RemoteException(ioe.getLocalizedMessage(), ioe);
		} finally {
			StreamUtils.close(in);
		}
	}

	static private void sendOutgoingAttachmentData(byte[] data, int sendType) throws RemoteException
	{
		Attachments axisAttachments = MessageContext.getCurrentContext().getResponseMessage().getAttachmentsImpl();
		axisAttachments.setSendType(sendType);

		ByteArrayDataSource bads = new ByteArrayDataSource(data, "application/octet-stream");
		try {
			axisAttachments.addAttachmentPart(new AttachmentPart(new DataHandler(bads)));
		} catch (AxisFault af) {
			throw new GeniiAttachmentException(af);
		}
	}

	/**
	 * If a block of data was attached to the message in a self-identifying format such as MTOM or
	 * DIME, then extract the data from the message.
	 */
	@SuppressWarnings("unchecked")
	static public byte[] extractAttachmentData() throws IOException, SOAPException
	{
		Message msg = MessageContext.getCurrentContext().getRequestMessage();
		Attachments attachments = msg.getAttachmentsImpl();
		if (attachments == null)
			return null;
		Collection<AttachmentPart> coll = (Collection<AttachmentPart>) attachments.getAttachments();
		if (coll == null || coll.size() == 0)
			return null;
		AttachmentPart part = coll.iterator().next();
		return GeniiAttachment.extractData(part);
	}
}
