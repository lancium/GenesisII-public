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
import java.net.URISyntaxException;
import java.net.URL;

import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;

public class JavaURIAsURLHandler extends AbstractURIHandler
	implements IURIHandler
{
	static private final String []_HANDLED_PROTOCOLS = 
		new String [] {"http", "https", "ftp"};
	
	public String[] getHandledProtocols()
	{
		return _HANDLED_PROTOCOLS;
	}
	
	public boolean canRead(String protocol)
	{
		return (protocol != null) && isHandledProtocol(protocol);
	}

	public boolean canWrite(String protocol)
	{
		if ((protocol != null) && protocol.equals("ftp"))
			return true;
		
		return false;
	}
	
	static private URI swizzleURICredentials(URI uri,
		UsernamePasswordIdentity credentials) throws URISyntaxException
	{
		if (credentials == null)
			return uri;
		
		int port = uri.getPort();
		String portString;
		if (port >= 0)
			portString = String.format(":%d", port);
		else
			portString = "";
		
		return new URI(
			uri.getScheme(), 
			String.format("%s:%s@%s%s", credentials.getUserName(),
				credentials.getPassword(), uri.getHost(), portString),
			uri.getPath(), uri.getQuery(), uri.getFragment());
	}
	
	public InputStream openInputStream(URI uri,
		UsernamePasswordIdentity credential) throws IOException
	{
		try
		{
			URL url = swizzleURICredentials(uri, credential).toURL();
			return url.openConnection().getInputStream();
		}
		catch (URISyntaxException use)
		{
			throw new IOException("Unable to parse URI.", use);
		}
	}

	public OutputStream openOutputStream(URI uri,
		UsernamePasswordIdentity credential) throws IOException
	{
		try
		{
			URL url = swizzleURICredentials(uri, credential).toURL();
			return url.openConnection().getOutputStream();
		}
		catch (URISyntaxException use)
		{
			throw new IOException("Unable to parse URI.", use);
		}
	}

	static private boolean isHandledProtocol(String protocol)
	{
		for (String proto : _HANDLED_PROTOCOLS)
		{
			if (proto.equals(protocol))
				return true;
		}
		
		return false;
	}
}
