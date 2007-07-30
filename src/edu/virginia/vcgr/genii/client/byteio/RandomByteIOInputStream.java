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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

class RandomByteIOInputStream extends InputStream
{
	private RandomByteIOPortType _stub;
	private IRByteIOTransferer _transferer;
	private long _offset = 0;
	
	RandomByteIOInputStream(EndpointReferenceType epr, URI xferType)
		throws ConfigurationException, RemoteException
	{
		_stub = ClientUtils.createProxy(RandomByteIOPortType.class, epr);
		
		if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_DIME_URI))
			_transferer = new DimeRByteIOTransferer(_stub);
		else if (xferType.equals(ByteIOConstants.TRANSFER_TYPE_MTOM_URI))
			_transferer = new MtomRByteIOTransferer(_stub);
		else
			_transferer = new SimpleRByteIOTransferer(_stub);
	}
	
	RandomByteIOInputStream(RNSPath path) throws IOException,
		RemoteException, ConfigurationException, RNSException
	{
		if (!path.exists())
			throw new FileNotFoundException("Couldn't find file \"" +
				path.pwd() + "\".");
		
		if (!path.isFile())
			throw new RNSException("Path \"" + path.pwd() + 
				"\" does not represent a file.");
		
		_transferer = new SimpleRByteIOTransferer(
			ClientUtils.createProxy(RandomByteIOPortType.class, path.getEndpoint()));
	}
	
	public void close() throws IOException
	{
		_transferer.close();
		_transferer = null;
	}
	
    public int read() throws IOException
    {
    	byte []data = _transferer.read(_offset, 1, 1, 0);
    	if (data.length == 1)
    	{
    		_offset++;
    		return data[0];
    	}
    	
    	return -1;
    }

    public int read(byte[] b) throws IOException
    {
    	byte []data = _transferer.read(_offset, b.length, 1, 0);
    	_offset += data.length;
    	System.arraycopy(data, 0, b, 0, data.length);
    	return (data.length == 0) ? -1 : data.length;
    }
    
    public int read(byte[] b, int off, int len) throws IOException
    {
    	byte []data = _transferer.read(_offset, len, 1, 0);
    	_offset += data.length;
    	System.arraycopy(data, 0, b, off, data.length);
    	return (data.length == 0) ? -1 : data.length;
    }
    
    public long skip(long n)
    {
    	_offset += n;
    	return _offset;
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
