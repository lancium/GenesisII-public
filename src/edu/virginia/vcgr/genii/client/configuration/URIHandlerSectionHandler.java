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
package edu.virginia.vcgr.genii.client.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.IXMLConfigurationSectionHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.io.IURIHandler;

public class URIHandlerSectionHandler 
	implements IXMLConfigurationSectionHandler
{
	static private QName _CHILD_QNAME = new QName(GenesisIIConstants.GENESISII_NS,
		"uri-handler");
	
	public Object parse(Node n) throws ConfigurationException
	{
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		Collection<IURIHandler> handlers = new ArrayList<IURIHandler>(length);
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				QName childQName = new QName(
					child.getNamespaceURI(), child.getLocalName());
				if (!childQName.equals(_CHILD_QNAME))
					throw new ConfigurationException(
						"Found unexpected child node \"" + childQName + "\".");
				
				String className = child.getTextContent();
				
				try
				{
					Class cl = Class.forName(className, true, loader);
					if (!IURIHandler.class.isAssignableFrom(cl))
						throw new ConfigurationException("Class \"" + 
							className + 
							"\" does not implement the IURIHandler interface.");
					Constructor cons = cl.getConstructor(new Class[0]);
					handlers.add((IURIHandler)cons.newInstance(new Object[0]));
				}
				catch (NoSuchMethodException nsme)
				{
					throw new ConfigurationException(
						"Couldn't find default constructor for \"" + className +
						"\".", nsme);
				}
				catch (InstantiationException ie)
				{
					throw new ConfigurationException(ie.getMessage(), ie);
				}
				catch (InvocationTargetException ite)
				{
					throw new ConfigurationException(ite.getMessage(), ite);
				}
				catch (IllegalAccessException iae)
				{
					throw new ConfigurationException(iae.getMessage(), iae);
				}
				catch (ClassNotFoundException cnfe)
				{
					throw new ConfigurationException("Couldn't locate class \""
						+ className + "\".");
				}
			}
		}
		
		return handlers;
	}
}
