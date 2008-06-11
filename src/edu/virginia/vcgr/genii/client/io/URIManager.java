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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.xml.namespace.QName;

import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.UsernamePasswordIdentity;

@SuppressWarnings("unchecked")
public class URIManager
{
	static private QName _URI_HANDLER_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "uri-handlers");
	
	static private HashMap<String, IURIHandler> _handlers =
		new HashMap<String, IURIHandler>();
	
	static
	{
		XMLConfiguration conf = 
			ConfigurationManager.getCurrentConfiguration().getClientConfiguration();
		
		Collection<IURIHandler> handlers =
			(Collection<IURIHandler>)conf.retrieveSection(_URI_HANDLER_QNAME);
		
		for (IURIHandler handler : handlers)
		{
			String []schemes = handler.getHandledProtocols();
			for (String scheme : schemes)
			{
				_handlers.put(scheme, handler);
			}
		}
	}
	
	static public String[] getHandledProtocols()
	{
		String []ret;
		Set<String> keys = _handlers.keySet();
		ret = new String[keys.size()];
		keys.toArray(ret);
		
		return ret;
	}
	
	static public boolean canRead(String scheme)
	{
		IURIHandler handler = _handlers.get(scheme);
		return ((handler != null) && handler.canRead(scheme));
	}
	
	static public boolean canWrite(String scheme)
	{
		IURIHandler handler = _handlers.get(scheme);
		return ((handler != null) && handler.canWrite(scheme));
	}
	
	static public void get(URI source, File target, 
		UsernamePasswordIdentity credential) throws IOException
	{
		String scheme = source.getScheme();
		if (scheme == null)
			throw new IOException("Don't know how to handle \"" + source + "\".");
		
		IURIHandler handler = _handlers.get(scheme);
		if (handler == null)
			throw new IOException("Don't know how to handle \"" + source + "\".");
		
		handler.get(source, target, credential);
	}
	
	static public void put(File source, URI target,
		UsernamePasswordIdentity credential) throws IOException
	{
		String scheme = target.getScheme();
		if (scheme == null)
			throw new IOException("Don't know how to handle \"" + target + "\".");
		
		IURIHandler handler = _handlers.get(scheme);
		if (handler == null)
			throw new IOException("Don't know how to handle \"" + target + "\".");
		
		handler.put(source, target, credential);
	}
}
