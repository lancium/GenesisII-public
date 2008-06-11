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
import java.util.Properties;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;

public class MailtoURIHandler extends AbstractURIHandler
	implements IURIHandler
{
	public String[] getHandledProtocols()
	{
		return new String[] {"mailto"};
	}

	public boolean canRead(String uriScheme)
	{
		return false;
	}

	public boolean canWrite(String uriScheme)
	{
		return (uriScheme != null && uriScheme.equals("mailto"));
	}

	public InputStream openInputStream(URI uri,
		UsernamePasswordIdentity credential) throws IOException
	{
		throw new IOException("Cannot read from mailto URIs.");
	}

	public OutputStream openOutputStream(URI uri, 
		UsernamePasswordIdentity credential) throws IOException
	{
		if (credential != null)
			throw new IOException(
				"Don't know how to perform mailto with a credential.");
		
		String address;
		Properties headers = new Properties();
		
		String schemeSpecific = uri.getSchemeSpecificPart();
		int index = schemeSpecific.indexOf('?');
		if (index < 0)
		{
			address = schemeSpecific;
		} else
		{
			address = schemeSpecific.substring(0, index);
			schemeSpecific = schemeSpecific.substring((index + 1));
			String []headerStrings = schemeSpecific.split("&");
			for (String header : headerStrings)
			{
				index = header.indexOf('=');
				if (index > 0)
				{
					headers.put(
						header.substring(0, index), header.substring(index + 1));
				}
			}
		}
		
		return new MailOutputStream(address, headers);
	}
}
