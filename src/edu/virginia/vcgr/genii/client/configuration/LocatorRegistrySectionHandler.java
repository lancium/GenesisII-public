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

import java.util.HashMap;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.IXMLConfigurationSectionHandler;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LocatorRegistrySectionHandler implements
		IXMLConfigurationSectionHandler
{
	static public final String INTERFACE_TYPE_NAME = "interface";
	static public final String LOCATOR_TYPE_NAME = "locator-type";
	
	static private Class findClass(String className)
		throws ConfigurationException
	{
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		
		try
		{
			return Class.forName(className, true, loader);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ConfigurationException("Couldn't locate class \""
				+ className + "\".");
		}
	}
	
	public Object parse(Node n) throws ConfigurationException
	{
		HashMap<String, Class> locators = new HashMap<String, Class>();
		
		NodeList children = n.getChildNodes();
		int length = children.getLength();
		
		for (int lcv = 0; lcv < length; lcv++)
		{
			Node child = children.item(lcv);
			if (child.getNodeType() == Node.ELEMENT_NODE)
			{
				NamedNodeMap attrs = child.getAttributes();

				Node interfaceNode = attrs.getNamedItem(INTERFACE_TYPE_NAME);
				if (interfaceNode == null)
					throw new ConfigurationException(
						"Couldn't find interface attribute.");
				
				Node nameNode = attrs.getNamedItem(LOCATOR_TYPE_NAME);
				if (nameNode == null)
					throw new ConfigurationException(
						"Couldn't find locator-type attribute.");
				
				locators.put(interfaceNode.getTextContent(),
					findClass(nameNode.getTextContent()));
			}
		}
		
		return locators;
	}
}
