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
package edu.virginia.vcgr.genii.client.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.byteio.ByteIOInputStream;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOutputStream;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class RNSURIHandler implements IURIHandler
{
	static private final String []_HANDLED_PROTOCOLS =
		new String[] { "rns" };
	
	public boolean canRead(String uriScheme)
	{
		return uriScheme != null && uriScheme.equals(_HANDLED_PROTOCOLS[0]);
	}

	public boolean canWrite(String uriScheme)
	{
		return uriScheme != null && uriScheme.equals(_HANDLED_PROTOCOLS[0]);
	}

	public String[] getHandledProtocols()
	{
		return _HANDLED_PROTOCOLS;
	}

	public InputStream openInputStream(URI uri) throws IOException
	{
		try
		{
			RNSPath path = RNSPath.getCurrent();
			path = path.lookup(uri.getSchemeSpecificPart(), 
				RNSPathQueryFlags.MUST_EXIST);
			return new ByteIOInputStream(path);
		}
		catch (RNSException re)
		{
			throw new IOException(re.getMessage());
		}
		catch (ConfigurationException ce)
		{
			throw new IOException("Unable to open input stream.", ce);
		}
	}

	public OutputStream openOutputStream(URI uri) throws IOException
	{
		try
		{
			RNSPath path = RNSPath.getCurrent();
			path = path.lookup(uri.getSchemeSpecificPart(), 
				RNSPathQueryFlags.DONT_CARE);
			return new ByteIOOutputStream(path);
		}
		catch (RNSException re)
		{
			throw new IOException(re.getMessage());
		}
		catch (ConfigurationException ce)
		{
			throw new IOException(ce.getMessage());
		}
	}
}
