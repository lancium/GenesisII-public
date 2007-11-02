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
package edu.virginia.vcgr.genii.client.rns;

import org.ws.addressing.EndpointReferenceType;
import java.io.*;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;

public class PathElement implements Externalizable 
{	
	static final long serialVersionUID = 0L;

	private String _nameFromParent;
	private EndpointReferenceType _endpoint;
	
	// No-arg constructor for serialization
	public PathElement() {}
	
	public PathElement(String nameFromParent, 
		EndpointReferenceType endpoint)
	{
		_nameFromParent = nameFromParent;
		_endpoint = endpoint;
	}
	
	public String getName()
	{
		return _nameFromParent;
	}
	
	public EndpointReferenceType getEndpoint()
	{
		return _endpoint;
	}
	
	public void setEndpoint(EndpointReferenceType epr)
	{
		_endpoint = epr;
	}
	
	public boolean isRoot()
	{
		return (_nameFromParent == null);
	}
	
    public void writeExternal(ObjectOutput out) throws IOException
    {
    	out.writeObject(_nameFromParent);
    	
    	byte []data = EPRUtils.toBytes(_endpoint);
    	out.writeInt(data.length);
    	out.write(data);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
    	_nameFromParent = (String) in.readObject();
    	byte []data;
    	int length = in.readInt();
    	data = new byte[length];
    	int position = 0;
    	int read;
    	
    	while (length > 0)
    	{
    		read = in.read(data, position, length);
    		if (read <= 0)
    			throw new IOException("Unable to read EPR from input stream.");
    		position += read;
    		length -= read;
    	}
    	_endpoint = EPRUtils.fromBytes(data);
    }	
}