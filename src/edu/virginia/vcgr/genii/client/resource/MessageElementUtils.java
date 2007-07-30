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
package edu.virginia.vcgr.genii.client.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.axis.message.MessageElement;

import org.morgan.util.io.StreamUtils;
import org.xml.sax.InputSource;

import edu.virginia.vcgr.genii.client.ser.AnyHelper;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;

public class MessageElementUtils
{
	static public byte[] toBytes(MessageElement me)
		throws ResourceException
	{
		ByteArrayOutputStream baos = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			OutputStreamWriter writer =  new OutputStreamWriter(baos);
	        try {
	            AnyHelper.write(writer, me);
	        } catch (Exception e) {
	            throw new ResourceException("Generic Serialization Error.", e);
	        }
			writer.flush();
			writer.close();
			return baos.toByteArray();
		}
		catch (IOException ioe)
		{
			throw new ResourceException(ioe.toString(), ioe);
		}
		finally
		{
			StreamUtils.close(baos);
		}
			
	}
	
	static public MessageElement fromBytes(byte []data)
		throws ResourceException
	{
		ByteArrayInputStream bais = null;
		
		try
		{
			bais = new ByteArrayInputStream(data);
			return (MessageElement)ObjectDeserializer.deserialize(
				new InputSource(bais),
				MessageElement.class);
		}
		finally
		{
			StreamUtils.close(bais);
		}
	}
}
