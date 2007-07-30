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
package edu.virginia.vcgr.genii.client.byteio.xfer.simple;

import java.rmi.RemoteException;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.ggf.byteio.TransferInformationType;
import org.ggf.rbyteio.Append;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.ggf.rbyteio.Read;
import org.ggf.rbyteio.ReadResponse;
import org.ggf.rbyteio.TruncAppend;
import org.ggf.rbyteio.Write;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.xfer.AbstractRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.IRByteIOTransferer;

public class SimpleRByteIOTransferer 
	extends AbstractRByteIOTransferer implements IRByteIOTransferer
{
	public SimpleRByteIOTransferer(RandomByteIOPortType target)
	{
		super(target);
	}
	
	public byte[] read(long startOffset, int bytesPerBlock, int numBlocks,
			long stride) throws RemoteException
	{
		TransferInformationType holder =
			new TransferInformationType(null,
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		
		ReadResponse resp = _target.read(
			new Read(startOffset, bytesPerBlock, numBlocks, stride, holder));
		
		if (resp.getTransferInformation() == null)
			throw new RemoteException("Invalid read response.");
		
		MessageElement []any = resp.getTransferInformation().get_any();
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
			throw new RemoteException(e.toString(), e);
		}
	}

	public void write(long startOffset, int bytesPerBlock, long stride,
			byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[] { createByteBundle(data) }, 
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_target.write(new Write(startOffset, bytesPerBlock, stride, transType));
	}

	public void append(byte[] data) throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
				new MessageElement[] { createByteBundle(data) }, 
				ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_target.append(new Append(transType));
	}

	public void truncAppend(long offset, byte[] data) 
		throws RemoteException
	{
		TransferInformationType transType = new TransferInformationType(
			new MessageElement[] { createByteBundle(data) }, 
			ByteIOConstants.TRANSFER_TYPE_SIMPLE_URI);
		_target.truncAppend(new TruncAppend(offset, transType));
	}
}