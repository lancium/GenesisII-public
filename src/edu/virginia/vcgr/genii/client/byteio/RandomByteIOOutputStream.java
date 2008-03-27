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
package edu.virginia.vcgr.genii.client.byteio;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;

public class RandomByteIOOutputStream extends OutputStream
{
	private RandomByteIOTransferer _transferer;
	private long _offset = 0L;
	
	public RandomByteIOOutputStream(EndpointReferenceType target, 
		URI desiredTransferType)
			throws ConfigurationException, RemoteException
	{
		RandomByteIOPortType clientStub = ClientUtils.createProxy(
			RandomByteIOPortType.class, target);
		RandomByteIOTransfererFactory factory = 
			new RandomByteIOTransfererFactory(clientStub);
		_transferer = factory.createRandomByteIOTransferer(desiredTransferType);
	}
	
	public RandomByteIOOutputStream(EndpointReferenceType target)
		throws ConfigurationException, RemoteException
	{
		this(target, null);
	}
	
	@Override
	public void write(byte []data) throws IOException
	{
		_transferer.write(_offset, data.length, 0, data);
		_offset += data.length;
	}
	
	@Override
	public void write(byte []data, int offset, int length)
		throws IOException
	{
		byte []newData = new byte[length];
		System.arraycopy(data, offset, newData, 0, length);
		write(newData);
	}
	
	@Override
	public void write(int b) throws IOException
	{
		write(new byte[] { (byte)b });
	}

	public BufferedOutputStream createPreferredBufferedStream()
	{
		return new BufferedOutputStream(
			this, _transferer.getPreferredWriteSize());
	}
}