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
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.transfer.RandomByteIOTransfererFactory;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;

/**
 * An implementation of the standard Java Output stream that writes
 * to remote Random ByteIO resources.
 * 
 * @author mmm2a
 */
public class RandomByteIOOutputStream extends OutputStream
{
	/* The transferer being used by this stream. */
	private RandomByteIOTransferer _transferer;
	
	/* The current offset within the remote random byteio resource */
	private long _offset = 0L;
	
	/**
	 * Create a new RandomByteIO output stream for a given endpoint and
	 * transfer protocol.
	 * 
	 * @param target The target ByteIO to write bytes to.
	 * @param desiredTransferProtocol The desired transfer protocol to use when
	 * writing bytes.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public RandomByteIOOutputStream(EndpointReferenceType target, 
		URI desiredTransferType)
			throws IOException, RemoteException
	{
		RandomByteIOPortType clientStub = ClientUtils.createProxy(
			RandomByteIOPortType.class, target);
		RandomByteIOTransfererFactory factory = 
			new RandomByteIOTransfererFactory(clientStub);
		_transferer = factory.createRandomByteIOTransferer(desiredTransferType);
		_transferer.truncAppend(0, new byte[0]);
	}
	
	/**
	 * Create a new RandomByteIO output stream for a given endpoint
	 * 
	 * @param target The target ByteIO to write bytes to.
	 * 
	 * @throws ConfigurationException
	 * @throws RemoteException
	 */
	public RandomByteIOOutputStream(EndpointReferenceType target)
		throws IOException, RemoteException
	{
		this(target, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte []data) throws IOException
	{
		_transferer.write(_offset, data.length, 0, data);
		_offset += data.length;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(byte []data, int offset, int length)
		throws IOException
	{
		byte []newData = new byte[length];
		System.arraycopy(data, offset, newData, 0, length);
		write(newData);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void write(int b) throws IOException
	{
		write(new byte[] { (byte)b });
	}

	/**
	 * A convenience method for creating a buffered stream (using the
	 * current transferer's preferred buffering size) from this output stream.
	 * 
	 * @return The newly created buffered output stream.
	 */
	public BufferedOutputStream createPreferredBufferedStream()
	{
		return new BufferedOutputStream(
			this, _transferer.getPreferredWriteSize());
	}
}