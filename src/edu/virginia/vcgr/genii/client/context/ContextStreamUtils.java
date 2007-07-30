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
package edu.virginia.vcgr.genii.client.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.io.Reader;
import java.net.URL;

import javax.xml.namespace.QName;

import org.morgan.util.io.StreamUtils;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.client.ser.ObjectSerializer;
import edu.virginia.vcgr.genii.context.ContextType;

public class ContextStreamUtils
{
	static public QName CONTEXT_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "context-information");
	
	static public ICallingContext load(URL url) throws IOException
	{
		InputStream in = null;
		
		try
		{
			in = url.openConnection().getInputStream();
			return load(in);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	static public ICallingContext load(InputStream in) throws IOException
	{
		return load(new InputSource(in));
	}
	
	static public ICallingContext load(Reader in) throws IOException
	{
		return load(new InputSource(in));
	}
	
	static public ICallingContext load(InputSource in) throws IOException
	{
		try
		{
			ContextType ct = (ContextType)ObjectDeserializer.deserialize(
				in, ContextType.class);
			return new CallingContextImpl(ct);
		}
		catch (ResourceException re)
		{
			throw new IOException(re.getMessage());
		}
	}
	
	static public void store(Writer out, ICallingContext context)
		throws IOException
	{
		try
		{
			ContextType ct = context.getSerialized();
			ObjectSerializer.serialize(out, ct, CONTEXT_QNAME);
		}
		catch (ResourceException re)
		{
			throw new IOException(re.getMessage());
		}
	}
}
