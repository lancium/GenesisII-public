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

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.axis.types.URI;
import org.ggf.rbyteio.RandomByteIOPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.xfer.IRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.dime.DimeRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.mtom.MtomRByteIOTransferer;
import edu.virginia.vcgr.genii.client.byteio.xfer.simple.SimpleRByteIOTransferer;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

class RandomByteIOOutputStream extends OutputStream
{
	private RandomByteIOPortType _stub;
	private IRByteIOTransferer _transferer;
	private long _offset = 0;
	
	RandomByteIOOutputStream(EndpointReferenceType epr, URI xferType)
		throws ResourceException, ConfigurationException, RemoteException
	{
		_stub = ClientUtils.createProxy(RandomByteIOPortType.class, epr);

		if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
			_transferer = new DimeRByteIOTransferer(_stub);
		else if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
			_transferer = new MtomRByteIOTransferer(_stub);
		else
			_transferer = new SimpleRByteIOTransferer(_stub);
		
		_transferer.truncAppend(0, new byte[0]);
	}
	
	RandomByteIOOutputStream(RNSPath path)
		throws ResourceException, ConfigurationException, RNSException,
			RemoteException
	{
		if (path.exists())
		{
			if (!path.isFile())
				throw new RNSException("Path \"" + path.pwd() + 
					"\" does not represent a file.");
		} else
		{
			path.createFile();
		}
		
		_transferer = new SimpleRByteIOTransferer(
			ClientUtils.createProxy(RandomByteIOPortType.class, path.getEndpoint()));
		_transferer.truncAppend(0, new byte[0]);
	}
	
	public void close() throws IOException
	{
		_transferer.close();
		_transferer = null;
	}
	
    public void write(byte[] b) throws IOException
    {
    	_transferer.write(_offset, b.length, 0, b);
    	_offset += b.length;
    }
    
	public void write(byte[] b, int off, int len) throws IOException
	{
		byte []data = new byte[len];
		System.arraycopy(b, off, data, 0, len);
		_transferer.write(_offset, len, 0, data);
		_offset += len;
	}
	
    public void write(int b) throws IOException
    {
    	_transferer.write(_offset, 1, 0, new byte[] {(byte)b});
    	_offset += 1;
    }
    
	public void setTransferMechanism(URI transferMechanism)
	{
		if (transferMechanism.equals(
			ByteIOConstants.TRANSFER_TYPE_DIME_URI))
			_transferer = new DimeRByteIOTransferer(_stub);
		else if (transferMechanism.equals(
			ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
			_transferer = new MtomRByteIOTransferer(_stub);
		else
			_transferer = new SimpleRByteIOTransferer(_stub);
	}
}
